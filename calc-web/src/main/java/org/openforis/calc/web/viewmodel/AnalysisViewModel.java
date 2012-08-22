/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import org.openforis.calc.bi.SchemaManager;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author Mino Togna
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AnalysisViewModel extends AbstractViewModel {

	@WireVariable
	private SchemaManager biSchemaManager;

	@Init
	public void init() {
		biSchemaManager.initSchema();
	}

}
