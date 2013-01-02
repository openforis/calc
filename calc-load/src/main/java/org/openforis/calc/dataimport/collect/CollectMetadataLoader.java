package org.openforis.calc.dataimport.collect;

import java.io.FileNotFoundException;
import java.util.List;

import org.openforis.calc.model.CategoricalVariable;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.NumericVariable;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
public class CollectMetadataLoader extends CollectLoaderBase {

	// TODO to partially refactor into MetadataService
	
	private String lang = "en";
	private String codeSeparator = "/";
	private String codeListItemLabelSeparator = " > ";
	// TODO add inserted item counts (plot types, specimen types, variables, etc)
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			CollectMetadataLoader loader = ctx.getBean(CollectMetadataLoader.class);
			loader.importMetadata(TEST_PATH+IDML_FILENAME);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Transactional
	synchronized
	public void importMetadata(String idmlFilename) throws FileNotFoundException, IdmlParseException, InvalidMetadataException {		
		collectSurvey = metadataService.loadIdml(idmlFilename);
		survey = new Survey();
		survey.setName(collectSurvey.getProjectName(lang));
		survey.setUri(collectSurvey.getUri());
//		survey.setId(1);
		surveyDao.insert(survey);
		log.info("Survey: " +survey.getName()+" ("+survey.getId()+")");
		Schema schema = collectSurvey.getSchema();
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
		if ( type != null ) {
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
		} else {
			log.info("Skipping unmapped entity: "+node.getPath());
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
		NumericVariable var = new NumericVariable();
		var.setName(attr.getName());
		var.setObsUnitId(parentUnit.getId());
		var.setSourceId(attr.getId());
		numericVariableDao.insert(var);
		log.info("Num. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
	}

	private void importCatVariable(ObservationUnit parentUnit, BooleanAttributeDefinition attr) {
		CategoricalVariable var = new CategoricalVariable();
		var.setName(attr.getName());
		var.setObsUnitId(parentUnit.getId());
		var.setSourceId(attr.getId());
		var.setMultipleResponse(false);
		var.setType("binomial");
		categoricalVariableDao.insert(var);
		log.info("Cat. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
		insertCategory(var, TRUE_CATEGORY_CODE, TRUE_CATEGORY_LABEL, 1);
		insertCategory(var, FALSE_CATEGORY_CODE, FALSE_CATEGORY_LABEL, 2);
		
	}

	private void insertCategory(CategoricalVariable var, String code, String defaultLabel, int idx) {
		Category cat = new Category();
		cat.setVariableId(var.getId());
		cat.setCode(code);
		cat.setName(defaultLabel);
		cat.setOrder(idx);
		categoryDao.insert(cat);
		log.info("Category: "+cat.getCode()+" ("+cat.getId()+")");		
	}

	private void importCatVariable(ObservationUnit parentUnit, CodeAttributeDefinition attr) {
		CategoricalVariable var = new CategoricalVariable();
		var.setName(attr.getName());
		var.setObsUnitId(parentUnit.getId());
		var.setSourceId(attr.getId());
		var.setType("nominal");
		var.setMultipleResponse(attr.isMultiple());
		categoricalVariableDao.insert(var);
		log.info("Cat. variable: "+parentUnit.getName()+"."+var.getName()+" ("+var.getId()+")");
		importCategories(var, attr);
	}

	private void importCategories(CategoricalVariable var, CodeAttributeDefinition attr) {
		CodeList list = attr.getList();
		int level = attr.getCodeListLevel();
		List<CodeListItem> items = list.getItems(level);
		int idx = 1;
		for (CodeListItem item : items) {
			importCategory(var, item, idx++);
		}
	}

	private void importCategory(CategoricalVariable var, CodeListItem item, int idx) {
		String code = getCode(item);
		String label = getCodeListItemLabel(item);
		Category cat = new Category();
		cat.setVariableId(var.getId());
		cat.setCode(code);
		cat.setName(label);
		cat.setOrder(idx);
		cat.setSourceId(item.getId());
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
		unit.setSourceId(node.getId());
		if ( parentUnit != null ) {
			unit.setParentId(parentUnit.getId());
		}
		surveyUnitDao.insert(unit);
		log.info("Survey unit: " +unit.getName()+" ("+unit.getId()+")");
		return unit;
	}
}
