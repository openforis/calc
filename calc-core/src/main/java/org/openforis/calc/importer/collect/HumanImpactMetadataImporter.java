package org.openforis.calc.importer.collect;

import static org.openforis.calc.model.VariableType.BOOLEAN;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.VariableDao;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * 
 */
@Component
public class HumanImpactMetadataImporter {

	public static final String FALSE_CATEGORY_LABEL = "False";
	public static final String FALSE_CATEGORY_CODE = "F";
	public static final String TRUE_CATEGORY_LABEL = "True";
	public static final String TRUE_CATEGORY_CODE = "T";

	@Autowired
	private VariableDao variableDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CollectSurveyIdmlBinder idmlBinder;

	private org.openforis.idm.metamodel.Survey idmSurvey;

	private Log log = LogFactory.getLog(getClass());

	/**
	 */
	@Transactional
	synchronized 
	public void importMetadata(String surveyName, Reader reader) throws IOException, IdmlParseException {
		idmSurvey = (CollectSurvey) idmlBinder.unmarshal(reader);

		CodeList codeList = idmSurvey.getCodeList("human_impact");
		List<CodeListItem> items = codeList.getItems();
		for ( CodeListItem item : items ) {
			importBinaryCatVariable(2, item);
		}
	}

	private void importBinaryCatVariable(int obsUnitId, CodeListItem item) {
		Variable var = new Variable();

		String label = item.getLabels().get(0).getText();
		String code = item.getCode();
		String codeListLabel = item.getCodeList().getLabels().get(0).getText();
		String varName = (codeListLabel + " " + code).toLowerCase().replaceAll(" ", "_");

		var.setVariableName(varName);
		var.setObsUnitId(obsUnitId);
		var.setVariableLabel(label);
		var.setVariableTypeEnum(BOOLEAN);
		var.setForAnalysis(true);
		variableDao.insert(var);
		log.debug("Cat. variable: " + var.getVariableName() + " (" + var.getId() + ")");
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
		log.debug("Category: " + cat.getCategoryCode() + " (" + cat.getId() + ")");
	}

}
