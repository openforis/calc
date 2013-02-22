package org.openforis.calc.importer.collect;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.VariableDao;
import org.openforis.calc.service.MetadataService;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
// TODO call from MetadataService
public class IdmMetadataImporter {

	public static final String CALC_IDML_NAMESPACE = "http://www.openforis.org/calc/idml";
	public static final QName OBS_UNIT_QNAME = new QName(CALC_IDML_NAMESPACE, "observationUnit");
	public static final QName TYPE_QNAME = new QName(CALC_IDML_NAMESPACE, "type");
	public static final QName ATTRIBUTE_QNAME = new QName(CALC_IDML_NAMESPACE, "attribute");
	public static final String FALSE_CATEGORY_LABEL = "False";
	public static final String FALSE_CATEGORY_CODE = "F";
	public static final String TRUE_CATEGORY_LABEL = "True";
	public static final String TRUE_CATEGORY_CODE = "T";
	
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private ObservationUnitDao surveyUnitDao;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CollectSurveyIdmlBinder idmlBinder;

	private Survey survey;
	private org.openforis.idm.metamodel.Survey idmSurvey;
	
	private Log log = LogFactory.getLog(getClass());

	private String lang = "en";
	private String codeSeparator = "/";
	private String codeListItemLabelSeparator = " > ";
	// TODO add progress states item counts (plot types, specimen types, variables, etc)
	
	// TODO test - remove
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			IdmMetadataImporter loader = ctx.getBean(IdmMetadataImporter.class);
			loader.importMetadata("naforma1", "/home/gino/tzdata/idml.xml");
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Transactional
	synchronized
	public void importMetadata(String surveyName, String idmlFilename) throws IOException, IdmlParseException, InvalidMetadataException {		
		FileReader reader = new FileReader(idmlFilename);
		idmSurvey = (CollectSurvey) idmlBinder.unmarshal(reader);
		survey = new Survey();
		survey.setSurveyLabel(idmSurvey.getProjectName(lang));
		survey.setSurveyUri(idmSurvey.getUri());
		survey.setSurveyName(surveyName);
		survey.setId(2);
//		surveyDao.insert(survey);
		Schema schema = idmSurvey.getSchema();
		// TODO remove existing metadata
		log.debug("Importing survey " +survey.getSurveyName()+" ("+survey.getId()+")");
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			importMetadata(root, null);
		}
		log.info("Imported survey " +survey.getSurveyName()+" ("+survey.getId()+")");
	}

	/**
	 * Recursively imports all relevant entity metadata
	 * @param node
	 * @param parentUnit
	 * @throws InvalidMetadataException
	 */
	private void importMetadata(EntityDefinition node, ObservationUnit parentUnit) throws InvalidMetadataException {
		EntityType type = getEntityType(node);
		if ( type == null ) {
			if ( node.isMultiple() ) {
				log.debug("Skipping unmapped multiple entity: "+node.getPath());
				return;
			}
		} else {
			if ( type.isCluster() ) {
				if ( parentUnit != null ) {
					throw new InvalidMetadataException("'cluster' entity found inside observation unit '" +
							parentUnit.getObsUnitName() + "' instead of top level");
				}
				log.debug("Cluster entity found: "+node.getPath());
			} else if ( type.isObservationUnit() ){
				if ( type == EntityType.INTERVIEW ) 
					parentUnit = importObservationUnit(parentUnit, node, type);
			} else {
				throw new RuntimeException("Unimplemented entity type '"+type+"'");
			}
		}
		
		// Import metadata from all children
		for (NodeDefinition child : node.getChildDefinitions()) {
			if ( parentUnit != null && child instanceof AttributeDefinition ) {
				importVariable(parentUnit, (AttributeDefinition) child);
			} else if ( child instanceof EntityDefinition ){
				importMetadata((EntityDefinition) child, parentUnit);
			}
		}
	}

	private void importVariable(ObservationUnit parentUnit, AttributeDefinition attr) {
		if ( attr instanceof CodeAttributeDefinition && ((CodeAttributeDefinition) attr).getList().getLookupTable() == null ) {
			importCatVariable(parentUnit, (CodeAttributeDefinition) attr);
		} else if ( attr instanceof BooleanAttributeDefinition && !attr.isMultiple() ) {
			importBinaryCatVariable(parentUnit, (BooleanAttributeDefinition) attr);
		} else if ( attr instanceof NumberAttributeDefinition  && !attr.isMultiple() ) {
			importNumVariable(parentUnit, (NumberAttributeDefinition) attr);
		} else if ( attr instanceof CoordinateAttributeDefinition ) {
			log.debug("GPS location: "+attr.getName());
		} else {
			log.debug("Skipping unsupported attribute: "+attr.getPath());
		}
	}

	private void importNumVariable(ObservationUnit parentUnit, NumberAttributeDefinition attr) {
		Variable var = new Variable();
		var.setVariableName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setVariableLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setVariableType("ratio");
		var.setForAnalysis(true);
		variableDao.insert(var);
		log.debug("Num. variable: "+parentUnit.getObsUnitName()+"."+var.getVariableName()+" ("+var.getId()+")");
	}

	private void importBinaryCatVariable(ObservationUnit parentUnit, BooleanAttributeDefinition attr) {
		Variable var = new Variable();
		var.setVariableName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setVariableLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setVariableType("binary");
		var.setForAnalysis(true);
		variableDao.insert(var);
		log.debug("Cat. variable: " + parentUnit.getObsUnitName() + "." + var.getVariableName() + " (" + var.getId() + ")");
		importCategory(var, TRUE_CATEGORY_CODE, TRUE_CATEGORY_LABEL, 1);
		importCategory(var, FALSE_CATEGORY_CODE, FALSE_CATEGORY_LABEL, 2);
		
	}

	private void importCategory(Variable var, String code, String defaultLabel, int idx) {
		Category cat = new Category();
		cat.setVariableId(var.getId());
		cat.setCategoryCode(code);
		cat.setCategoryLabel(defaultLabel);
		cat.setCategoryOrder(idx);
		categoryDao.insert(cat);
		log.debug("Category: "+cat.getCategoryCode()+" ("+cat.getId()+")");		
	}

	private void importCatVariable(ObservationUnit parentUnit, CodeAttributeDefinition attr) {
		Variable var = new Variable();
		var.setVariableName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setVariableLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setVariableType(attr.isMultiple() ? "multiple" : "nominal");
		var.setForAnalysis(true);
		variableDao.insert(var);
		log.debug("Cat. variable: " + parentUnit.getObsUnitName() + "." + var.getVariableName() + " (" + var.getId() + ")");
		importCategories(var, attr);
	}

	private void importCategories(Variable var, CodeAttributeDefinition attr) {
		CodeList list = attr.getList();
		int level = attr.getCodeListLevel();
		List<CodeListItem> items = list.getItems(level);
		int idx = 1;
		for (CodeListItem item : items) {
			String code = getCode(item);
			String label = getCodeListItemLabel(item);
			importCategory(var, code, label, idx++);
		}
	}

	private String getCodeListItemLabel(CodeListItem item) {
		StringBuilder sb = new StringBuilder();
		CodeListItem ptr = item;
		while ( ptr != null ) {
			if ( sb.length() > 0 ) {
				sb.insert(0, codeListItemLabelSeparator);
			}
			String label = ptr.getLabel(lang);
			label = label == null ? ptr.getLabel("") : label;		
			sb.insert(0, label);
			ptr = ptr.getParentItem();
		}
		return sb.toString();

	}

	private String getCode(CodeListItem item) {
		CodeList list = item.getCodeList();
		if ( list.getHierarchy().isEmpty() || list.getCodeScope() == CodeScope.SCHEME ) {
			return item.getCode();
		} else {
			StringBuilder sb = new StringBuilder();
			CodeListItem ptr = item;
			while ( ptr != null ) {
				if ( sb.length() > 0 ) {
					sb.insert(0, codeSeparator);
				}
				sb.insert(0, ptr.getCode());
				ptr = ptr.getParentItem();
			}
			return sb.toString();
		}
	}

	private ObservationUnit importObservationUnit(ObservationUnit parentUnit, EntityDefinition node, EntityType type) {
		String name = node.getAnnotation(OBS_UNIT_QNAME);
		name = name == null ? node.getName() : name;
		ObservationUnit unit = new ObservationUnit();
		unit.setSurveyId(survey.getId());
		unit.setObsUnitName(name);
		unit.setObsUnitType(type.toString());
		if ( parentUnit != null ) {
			unit.setObsUnitParentId(parentUnit.getId());
		}
		surveyUnitDao.insert(unit);
		log.debug("Survey unit: " + unit.getObsUnitName() + " (" + unit.getId() + ")");
		return unit;
	}

	private EntityType getEntityType(EntityDefinition node) throws InvalidMetadataException {
		String type = node.getAnnotation(TYPE_QNAME);
		
		return type == null ? null : EntityType.get(type);
	}
}
