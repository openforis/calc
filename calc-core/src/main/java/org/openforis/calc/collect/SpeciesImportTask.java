package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jooq.Batch;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.CreateTableStep;
import org.openforis.calc.psql.Psql;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.metadata.species.SpeciesBackupCSVReader;
import org.openforis.collect.io.metadata.species.SpeciesBackupLine;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
				File tempFile = fileExtractor.extract(entryName);
				CsvReader reader = new CsvReader(tempFile);
				total += reader.size();
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
				String speciesListName = FilenameUtils.getBaseName(entry);
				
				SpeciesCodeTable table = createTable(speciesListName);
				
				File tempFile = fileExtractor.extract(entry);
				SpeciesBackupCSVReader reader = null;
				try {
					
					reader = new SpeciesBackupCSVReader(tempFile);
					reader.init();
					
					int nextRecordId = 1;
					List<InsertQuery<Record>> batchInserts = new ArrayList<InsertQuery<Record>>();
					
					while ( isRunning() ) {
						SpeciesBackupLine line = reader.readNextLine();
						if ( line != null ) {
							InsertQuery<Record> insertQuery = createInsertQuery(table, line, nextRecordId ++);
							batchInserts.add(insertQuery);
							
							if ( batchInserts.size() == MAX_BATCH_SIZE ) {
								flushBatchInserts(batchInserts);
							}
						}
						incrementItemsProcessed();
						if ( ! reader.isReady() ) {
							break;
						}
					}
					if ( ! batchInserts.isEmpty() ) {
						//flush remaining inserts
						flushBatchInserts(batchInserts);
					}
				} catch(Exception e) {
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		} finally {
			close(zipFile);
		}
	}

	protected InsertQuery<Record> createInsertQuery(SpeciesCodeTable table, SpeciesBackupLine line, int recordId) {
		InsertQuery<Record> insert = psql.insertQuery(table);
		insert.addValue(table.getIdField(), recordId);
		insert.addValue(table.getCodeField(), line.getCode());
		insert.addValue(table.getScientificNameField(), line.getScientificName());
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
