package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.CreateTableStep;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.metadata.species.SpeciesBackupCSVReader;
import org.openforis.collect.io.metadata.species.SpeciesBackupLine;
import org.openforis.commons.io.csv.CsvReader;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportTask extends Task {
	
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
					int recordId = 1;
					while ( isRunning() ) {
						SpeciesBackupLine line = reader.readNextLine();
						if ( line != null ) {
							insertRecord(table, recordId ++, line);
						}
						incrementItemsProcessed();
						if ( ! reader.isReady() ) {
							break;
						}
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

	protected void insertRecord(SpeciesCodeTable table, int recordId,
			SpeciesBackupLine line) {
		InsertQuery<Record> insert = psql().insertQuery(table);
		insert.addValue(table.getIdField(), recordId ++);
		insert.addValue(table.getCodeField(), line.getCode());
		insert.addValue(table.getScientificNameField(), line.getScientificName());
		insert.execute();
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
