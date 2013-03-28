package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT;

import java.io.IOException;
import java.util.List;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.model.PlotSection;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.SamplePlot;
import org.openforis.calc.persistence.jooq.tables.records.PlotSectionRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component 
@Transactional
public class PlotSectionDao extends JooqDaoSupport<PlotSectionRecord, PlotSection> {

//	private static final Log log = LogFactory.getLog( PlotSectionDao.class );
	
	private final static org.openforis.calc.persistence.jooq.tables.PlotSection P = PLOT_SECTION;
	
	
	@Autowired
	private SurveyDao surveyDao;

	@Autowired
	private PlotSectionValueDao plotSectionValueDao;
	
//	@Autowired
//	private PlotNumericVariableDao plotNumericVariableDao;
//	@Autowired
//	private PlotCategoricalValueDao plotCategoricalValueDao;
	
	
	public PlotSectionDao() {
		super(PLOT_SECTION, PlotSection.class, 
				PLOT_SECTION.SAMPLE_PLOT_ID, PLOT_SECTION.PLOT_SECTION_, PLOT_SECTION.VISIT_TYPE);
	}

	@Transactional
	public Integer getId(int samplePlotId, String section, String visitType) {
		return getIdByKey(samplePlotId, section, visitType);
	}
	
//	@Transactional
//	public void batchInsert(FlatDataStream dataStream, List<VariableMetadata> variables, List<SamplePlot> groundPlots) throws IOException {
//		Factory create = getJooqFactory();
//		List<Query> queries = new ArrayList<Query>();
//		Set<String> plotIdents = new HashSet<String>();
//		FlatRecord r;
//		int rowCount = 0;
//		
//		while( (r=dataStream.nextRecord()) != null ) {
//			rowCount ++;
//
//			String clusterCode = r.getValue("cluster_code",String.class);
//			Integer plotNo = r.getValue("plot_no",Integer.class);
//			String plotSection = r.getValue("plot_section",String.class);
//			String visitType = r.getValue("visit_type",String.class);
//			if ( plotNo == null ) {
//				log.warn("Skipping plot with missing plot_no at row "+ rowCount );
//				continue;			
//			}
//			
//			String plotIdent = PlotSection.getPlotIdentifer(clusterCode, plotNo, plotSection, visitType);
//			Date surveyDate = r.getValue("survey_date", Date.class);
//			Double gpsX = r.getValue("gps_reading_x", Double.class);
//			Double gpsY = r.getValue("gps_reading_y",Double.class);
//			String gpsSrs = r.getValue("gps_reading_srs_id",String.class);
//			Integer step = r.getValue("step",Integer.class);
//			Double share = r.getValue("percent_share",Double.class);
//			Double direction = r.getValue("center_direction", Double.class);
//			Double distance = r.getValue("center_distance",Double.class);
//			Boolean accessible = r.getValue("accessible",Boolean.class);
//			
//			if ( plotIdents.contains(plotIdent) ) {
//				log.warn("Skipping duplicate plot "+plotIdent+" at row "+ rowCount );
//				continue;
//			}
//			
//			SamplePlot splot = getGroundPlot(groundPlots, clusterCode, plotNo);
//			if ( splot == null ) {
//				log.warn("Skipping unrecognized plot "+plotIdent+" at row "+rowCount);
//				continue;
//			}
//			
//			GeodeticCoordinate gpsReading = GeodeticCoordinate.toInstance(gpsX, gpsY, gpsSrs);
//			if ( gpsReading == null ) {
//				log.warn("Skipping plot with invalid gps_reading: "+plotIdent+" at row "+rowCount);
//				continue;
//			}
//			
//			Integer samplePlotId = splot.getId();
//			
//			Field<Long> s = Sequences.PLOT_SECTION_ID_SEQ.nextval();
//			Long plotSectionId = create.select(s).fetchOne(s);
//			
//			Query insert = create
//				.insertInto(PLOT_SECTION, P.PLOT_SECTION_ID, P.SAMPLE_PLOT_ID, P.PLOT_SECTION_,P.PLOT_SECTION_SURVEY_DATE, P.VISIT_TYPE, P.STEP, P.PLOT_GPS_READING, P.PLOT_ACTUAL_LOCATION,
//						P.ACCESSIBLE, P.PLOT_SHARE, P.PLOT_DIRECTION, P.PLOT_DISTANCE)
//				.values( plotSectionId, samplePlotId, plotSection, surveyDate, visitType, step, gpsReading, gpsReading,
//						accessible,share, direction, distance);
//			
//			queries.add( insert );
//			
//			
//			for (VariableMetadata var : variables) {
//				String name = var.getVariableName();
//				if ( var.isNumeric() ) {
//					Double value = r.getValue(name, Double.class);
//					if ( value != null ) {
//						Query insertPNV = create
//							.insertInto(PNV, PNV.PLOT_SECTION_ID, PNV.VARIABLE_ID, PNV.VALUE, PNV.COMPUTED)
//							.values(plotSectionId, var.getVariableId(), value, false );
//						queries.add(insertPNV);
//					}
//				}
//				if ( var.isCategorical() ) {
//					String code = r.getValue(name, String.class);
//					if ( code != null ) {
//						// TODO doesn't work for categorical variables
//						Category cat = var.getCategoryByCode(code);
//						if ( cat == null ) {
//							log.warn("Skipping unknown code "+code);
//						} else {
//							Query insertPCV = create.insertInto(PCV, PCV.PLOT_SECTION_ID, PCV.CATEGORY_ID, PCV.COMPUTED)
//													.values( plotSectionId, cat.getCategoryId(), false );
//							
//							queries.add(insertPCV);
//						}
//					}
//				}
//			}		
//			plotIdents.add( plotIdent );
//		}
//		
//		create.batch( queries ).execute();
//	}
	
//	private SamplePlot getGroundPlot(List<SamplePlot> groundPlots, String clusterCode, Integer plotNo) {
//		for ( SamplePlot plot : groundPlots ) {
//			String plotKey = getPlotKey(plot.getCluster(), plot.getPlotNo());
//			if(plotKey.equals( getPlotKey(clusterCode, plotNo))){
//				return plot;
//			}
//		}
//		return null;
//	}
	
//	private String getPlotKey(Cluster cluster, int plotNo) {
//		String clusterCode = cluster == null ? null : cluster.getClusterCode();
//		return getPlotKey(clusterCode, plotNo);		
//	}
	
//	private String getPlotKey(String clusterCode, int plotNo) {
//		if ( clusterCode == null ) {
//			return Integer.toString(plotNo);
//		} else {
//			return clusterCode + "_" + plotNo;
//		}
//	}
	
	@Transactional
	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.PlotSection ps = PLOT_SECTION.as("PS");
		SamplePlot sp = SAMPLE_PLOT.as("sp");
		create.delete(ps)
			  .where(ps.SAMPLE_PLOT_ID.in(
					  	create.select(sp.SAMPLE_PLOT_ID)
					  		  .from(sp)
					  		  .where(sp.OBS_UNIT_ID.eq(id))))
	  		  .execute();
	}

	@Transactional
	public void updateArea(FlatDataStream dataStream) throws IOException {
		startBatch();
		Factory create = getBatchFactory();
		
		FlatRecord r ;
		while( (r=dataStream.nextRecord()) != null ) {
			Double area = r.getValue("area", Double.class);
			Integer plotSectionId = r.getValue("plot_section_id", Integer.class);
			
			Query update = 
					create
					.update(P)
					.set(P.PLOT_SECTION_AREA, area)
					.where(P.PLOT_SECTION_ID.eq(plotSectionId));
			
			addQueryToBatch( update );
		}
		
		executeBatch();
	}

	@Transactional
	public void updateCurrentValues(Integer obsUnitId, FlatDataStream dataStream, List<VariableMetadata> variables) throws IOException {
		
		plotSectionValueDao.updateCurrentValues(obsUnitId, dataStream, variables);
		
	}
	
	
}