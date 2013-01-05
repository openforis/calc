package org.openforis.calc.dataimport;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.service.MetadataService;
import org.openforis.calc.util.csv.CsvLine;
import org.openforis.calc.util.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public abstract class AbstractFieldDataCsvImporter {
	
	// TODO refactor better and clean up
	
	@Autowired 
	private SurveyDao surveyDao;
	@Autowired 
	private ObservationUnitDao observationUnitDao;
	@Autowired
	private MetadataService metadataService;

	protected Log log = LogFactory.getLog(getClass());
	
	private long start;
	private int insertCount;
	private int duration;
	private int reportFrequency;
	private int rowCount;

	private Survey survey;
	private ObservationUnit unit;
	
	public AbstractFieldDataCsvImporter() {
		reportFrequency = 10000;
	}

	protected Survey loadSurvey(String uri) throws ImportException {
		Survey survey = surveyDao.findByUri(uri);
		if ( survey == null ) {
			throw new ImportException("No survey with URI "+uri);
		}
		return survey;
	}

	public int getInsertCount() {
		return insertCount;
	}

	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	public int getRowCount() {
		return rowCount;
	}

	protected Survey getSurvey() {
		return survey;
	}
	
	@Transactional
	synchronized
	public void doImport(String surveyUri, String unitName, String filename) throws ImportException, IOException {
		resetCounters();
		survey = loadSurvey(surveyUri);
		metadataService.loadSurveyMetadata(survey);
		unit = survey.getObservationUnitByName(unitName);
		CsvReader reader = null;
		try {
			FileReader fileReader = new FileReader(filename);
			reader = new CsvReader(fileReader);
			reader.readHeaderLine();
			beforeImport(reader);
			CsvLine line;
			while ((line = reader.readNextLine()) != null) {				
				nextLine();
				processLine(line);
		    }			
		} catch ( Exception ex ) {
			throw new ImportException("Error importing row "+getRowCount(), ex);
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
		duration = (int) (System.currentTimeMillis() - start);
        log.info("Inserted "+insertCount+" records in "+duration/1000.0+"s");
	}

	protected abstract void beforeImport(CsvReader reader)  throws ImportException, IOException;
	
	protected abstract void processLine(CsvLine line) throws ImportException, IOException;
	
	protected Integer parsePlotSectionNo(String section) throws ImportException {
		if ( section == null || section.isEmpty() ) {
			return 1;
		} else if ( section.matches("[0-9]+") ) {
			return Integer.valueOf(section);
		} else if ( section.matches("[A-Za-z]") ) {
			// convert A, B, C.. to 1, 2, 3..
			return section.toUpperCase().charAt(0) - 64;
		} else {
			throw new ImportException("Invalid plot section '"+section+"'");
		}
	}

	protected String getPlotIdentifer(String clusterCode, Integer plotNo, String plotSection, String surveyType) {
		StringBuilder sb = new StringBuilder();
		sb.append(clusterCode);
		sb.append(" ");
		sb.append(plotNo);
		if ( plotSection != null ) {
			sb.append(" ");
			sb.append(plotSection);
		}
		if ( !"P".equals(surveyType) ) { 
			sb.append(" (");
			sb.append(surveyType);
			sb.append(")");
		}
		return sb.toString();
	}
	
	private void resetCounters() {
		start = System.currentTimeMillis();
		insertCount = 0;
		rowCount = 0;
	}
	
	protected void nextLine() {
		rowCount += 1;				
		if ( rowCount % reportFrequency == 0 ) {
			log.info(rowCount+" rows read, "+insertCount+" records inserted.");
		}
	}
	
	protected void incrementInsertCount() {
		insertCount += 1;
	}
	
	public int getDuration() {
		return duration;
	}
	
	protected List<Variable> getVariables(List<String> colnames) {
		List<String> skipped = new ArrayList<String>();
		List<String> varcols = new ArrayList<String>();
		List<Variable> vars = new ArrayList<Variable>();
		for (String col : colnames) {
			Variable var = unit.getVariable(col);
			if ( var == null ) {
				skipped.add(col);
			} else {
				vars.add(var);
				varcols.add(var.getName());
			}
		}
		log.info("Recognized system attributes: TBD");
		log.info("Recognized user variables: "+varcols);
		log.info("Unrecognized (skipped) columns: "+skipped);
		return vars;
	}

	protected ObservationUnit getObservationUnit() {
		return unit;
	}
}