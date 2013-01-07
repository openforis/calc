package org.openforis.calc.dataimport.collect;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.Variable;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class IdmMetadataLoader extends IdmLoaderBase {

	// TODO to partially refactor into MetadataService?
	private static final String TEST_SURVEY_NAME = "naforma1";

	private String lang = "en";
	private String codeSeparator = "/";
	private String codeListItemLabelSeparator = " > ";
	// TODO add inserted item counts (plot types, specimen types, variables, etc)
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			IdmMetadataLoader loader = ctx.getBean(IdmMetadataLoader.class);
			loader.importMetadata(TEST_SURVEY_NAME, TEST_PATH+IDML_FILENAME);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Transactional
	synchronized
	public void importMetadata(String surveyName, String idmlFilename) throws IOException, IdmlParseException, InvalidMetadataException {		
		idmSurvey = metadataService.loadIdml(idmlFilename);
		survey = new Survey();
		survey.setDefaultLabel(idmSurvey.getProjectName(lang));
		survey.setUri(idmSurvey.getUri());
		survey.setName(surveyName);
//		survey.setId(1);
		surveyDao.insert(survey);
		log.info("Survey: " +survey.getDefaultLabel()+" ("+survey.getId()+")");
		Schema schema = idmSurvey.getSchema();
		traverse(schema);
	}

	private void traverse(Schema schema) throws InvalidMetadataException {
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			traverse(root, null);
		}
	}

	private void traverse(EntityDefinition node, ObservationUnit parentUnit) throws InvalidMetadataException {
		EntityType type = getEntityType(node);
		if ( type == null ) {
			if ( node.isMultiple() ) {
				log.info("Skipping unmapped multiple entity: "+node.getPath());
				return;
			}
		} else {
			if ( type.isCluster() ) {
				if ( parentUnit != null ) {
					throw new InvalidMetadataException("'cluster' entity found inside observation unit '" +
							parentUnit.getName()+"' instead of top level");
				}
				log.info("Cluster entity found: "+node.getPath());
			} else if ( type.isObservationUnit() ){
				parentUnit = importObservationUnit(parentUnit, node, type);
			} else {
				throw new RuntimeException("Unimplemented entity type '"+type+"'");
			}
		}
		
		for (NodeDefinition child : node.getChildDefinitions()) {
			if ( parentUnit != null && child instanceof AttributeDefinition ) {
				importVariable(parentUnit, (AttributeDefinition) child);
			} else if ( child instanceof EntityDefinition ){
				traverse((EntityDefinition) child, parentUnit);
			}
		}
	}

	private void importVariable(ObservationUnit parentUnit, AttributeDefinition attr) {
		if ( attr instanceof CodeAttributeDefinition && ((CodeAttributeDefinition) attr).getList().getLookupTable() == null ) {
			importCatVariable(parentUnit, (CodeAttributeDefinition) attr);
		} else if ( attr instanceof BooleanAttributeDefinition && !attr.isMultiple() ) {
			importCatVariable(parentUnit, (BooleanAttributeDefinition) attr);
		} else if ( attr instanceof NumberAttributeDefinition  && !attr.isMultiple() ) {
			importNumVariable(parentUnit, (NumberAttributeDefinition) attr);
		} else if ( attr instanceof CoordinateAttributeDefinition ) {
			log.info("GPS location: "+attr.getName());
		} else {
			log.info("Skipping unsupported attribute: "+attr.getPath());
		}
	}

	private void importNumVariable(ObservationUnit parentUnit, NumberAttributeDefinition attr) {
		Variable var = new Variable();
		var.setName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setDefaultLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setType("ratio");
		variableDao.insert(var);
		log.info("Num. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
	}

	private void importCatVariable(ObservationUnit parentUnit, BooleanAttributeDefinition attr) {
		Variable var = new Variable();
		var.setName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setDefaultLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setType("binary");
		variableDao.insert(var);
		log.info("Cat. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
		insertCategory(var, TRUE_CATEGORY_CODE, TRUE_CATEGORY_LABEL, 1);
		insertCategory(var, FALSE_CATEGORY_CODE, FALSE_CATEGORY_LABEL, 2);
		
	}

	private void insertCategory(Variable var, String code, String defaultLabel, int idx) {
		Category cat = new Category();
		cat.setVariableId(var.getId());
		cat.setCode(code);
		cat.setDefaultLabel(defaultLabel);
		cat.setOrder(idx);
		categoryDao.insert(cat);
		log.info("Category: "+cat.getCode()+" ("+cat.getId()+")");		
	}

	private void importCatVariable(ObservationUnit parentUnit, CodeAttributeDefinition attr) {
		Variable var = new Variable();
		var.setName(attr.getCompoundName());
		var.setObsUnitId(parentUnit.getId());
		var.setDefaultLabel(attr.getLabel(Type.INSTANCE, lang));
		var.setType(attr.isMultiple() ? "multiple" : "nominal");
		variableDao.insert(var);
		log.info("Cat. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
		importCategories(var, attr);
	}

	private void importCategories(Variable var, CodeAttributeDefinition attr) {
		CodeList list = attr.getList();
		int level = attr.getCodeListLevel();
		List<CodeListItem> items = list.getItems(level);
		int idx = 1;
		for (CodeListItem item : items) {
			importCategory(var, item, idx++);
		}
	}

	private void importCategory(Variable var, CodeListItem item, int idx) {
		String code = getCode(item);
		String label = getCodeListItemLabel(item);
		Category cat = new Category();
		cat.setVariableId(var.getId());
		cat.setCode(code);
		cat.setDefaultLabel(label);
		cat.setOrder(idx);
		categoryDao.insert(cat);
		log.info("Category: "+cat.getCode()+" ("+cat.getId()+")");
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
		unit.setName(name);
		unit.setType(type.toString());
		if ( parentUnit != null ) {
			unit.setParentId(parentUnit.getId());
		}
		surveyUnitDao.insert(unit);
		log.info("Survey unit: " +unit.getName()+" ("+unit.getId()+")");
		return unit;
	}
}
