package org.openforis.calc.dataimport.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.calc.dataimport.ImportException;
import org.openforis.calc.dataimport.ModelObjectSourceIdMap;
import org.openforis.calc.model.ImportableModelObject;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.ObservationUnitInstance;
import org.openforis.calc.model.SamplingDesignIdentifiers;
import org.openforis.calc.model.SurveyedCluster;
import org.openforis.calc.model.SurveyedPlot;
import org.openforis.calc.persistence.SurveyedClusterDao;
import org.openforis.calc.persistence.SurveyedPlotDao;
import org.openforis.calc.service.SamplingDesignService;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.TextAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 * @deprecated use FieldDataCsvImporter instead
 */
@Deprecated
public class CollectDataLoader extends CollectLoaderBase {
	
	// TODO cleanup and refactor
	// TODO move into DataService?
	
	private static final String CODE_ATTR = "code";
	private static final String NO_ATTR = "no";
	private static final String SECTION_ATTR = "section";
	private static final String SURVEY_DATE_ATTR = "survey_date";

	@Autowired
	protected SamplingDesignService samplingDesignService;
	@Autowired
	protected SurveyedClusterDao surveyedClusterDao;
	@Autowired
	protected SurveyedPlotDao surveyedPlotDao;
	
	
	private DataUnmarshaller dataUnmarshaller;
	private ModelObjectSourceIdMap sourceIds;
	private SamplingDesignIdentifiers samplingDesignIds;
	private int step;

	private Integer surveyId;
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			CollectDataLoader loader = ctx.getBean(CollectDataLoader.class);
			loader.importData(TEST_PATH, 3);

		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	synchronized
	public void importData(String path, int step) throws IOException, ImportException, InvalidMetadataException {
		try {
			this.step = step;
			collectSurvey = metadataService.loadIdml(path+IDML_FILENAME);
			survey = surveyDao.fetchByUri(collectSurvey.getUri());
			surveyId = survey.getId();
			sourceIds = metadataService.loadSourceIds(surveyId);
			samplingDesignIds = samplingDesignService.loadGroundPlotIds(surveyId);
			DataHandler handler = new DataHandler(collectSurvey, null);
			dataUnmarshaller = new DataUnmarshaller(handler);
			importXml(path+"3/699.xml");
		} catch (IdmlParseException e) {
			throw new ImportException(e);
		}
	}

	private void importXml(String filename) throws ImportException, InvalidMetadataException {
		ParseRecordResult result;
		try {
			result = dataUnmarshaller.parse(filename);
			if ( !result.isSuccess() ) {
				logFailures(filename, result);
				throw new ImportException("Failed to load data");
			}
			logWarnings(filename, result);
			CollectRecord record = result.getRecord();
			importRecord(record);
		} catch (DataUnmarshallerException e) {
			throw new ImportException(e);
		}
	}

	private void importRecord(CollectRecord record) throws ImportException, InvalidMetadataException {
		Entity root = record.getRootEntity();
		traverse(root, null, null, null);
	}

	private void traverse(Entity entity, ObservationUnit parentUnit, SurveyedCluster cluster, 
				ObservationUnitInstance parentUnitObj) 
				throws ImportException, InvalidMetadataException {
		EntityDefinition defn = entity.getDefinition();
		EntityType type = getEntityType(defn);
		if ( type != null ) {
			switch (type) {
			case CLUSTER:
				if ( parentUnit != null ) {
					throw new InvalidMetadataException("Illegal nesting of cluster "+defn.getPath()
								+" inside "+parentUnit.getType());
				}
				cluster = importCluster(entity);
				break;
			case PLOT:
				if ( parentUnit != null && !"plot".equals(parentUnit.getType()) ) {
					throw new InvalidMetadataException("Illegal nesting of plot "+defn.getPath()+" inside "+parentUnit.getType());
				}
				ObservationUnit unit = getObservationUnit(defn);
				if ( !"plot".equals(unit.getType()) ) {
					throw new ImportException("Invalid observation unit type "+unit.getType()+" for "+defn.getPath());					
				}
				if ( parentUnit != null && unit.getParentId() != parentUnit.getId() ) {
					throw new InvalidMetadataException("Obs. unit " + parentUnit.getName() + " not parent of unit " + unit.getName());
				}
				parentUnitObj = importPlot(cluster, unit, entity, (SurveyedPlot) parentUnitObj);
				parentUnit = unit;
				break;
			case SPECIMEN:
				if ( parentUnit != null && !"plot".equals(parentUnit.getType()) ) {
					throw new InvalidMetadataException("Illegal nesting of "+defn.getPath()+" inside "+parentUnit.getType());
				}
//				parentUnit = importSpecimen(parentUnit, entity, parentId);
				break;
			default:
				throw new RuntimeException("Unimplemented entity type '"+type+"'");
			}
			
			List<Entity> children = getChildEntities(entity);
			for (Entity child : children) {
				traverse(child, parentUnit, cluster, parentUnitObj);
			}
		}
	}

	private ObservationUnit getObservationUnit(EntityDefinition defn) throws ImportException {
		ImportableModelObject imo = sourceIds.getModelObject(defn.getId());
		if ( imo == null || !(imo instanceof ObservationUnit) ) {
			throw new ImportException("Collect plot defn id "+defn.getId()+" does not correspond to valid calc observation unit");
		}
		return (ObservationUnit) imo;
	}

	private SurveyedCluster importCluster(Entity entity) throws InvalidMetadataException {
		SurveyedCluster cluster = new SurveyedCluster();
		cluster.setSurveyId(surveyId);
		cluster.setSourceId(entity.getId());
		cluster.setStep(step);
		List<Attribute<?,?>> children = getChildAttributes(entity);
		for (Attribute<?,?> child : children) {
			NodeDefinition defn = child.getDefinition();
			String calcAttr = defn.getAnnotation(ATTRIBUTE_QNAME);
			if ( CODE_ATTR.equals(calcAttr) ) {
				String code = getClusterCode(child);
				Integer id = samplingDesignIds.getClusterIds().getIdByCode(code);
				cluster.setClusterId(id);
			}
			// TODO import cluster.measurement into plot.surveyType 
			// TODO allow use cluster no instead of cluster code
		}
		surveyedClusterDao.insert(cluster);
		log.info("Inserted cluster "+cluster.getId());
		return cluster;
	}

	private SurveyedPlot importPlot(SurveyedCluster cluster, ObservationUnit unit, Entity entity, 
			SurveyedPlot parentPlot) throws InvalidMetadataException, ImportException {
		SurveyedPlot plot = new SurveyedPlot();
		Integer clusterId = cluster == null ? null : cluster.getId();
		plot.setParentId(clusterId);
		Integer parentId = parentPlot == null ? null : parentPlot.getId();
		plot.setParentId(parentId);
		plot.setSurveyId(surveyId);
		plot.setSourceId(entity.getId());
		plot.setObsUnitId(unit.getId());
		plot.setStep(step);
		List<Attribute<?,?>> children = getChildAttributes(entity);
		for (Attribute<?,?> child : children) {
			NodeDefinition defn = child.getDefinition();
			String calcAttr = defn.getAnnotation(ATTRIBUTE_QNAME);
			if ( NO_ATTR.equals(calcAttr) ) {
				Integer plotNo = getPlotNo(child);
				Integer plotId = samplingDesignIds.getPlotIds().getIdByNo(clusterId, plotNo);
				plot.setPlotId(plotId);
			} else if ( SECTION_ATTR.equals(calcAttr) ) {
				Integer sectionNo = getPlotSectionNo(child);
				plot.setSectionNo(sectionNo);
			} else if ( SURVEY_DATE_ATTR.equals(calcAttr) ) {
				Date surveyDate = getDateAttribute(child);
				plot.setSurveyDate(surveyDate);
			}
			// TODO import cluster.measurement into plot.surveyType 
			// TODO allow use cluster no instead of cluster code
		}
		if ( plot.getSectionNo() == null ) {
			plot.setSectionNo(1);
		}
		surveyedPlotDao.insert(plot);
		log.info("Inserted plot "+plot.getId());
		return plot;
	}

	private List<Entity> getChildEntities(Entity entity) {
		return getChildEntities(entity, new ArrayList<Entity>());
	}
	
	private List<Entity> getChildEntities(Entity entity, List<Entity> res) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child : children) {
			if ( child instanceof Entity ) {
				Entity childEntity = (Entity) child;
				EntityDefinition entityDefn = childEntity.getDefinition();
				if ( entityDefn.isMultiple() ) {
					res.add(childEntity);
				} else {
					getChildEntities(childEntity, res);
				}
			}
		}
		return res;
	}

	private List<Attribute<?,?>> getChildAttributes(Entity entity) {
		return getChildAttributes(entity, new ArrayList<Attribute<?,?>>());
	}
	
	private List<Attribute<?,?>> getChildAttributes(Entity entity, List<Attribute<?,?>> res) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child : children) {
			if ( child instanceof Entity ) {
				Entity childEntity = (Entity) child;
				EntityDefinition entityDefn = childEntity.getDefinition();
				if ( !entityDefn.isMultiple() ) {
					getChildAttributes(childEntity, res);
				}
			} else if ( child instanceof Attribute ){
				Attribute<?,?> attr = (Attribute<?, ?>) child;
				AttributeDefinition attrDefn = attr.getDefinition();
				if ( !attrDefn.isMultiple() ) {
					res.add(attr);
				}
			}
		}
		return res;
	}

	private Date getDateAttribute(Node<?> child) throws InvalidMetadataException {
		if ( child instanceof DateAttribute ) {
			org.openforis.idm.model.Date date = ((DateAttribute)child).getValue();
			return date == null ? null : date.toJavaDate();
		} else {
			throw new InvalidMetadataException("Unsupported date type "+child.getClass().getSimpleName());
		}
	}

	private Integer getPlotNo(Node<?> child) throws InvalidMetadataException, ImportException {
		if ( child instanceof NumberAttribute ) {
			Number value = ((NumberAttribute<?,?>) child).getValue().getValue();
			return value == null ? null : value.intValue();
		} else if ( child instanceof CodeAttribute ) {
				String code = ((CodeAttribute) child).getValue().getCode();
				if ( code == null ) {
					return null;
				} else {
					try {
						Integer value = Integer.valueOf(code);
						return value;
					} catch (NumberFormatException e) {
						throw new ImportException("Non-numeric plot code "+code);
					}
				}
		} else { 
			throw new InvalidMetadataException("Unsupported plot no type "+child.getClass().getSimpleName());
		}		
	}

	private Integer getPlotSectionNo(Node<?> child) throws InvalidMetadataException, ImportException {
		if ( child instanceof NumberAttribute ) {
			Number value = ((NumberAttribute<?,?>) child).getValue().getValue();
			return value == null ? null : value.intValue();
		} else if ( child instanceof TextAttribute ) {
			// convert A, B, C.. to 1, 2, 3..
				String value = ((TextAttribute) child).getValue().getValue();
				if ( value == null ) {
					return null;
				} else if ( value.length() == 1 ) {
					return value.charAt(0) - 64;
				} else {
					throw new ImportException("Invalid plot section code: "+value);
				}
		} else { 
			throw new InvalidMetadataException("Expected text or code attributes but got "+child.getClass());
		}		
	}

	private String getClusterCode(Node<?> child) throws InvalidMetadataException {
		if ( child instanceof CodeAttribute ) {
			return ((CodeAttribute) child).getValue().getCode();
		} else if ( child instanceof TextAttribute ) {
			return ((TextAttribute) child).getValue().getValue();
		} else { 
			throw new InvalidMetadataException("Expected text or code attributes but got "+child.getClass());
		}
	}

	private void logWarnings(String filename, ParseRecordResult result) {
		List<NodeUnmarshallingError> warns = result.getWarnings();
		if ( warns!=null && !warns.isEmpty() ) {
			log.warn(filename+" contains warnings:");
			logUnmarshallingErrors(warns);
		}
	}

	private void logFailures(String filename, ParseRecordResult result) {
		List<NodeUnmarshallingError> fails = result.getFailures();
		if ( fails!=null && !fails.isEmpty() ) {
			log.warn(filename+" contains errors:");
			logUnmarshallingErrors(fails);
		}
	}

	private void logUnmarshallingErrors(List<NodeUnmarshallingError> errors) {
		for (NodeUnmarshallingError error : errors) {
			log.warn(error.getPath()+": "+error.getMessage());
		}
	}
}
