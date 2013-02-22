package org.openforis.calc.importer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * Not thread-safe, callers must manage synchronization
 * 
 * @author G. Miceli
 *
 */
public abstract class AbstractFlatFileImporter {
	private static final int DEFAULT_INSERT_FREQUENCY = 10000;
	private static final int DEFAULT_REPORT_FREQUENCY = 10000;
	private int rowCount;
	private int reportFrequency;
	private int insertFrequency;
	private long startTime;
	private long endTime;
	private Log log = LogFactory.getLog(getClass());
	private boolean active;

	public AbstractFlatFileImporter() {
		reportFrequency = DEFAULT_REPORT_FREQUENCY;
		insertFrequency = DEFAULT_INSERT_FREQUENCY;
		startTime = -1;
		endTime = -1;
		active = false;
	}
	
	@Transactional
	public void importData(FlatDataStream stream) throws ImportException, IOException {
		try {
			startTime = System.currentTimeMillis();
			endTime = -1;
			rowCount = 0;
			active = true;
			
			onStart();

			FlatRecord record;
			while ( (record = stream.nextRecord()) != null ) {
				processRecord(record);
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
			endTime = System.currentTimeMillis();
			onEnd();
		} finally {
			cleanup();
			active = false;
		}
	}

	protected void reportProgress() {
		log.info(rowCount + " rows processed");
	}

	protected abstract void performInserts();

	protected abstract void processRecord(FlatRecord record);

	protected void onStart() {
	}

	protected void onEnd() {
	}

	protected void cleanup() {
	}

	public long getDuration() {
		return active ? (System.currentTimeMillis() - startTime) :
			endTime - startTime;
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
		return active;
	}
	
	protected String nvl(String code, Integer no) {
		return code == null ? no + "" : code;
	}

}
