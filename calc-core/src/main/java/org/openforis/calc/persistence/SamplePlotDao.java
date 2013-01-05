package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.List;

import org.jooq.impl.Factory;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SamplePlotRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class SamplePlotDao extends JooqDaoSupport<SamplePlotRecord, SamplePlot> {

	public SamplePlotDao() {
		super(SAMPLE_PLOT, SamplePlot.class);
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

	public List<SamplePlot> findGroundPlotsBySurveyId(int surveyId) {
		Factory create = getJooqFactory();
		return 
		    create.select(SAMPLE_PLOT.getFields())
		          .from(SAMPLE_PLOT, OBSERVATION_UNIT)
		          .where(SAMPLE_PLOT.OBS_UNIT_ID.eq(OBSERVATION_UNIT.ID)
		        		  .and(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId))
		        		  .and(SAMPLE_PLOT.GROUND_PLOT.isTrue()))
		        		  .fetch().into(SamplePlot.class);
	}
}