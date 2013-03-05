package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.OBSERVATION_UNIT;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT;

import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.records.SamplePlotRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component 
@Transactional
public class SamplePlotDao extends JooqDaoSupport<SamplePlotRecord, SamplePlot> {

	public SamplePlotDao() {
		super(SAMPLE_PLOT, SamplePlot.class);
	}


	public int nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.SAMPLE_PLOT_ID_SEQ).intValue();
	}

//	public List<SamplePlot> findGroundPlotsByObservationUnitId(int observationUnitId) {
//		Factory create = getJooqFactory();
//		return 
//		    create.select(SAMPLE_PLOT.ID, SAMPLE_PLOT.NO, SAMPLE_PLOT.CLUSTER_ID)
//		          .from(SAMPLE_PLOT)
//		          .where(SAMPLE_PLOT.OBS_UNIT_ID.eq(observationUnitId)
//		        		  .and(SAMPLE_PLOT.GROUND_PLOT.isTrue()))
//		        		  .fetch().into(SamplePlot.class);
//	}

	@Deprecated
	public List<SamplePlot> findGroundPlotsBySurveyId(int surveyId) {
		Factory create = getJooqFactory();
		return 
		    create.select(SAMPLE_PLOT.getFields())
		          .from(SAMPLE_PLOT, OBSERVATION_UNIT)
		          .where(SAMPLE_PLOT.OBS_UNIT_ID.eq(OBSERVATION_UNIT.OBS_UNIT_ID)
		        		  .and(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId))
		        		  .and(SAMPLE_PLOT.GROUND_PLOT.isTrue()))
		        		  .fetch().into(SamplePlot.class);
	}

	public FlatDataStream streamAll(String[] fields, int observationUnitId) {
		return stream(fields, SAMPLE_PLOT.OBS_UNIT_ID, observationUnitId);
	}
	
	public FlatDataStream streamGroundPlots(String[] fields, int observationUnitId) {
		Result<Record> result = selectByName(fields)
		      .from(SAMPLE_PLOT)
		      .where(SAMPLE_PLOT.OBS_UNIT_ID.eq(observationUnitId)
		    		  .and(SAMPLE_PLOT.GROUND_PLOT.isTrue()))
		      .fetch();
		
		return stream(result);
	}

	public FlatDataStream streamPermanentPlots(String[] fields, int observationUnitId) {
		Result<Record> result = selectByName(fields)
		      .from(SAMPLE_PLOT)
		      .where(SAMPLE_PLOT.OBS_UNIT_ID.eq(observationUnitId)
		    		  .and(SAMPLE_PLOT.PERMANENT_PLOT.isTrue()))
		      .fetch();
		
		return stream(result);
	}
	
}
