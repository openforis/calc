/**
 * 
 */
package org.openforis.calc.operation;

import java.math.BigDecimal;
import java.util.List;

import org.openforis.calc.model.TreeObsView;
import org.openforis.calc.persistence.dao.TreeObsDAO;
import org.renjin.eval.Context;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ExpressionVector;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mino Togna
 *
 */
public class TreeVolumeCalculation extends AbstractROperation {
	
	@Autowired
	private TreeObsDAO treeObsDao;
	
	public void evaluate() throws OperationException {
		RenjinScriptEngine engine = getRenjinScriptEngine();
		Context context = engine.getTopLevelContext();
		ExpressionVector expressionVector = getExpressionVector();
		
		List<TreeObsView> trees = treeObsDao.getAllFromView();
		for ( TreeObsView treeView : trees ) {
			BigDecimal estTopHeight = treeView.getEstTopHeight();
			BigDecimal dbh = treeView.getDbh();
			
			engine.put("h", estTopHeight.doubleValue());
			engine.put("d", dbh.doubleValue());
			engine.put("ff", 0.5);
			
			context.evaluate(expressionVector, context.getEnvironment());
			DoubleVector res = (DoubleVector) engine.get("volpred");
			BigDecimal volPred = new BigDecimal(res.asReal());
			treeView.setEstVolume(volPred);
		}
		
		treeObsDao.batchUpdate(trees, "estVolume");
	}

}
