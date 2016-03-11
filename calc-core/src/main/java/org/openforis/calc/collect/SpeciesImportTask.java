package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jooq.Batch;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.CreateTableStep;
import org.openforis.calc.psql.Psql;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.metadata.species.SpeciesBackupCSVReader;
import org.openforis.collect.io.metadata.species.SpeciesBackupLine;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportTask extends Task {
	
	private static final int MAX_BATCH_SIZE = 1000;

	@Autowired
	private Psql psql;
	
	private File backupFile;
	
	@Override
	public String getName() {
		return "Import species";
	}
	
	@Override
	protected long countTotalItems() {
		int total = 0;
		ZipFile zipFile = null;
		BackupFileExtractor fileExtractor = null;
		try {
			zipFile = new ZipFile(backupFile);
			fileExtractor = new BackupFileExtractor(zipFile);
			List<String> speciesFileNames = fileExtractor.listSpeciesEntryNames();
			for (String entryName : speciesFileNames) {
				CsvReader reader = null;
				try {
					File tempFile = fileExtractor.extract(entryName);
					reader = new CsvReader(tempFile);
					total += reader.size();
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
			return total;
		} catch (Exception e) {
			throw new RuntimeException("Error initializing species import task: " + e.getMessage(), e);
		} finally {
			close(zipFile);
		}
	}

	@Override
	protected void execute() throws Throwable {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(backupFile);
			BackupFileExtractor fileExtractor = new BackupFileExtractor(zipFile);
			List<String> speciesFileNames = fileExtractor.listSpeciesEntryNames();
			for (String entry : speciesFileNames) {
				createSpeciesTable(zipFile, entry);
			}
		} finally {
			close(zipFile);
		}
	}

	protected void createSpeciesTable(ZipFile zipFile, String entry) {
		String speciesListName = FilenameUtils.getBaseName(entry);
		
		BackupFileExtractor fileExtractor = new BackupFileExtractor(zipFile);
		
		SpeciesCodeTable table = createTable(speciesListName);
		
		File tempFile = fileExtractor.extract(entry);
		SpeciesBackupCSVReader reader = null;
		try {
			
			reader = new SpeciesBackupCSVReader(tempFile);
			reader.init();
			
			int nextRecordId = 1;
			List<InsertQuery<Record>> batchInserts = new ArrayList<InsertQuery<Record>>();
			
			Set<String> genuses = new HashSet<String>();
			
			while ( isRunning() ) {
				SpeciesBackupLine line = reader.readNextLine();
				if ( line != null ) {
					String code 				= line.getCode();
					String scientificName 		= line.getScientificName();
					InsertQuery<Record> insert 	= createInsertQuery(table, nextRecordId ++, code, scientificName);
					
					batchInserts.add( insert );
					
					String genus = ( scientificName.indexOf(" ") > 0 ) ? scientificName.substring(0, scientificName.indexOf(" ")) : scientificName;
					genuses.add( genus );
					
					if ( batchInserts.size() == MAX_BATCH_SIZE ) {
						flushBatchInserts(batchInserts);
					}
				}
				incrementItemsProcessed();
				if ( ! reader.isReady() ) {
					break;
				}
			}
			
			batchInserts.add(createInsertQuery(table, nextRecordId ++, "-1", "NA" ));
			genuses.add( "NA" );
			batchInserts.add(createInsertQuery(table, nextRecordId ++, "UNK", "UNKNOWN" ));
			genuses.add( "UNKNOWN" );
			batchInserts.add(createInsertQuery(table, nextRecordId ++, "UNL", "UNLISTED" ));
			genuses.add( "UNLISTED" );
			
			int i = 0;
			for ( String genus : genuses ) {
				String code = "g_" + (i++);
				batchInserts.add( createInsertQuery(table, nextRecordId ++, code , genus , SpeciesCodeTable.Rank.genus.toString() ));
				
				if ( batchInserts.size() == MAX_BATCH_SIZE ) {
					flushBatchInserts(batchInserts);
				}
			}
			
			if ( ! batchInserts.isEmpty() ) {
				//flush remaining inserts
				flushBatchInserts(batchInserts);
			}
			
			createSpeciesView( table );
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private void createSpeciesView(SpeciesCodeTable table) {
		SpeciesCodeView view 		= new SpeciesCodeView( table );
		SpeciesCodeTable genusTable = table.as( "genusTable" );
		
		Table<Record> genusSelect = psql().select().from(table).where(table.getRankField().eq(SpeciesCodeTable.Rank.genus.toString())).asTable( genusTable.getName() );
		
		SelectQuery<Record> select = psql().selectQuery();
		
		select.addSelect( table.getIdField() , table.getCodeField() , table.getScientificNameField() , table.getRankField() );
		
		select.addSelect( genusTable.getIdField().as(view.getGenusIdField().getName()) );
		select.addSelect( genusTable.getCodeField().as(view.getGenusCodeField().getName()) );
		select.addSelect( genusTable.getScientificNameField().as(view.getGenusScientificNameField().getName()) );
		select.addSelect( genusTable.getRankField().as(view.getGenusRankField().getName()) );
		
		select.addFrom( table );
		
		Field<String> speciesTableGenusField = DSL.concat(table.getScientificNameField()  , DSL.field("' '", String.class));
		select.addJoin(
				genusSelect, 
				genusTable.getScientificNameField().eq(
						DSL.substring( speciesTableGenusField, DSL.field("0", Integer.class) , DSL.position(speciesTableGenusField, " "))
						)
				);
		
		select.addConditions( table.getRankField().isNull() );
		
		psql()
			.createView( view )
			.as( select )
			.execute();
	}

	private InsertQuery<Record> createInsertQuery(SpeciesCodeTable table, int recordId, String code, String scientificName ) {
		return createInsertQuery( table, recordId, code, scientificName, null );
	}
	
	private InsertQuery<Record> createInsertQuery(SpeciesCodeTable table, int recordId, String code, String scientificName , String rank) {
		InsertQuery<Record> insert = psql.insertQuery(table);
		insert.addValue(table.getIdField(), recordId);
		insert.addValue(table.getCodeField(), code);
		insert.addValue(table.getScientificNameField(), scientificName);
		
		if( rank != null ){
			insert.addValue( table.getRankField(), rank );
		}
		
		return insert;
	}
	
	private void flushBatchInserts(List<InsertQuery<Record>> inserts) {
		Batch batch = psql.batch(inserts);
		batch.execute();
		inserts.clear();
	}

	protected SpeciesCodeTable createTable(String speciesListName) {
		SpeciesCodeTable table = new SpeciesCodeTable(speciesListName, getInputSchema().getName());
		
		CreateTableStep createTableStep = psql()
			.createTable(table)
			.columns(table.fields());
		
		createTableStep.execute();
		
		return table;
	}

	private void close(ZipFile zipFile) {
		if ( zipFile != null ) {
			try {
				zipFile.close();
			} catch (IOException e) {
			}
		}
	}
	
	public File getBackupFile() {
		return backupFile;
	}
	
	public void setBackupFile(File backupFile) {
		this.backupFile = backupFile;
	}
	
}
