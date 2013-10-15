package test;

//import java.util.List;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author M. Togna
 *
 */
//@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class PerformanceDaoTest
{
//extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private CalculationEngine calculationEngine;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private TaskManager taskManager;

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private DataSource userDataSource;
	
	@Autowired
	private R r;
	
//	@Test
	public void testSelect() {
		
		int limit = 5000;
		
		Connection c = null;
		Statement s = null;
		PreparedStatement ps = null;
		
		
		
		try {
			long start = System.currentTimeMillis();
			System.out.println(Math.ceil(44.23));
			
			REnvironment rEnvironment = r.newEnvironment();
			String eq = "0.5 * pi * (0.01 * $a$ / 2)^2";
			
			
			c = userDataSource.getConnection();
			s = c.createStatement();
						
			// get number of rows		
			String sql = "select count(*) from naforma1_out.tree";
			ResultSet rs = s.executeQuery(sql );
			rs.next();
			int rows = rs.getInt(1);
			//System.out.println( "========== Size: "  + i);
			rs.close();
			
			//number of iterations
			long iterations = Math.round( Math.ceil( limit / rows ) );
			
			for( int i = 0 ; i < iterations; i++) {
				
			}
			sql = "select _tree_id, total_height, dbh from naforma1_out.tree ";
			sql+=" limit 5000 offset 0;";
			
			
			rs = s.executeQuery(sql );
			ps = c.prepareStatement("update naforma1_out.tree set test = ? where _tree_id = ?");
			
			while( rs.next() ){
				int treeId = rs.getInt("_tree_id");
				BigDecimal height = rs.getBigDecimal("total_height");
				BigDecimal dbh = rs.getBigDecimal("dbh");
				
//				System.out.println(eq + " dbh: " + dbh);
				String f = eq.replaceFirst( "\\$a\\$", String.valueOf(dbh) );
//				System.out.println(treeId + "  e:" + f);
				//1145 ms w/o update
				double d = rEnvironment.evalDouble(f);

				
				//9293 ms w update (w/o batch
				
				ps.setBigDecimal(1, BigDecimal.valueOf(d));
				ps.setInt(2, treeId);
				ps.addBatch();
//				System.out.println(ps.toString());
				//System.out.println( d );
//				System.out.println(rs.getRow());
//				System.out.println(treeId + " h:" + height);
			}
			
			ps.executeBatch();
			System.out.println("============= Finished in " + (System.currentTimeMillis() - start) );
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(s!=null)
				try {
					s.close();
				} catch (SQLException e) {}
			if(c!=null)
				try {
					c.close();
				} catch (SQLException e) {}
			if(ps!=null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}
	
}
