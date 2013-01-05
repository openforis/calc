package org.openforis.calc.dataimport.collect;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.VariableDao;
import org.openforis.calc.service.MetadataService;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class CollectLoaderBase {
	
	// test - remove
	protected static final String TEST_PATH = "/home/gino/workspace/tzdata/";
	
	protected static final String IDML_FILENAME = "idml.xml";

	public static final String CALC_IDML_NAMESPACE = "http://www.openforis.org/calc/idml";
	public static final QName OBS_UNIT_QNAME = new QName(CALC_IDML_NAMESPACE, "observationUnit");
	public static final QName TYPE_QNAME = new QName(CALC_IDML_NAMESPACE, "type");
	public static final QName ATTRIBUTE_QNAME = new QName(CALC_IDML_NAMESPACE, "attribute");
	public static final String FALSE_CATEGORY_LABEL = "False";
	public static final String FALSE_CATEGORY_CODE = "F";
	public static final String TRUE_CATEGORY_LABEL = "True";
	public static final String TRUE_CATEGORY_CODE = "T";
	
	@Autowired
	protected MetadataService metadataService;
	@Autowired
	protected SurveyDao surveyDao;
	@Autowired
	protected ObservationUnitDao surveyUnitDao;
	@Autowired
	protected VariableDao variableDao;
	@Autowired
	protected CategoryDao categoryDao;
	
	protected Survey survey;
	protected  CollectSurvey collectSurvey;
	
	protected Log log = LogFactory.getLog(getClass());
	

	public static EntityType getEntityType(EntityDefinition node) throws InvalidMetadataException {
		String type = node.getAnnotation(TYPE_QNAME);
		
		return type == null ? null : EntityType.get(type);
	}
	
}