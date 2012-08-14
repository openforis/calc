package test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Column;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.openforis.calc.model.PlotObs;
import org.openforis.calc.model.TreeObsView;
import org.openforis.calc.operation.OperationException;
import org.openforis.calc.operation.TreeHeightImputation;
import org.openforis.calc.operation.TreeVolumeCalculation;
import org.openforis.calc.operation.UpdatePlotArea;
import org.openforis.calc.persistence.dao.PlotObsDAO;
import org.openforis.calc.r.RClient;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Test {

	public static void main(String[] args) throws OperationException, ExecuteException, IOException, InterruptedException, RserveException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "applicationContext-persistence.xml","applicationContext.xml" });
//		UpdatePlotArea u = (UpdatePlotArea) context.getBean("updatePlotArea");
//		long s = System.currentTimeMillis();
//		u.evaluate();
//		long e = System.currentTimeMillis() - s;
//		System.out.println(e);
		
		
//		s = System.currentTimeMillis();
//		u.evaluate();
//		e = System.currentTimeMillis() - s;
//		System.out.println(e);
	
//		PlotObsDAO d = (PlotObsDAO) context.getBean("plotObsDao");
//		List<PlotObs> plots = d.getAll();
//		System.out.println("plots: "+plots.size());
		
//		TreeHeightImputation t = (TreeHeightImputation) context.getBean("treeHeightImputation");
//		t.evaluate();
		
		TreeVolumeCalculation c =  (TreeVolumeCalculation) context.getBean("treeVolumeCalculation");
		c.evaluate();
		
		
//		String cmd = "/home/minotogna/dev/projects/openforis/calc/setup/scripts/start_Rserve.sh";
//		CommandLine cmdLine = CommandLine.parse(cmd);
//		DefaultExecutor executor = new DefaultExecutor();
//		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//		executor.execute(cmdLine, resultHandler);
//		resultHandler.waitFor();
//		System.out.println(execute);
		
		
//		RClient rClient = RClient.getInstance();
//		rClient.connect();
//		System.out.println("================ is connected" + rClient.isConnected());
		
		System.exit(0);
	}
	
}
