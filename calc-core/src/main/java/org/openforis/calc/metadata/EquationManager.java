/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.EquationDao;
import org.openforis.calc.persistence.jooq.tables.daos.EquationListDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.R;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Component
public class EquationManager {
	
	@Autowired
	private EquationListDao equationListDao;
	
	@Autowired
	private EquationDao equationDao;
	
	@Autowired
	private Psql psql;
	
	@Autowired
	private R r;

	private static Pattern VARIABLE_PATTERN = Pattern.compile( "\\b([A-Za-z]+?)\\b" );;
	
	public Set<String> extractVariableNames( String filePath ) throws IOException {
		Set<String> variables = new HashSet<String>();
		
		CsvReader csvReader = new CsvReader(filePath);
		csvReader.readHeaders();
//		int i = 0;
		for ( FlatRecord record = csvReader.nextRecord(); record != null; record = csvReader.nextRecord() ) {
//			String code = record.getValue( 0, String.class );
			String equation = record.getValue( 1, String.class );
//			System.out.println(" === Iter : " + (i++) + ".  Equation : " + equation);
			Matcher matcher = VARIABLE_PATTERN.matcher( equation );
			while( matcher.find() ) {
				String var = matcher.group( 1 );
				if( !r.getBaseFunctions().contains( var ) ) {
					variables.add( var );
				}
			}
		}
		
		csvReader.close();
		
		return variables;
	}
	
	public static void main(String[] args) throws IOException {
		EquationManager m = new EquationManager();
		m.extractVariableNames("/openforis/test-data/src/main/resources/laputa/calc-volume-models.csv");
	}
	
	@Transactional
	public void importFromCsv( Workspace workspace , String filePath, String listName ) throws IOException {
		
		// delete equation list
		deleteListByName( workspace , listName );
		
		// create equation list
		EquationList list = new EquationList();
		list.setName( listName );
		list.setId( psql.nextval( Sequences.EQUATION_LIST_ID_SEQ ) );
		workspace.addEquationList( list );
		
		CsvReader csvReader = new CsvReader(filePath);
		csvReader.readHeaders();
		
		for ( FlatRecord record = csvReader.nextRecord(); record != null; record = csvReader.nextRecord() ) {
			System.out.println( record );
		}
		
		csvReader.close();
	}
	
	@Transactional
	private void deleteListByName( Workspace workspace, String listName ) {
		List<EquationList> equationLists = workspace.getEquationLists();
		for (EquationList equationList : equationLists) {
			
			if( equationList.getName().equals(listName) ) {
				workspace.deleteEquationList( equationList );
				equationListDao.deleteById( equationList.getId() );
				break;
			}
			
		}
	}

	@Transactional
	public void loadListsByWorkspace( Workspace workspace ) {
		
		List<EquationList> list = equationListDao.fetchByWorkspaceId( workspace.getId() );
		
		for (EquationList eqList : list) {
			// load equations
			List<Equation> equations = psql
				.select()
				.from( Tables.EQUATION )
				.where( Tables.EQUATION.LIST_ID.eq(eqList.getId()) )
				.fetchInto( Equation.class );
			
			eqList.setEquations( equations );
			
		}
		// add equation list to workspace
		workspace.setEquationLists( list );
		
	}
}
