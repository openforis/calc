/**
 * 
 */
package org.openforis.calc.operation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.model.TreeObsView;
import org.openforis.calc.persistence.dao.TreeObsDAO;
import org.openforis.calc.r.RClient;
import org.openforis.calc.r.RUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class TreeHeightImputation extends AbstractROperation {

	// private static Log LOG = LogFactory.getLog(TreeHeightImputation.class);

	@Autowired
	private TreeObsDAO treeObsDao;

	public void evaluate() throws OperationException {
		// checkParameters();
		List<TreeObsView> trees = treeObsDao.getAllFromView();
		double[] heightPred;
		String[] ids;
		try {
			RClient client = RClient.getInstance();
			client.connect();

			REXP dataFrame = getDataFrame(trees);
			client.assign("data", dataFrame);

			String script = getScript();
			client.voidEval(script);

			REXP resultData = client.get("data");
			RList resultList = resultData.asList();

			REXPDouble heightPredRes = (REXPDouble) resultList.at("hpred");
			heightPred = heightPredRes.asDoubles();

			REXPString idsRes = (REXPString) resultList.at("id");
			ids = idsRes.asStrings();
			client.disconnect();
		} catch ( Exception e ) {
			throw new RuntimeException("Error while evaluating the tree height imputation", e);
		}

		// Saving results to db
		for ( int i = 0 ; i < heightPred.length ; i++ ) {
			double h = heightPred[i];
			int id = Integer.parseInt(ids[i]);

			TreeObsView treeView = trees.get(i);
			if ( !treeView.getId().equals(id) ) {
				throw new RuntimeException("Source id is not the same as the result");
			}
			treeView.setEstTopHeight(new BigDecimal(h));
		}
		treeObsDao.batchUpdate(trees, "estTopHeight");
	}

	private REXP getDataFrame(List<TreeObsView> trees) {

		List<Double> treeHeights = new ArrayList<Double>();
		List<Double> treeDbhs = new ArrayList<Double>();
		List<String> treeIds = new ArrayList<String>();
		List<String> clusterIds = new ArrayList<String>();

		for ( TreeObsView treeObs : trees ) {
			Double h = treeObs.getTopHeight() != null ? treeObs.getTopHeight().doubleValue() : Double.valueOf(-1);
			Double dbh = treeObs.getDbh().doubleValue();
			String id = treeObs.getId() + "";
			String cluster_id = treeObs.getClusterCode();

			treeHeights.add(h);
			treeDbhs.add(dbh);
			treeIds.add(id);
			clusterIds.add(cluster_id);
		}

		REXP[] rexps = new REXP[] { RUtils.toVector(treeIds.toArray(new String[] {})), RUtils.toVector(treeHeights), RUtils.toVector(treeDbhs), RUtils.toVector(clusterIds.toArray(new String[] {})) };
		RList rList = new RList(rexps, new String[] { "id", "totalh", "dbh", "cluster_id" });
		REXP dataFrame = null;
		try {
			dataFrame = REXP.createDataFrame(rList);
		} catch ( REXPMismatchException e ) {
			throw new RuntimeException("Error while creating data frame", e);
		}
		return dataFrame;
	}

}
