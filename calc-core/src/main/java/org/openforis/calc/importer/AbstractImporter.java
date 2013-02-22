package org.openforis.calc.importer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class AbstractImporter {
	private int rowCount;
	private int duration;
	private int reportFrequency;
	private int insertFrequency;
	private long startTime;
	private Log log = LogFactory.getLog(getClass());

	public AbstractImporter() {
		reportFrequency = 10000;
		insertFrequency = 10000;
		startTime = -1;
	}
	
	@Transactional
	public void importData(FlatDataStream stream) throws ImportException, IOException {
		try {
			onStart();
			
			FlatRecord record;
			while ( (record = stream.nextRecord()) != null ) {
				processRow(record);
				rowCount += 1;
				if ( rowCount % insertFrequency == 0 ) {
					performInserts();
				}
				if ( rowCount % reportFrequency == 0 ) {
					reportProgress();
				}
			}

			if ( rowCount % insertFrequency != 0 ) {
				performInserts();
			}
			if ( rowCount % reportFrequency != 0 ) {
				reportProgress();
			}
			onEnd();
		} finally {
			cleanup();
		}
	}

	protected void reportProgress() {
		log.info(rowCount + " rows processed");
	}

	protected abstract void performInserts();

	protected abstract void processRow(FlatRecord record);


	protected void onStart() {
		startTime = System.currentTimeMillis();
		rowCount = 0;
	}

	protected void onEnd() {
		duration = (int) (System.currentTimeMillis() - startTime);
	}

	protected void cleanup() {
		startTime = -1;
	}

	public int getDuration() {
		return duration;
	}

	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	public int getInsertFrequency() {
		return insertFrequency;
	}

	public void setInsertFrequency(int insertFrequency) {
		this.insertFrequency = insertFrequency;
	}

	public boolean isActive() {
		return startTime > 0;
	}
}
