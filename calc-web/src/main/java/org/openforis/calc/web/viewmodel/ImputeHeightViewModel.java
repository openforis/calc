/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openforis.calc.operation.OperationException;
import org.openforis.calc.operation.TreeHeightImputation;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * @author Mino Togna
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
@Deprecated
public class ImputeHeightViewModel {

//	private List<Tree> trees;
//	private static String dataFileName = "trees_cleaned_reduced.txt";
//	
//	@NotifyChange("trees")
//	@Command("calc")
//	public void calcHeights() throws OperationException {
//		long time = System.currentTimeMillis();
//		System.out.println("calculate heights.. :)");
//
//		// Window window = (Window) Executions.createComponents("/view/progressBar.zul",null,null);
//		// window.setClosable(false);
//		// window.doModal();
//
//		// try {
//		// Thread.sleep(2000);
//		// } catch ( InterruptedException e ) {
//		// }
//		// window.setClosable(true);
//		// window.detach();
//
//		TreeHeightImputation op = getOperation(trees);
//		op.evaluate();
//		List<Double> heightsPredicted = op.getTreeHeightsPredicted();
//		int i = 0;
//		for ( Double d : heightsPredicted ) {
//			Tree tree = trees.get(i++);
//			tree.setHeightPredicted(d);
//		}
//
//		time = System.currentTimeMillis() - time;
//		long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
//		long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);
//		String string = String.format("%d min, %d sec", minutes, seconds);
//		System.err.println("Calculation complited in " + string);
//	}
//
////	@Command
////	@NotifyChange("trees")
////	public void revertMail() {
////		//getMailData().revertDeletedMails();
////	}
//
//	private TreeHeightImputation getOperation(List<Tree> trees) {
//		TreeHeightImputation op = new TreeHeightImputation();
//		List<Double> dbhs = new ArrayList<Double>();
//		List<Double> heights = new ArrayList<Double>();
//		List<String> ids = new ArrayList<String>();
//		for ( Tree tree : trees ) {
//			dbhs.add(tree.getDbh());
//			heights.add(tree.getHeight());
//			ids.add(tree.getId());
//		}
//		op.setTreeDbhs(dbhs);
//		op.setTreeHeights(heights);
//		op.setTreeIds(ids);
//
//		return op;
//	}
//
//	@Init
//	public void initTrees() {
//
//		trees = new ArrayList<Tree>();
//
//		InputStream source = ImputeHeightViewModel.class.getClassLoader().getResourceAsStream(dataFileName);
//		Scanner scanner = new Scanner(source);
//		int i = 0;
//		while ( scanner.hasNextLine() ) {
//			String line = scanner.nextLine();
//			if ( i++ > 0 && StringUtils.isNotBlank(line) ) {
//				String[] values = line.split(" ");
//
//				String healtId = values[10];
//				if ( !"7".equals(healtId) ) {
//					double dbh = Double.parseDouble(values[9]);
//					double totalHeight = Double.parseDouble(values[13]);
//
//					int clux = Integer.parseInt(values[0]);
//					int cluy = Integer.parseInt(values[0]);
//					String cId = (1000 * clux + cluy) + "";
//
//					Tree tree = new Tree(cId, dbh, totalHeight);
//					trees.add(tree);
//				}
//			}
//		}
//	}
//
//	public List<Tree> getTrees() {
//		return trees;
//	}

}
