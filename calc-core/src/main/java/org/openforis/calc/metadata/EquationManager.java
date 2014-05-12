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

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
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

	private static Pattern VARIABLE_PATTERN = Pattern.compile( "([A-Za-z_]+[A-Za-z_.]*)" );
	
	@Transactional
	public void createFromCsv( Workspace workspace , String filePath, String listName ) throws IOException {
		// create equation list
		EquationList equationList = new EquationList();
		equationList.setId( psql.nextval( Sequences.EQUATION_LIST_ID_SEQ ) );
		equationList.setName( listName );
		workspace.addEquationList( equationList );
		
		importCsv(equationList, filePath);
		
	}
	
	@Transactional
	public void updateFromCsv( Workspace workspace , String filePath, String listName , long listId ) throws IOException {
		EquationList equationList = workspace.getEquationListById(listId);
		equationList.setName( listName );
		workspace.addEquationList( equationList );
		
		importCsv(equationList, filePath);
	}
	
	@Transactional
	private void importCsv( EquationList equationList , String filePath) throws IOException {
		// delete equations
		deleteEquations( equationList );
		
		CsvReader csvReader = new CsvReader(filePath);
		csvReader.readHeaders();
		
		Set<String> variables = new HashSet<String>();
		for ( FlatRecord record = csvReader.nextRecord(); record != null; record = csvReader.nextRecord() ) {
			String code = record.getValue( 0, String.class );
			String equationString = record.getValue( 1, String.class );
			String condition = record.getValue( 2, String.class );
			
			Equation equation = new Equation();
			equation.setId( psql.nextval(Sequences.EQUATION_ID_SEQ) );
			equation.setCode(code);
			equation.setEquation(equationString);
			equation.setCondition(condition);
			
			equationList.addEquation(equation);
			
			variables.addAll( extractVariables(condition) );
			variables.addAll( extractVariables(equationString) );
		}
		
		ParameterMap parameters = new ParameterHashMap();
		parameters.setArray( "variables" , variables );
		equationList.setParameters(parameters);
		
		
		if( equationListDao.exists(equationList) ) {
			equationListDao.update( equationList );
		} else {
			equationListDao.insert( equationList );
		}
		equationDao.insert( equationList.getEquations() );
		
		csvReader.close();
	}
	
	@Transactional
	private void deleteEquations( EquationList equationList ) {
		equationDao.delete( equationList.getEquations() );
		equationList.setEquations( null );
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

	public boolean isNameUnique( String listName , Long listId ) {
		SelectQuery<Record> select = psql.selectQuery();
		select.addFrom( Tables.EQUATION_LIST );
		select.addConditions( Tables.EQUATION_LIST.NAME.eq(listName) );
		if( listId != null ) {
			select.addConditions( Tables.EQUATION_LIST.ID.notEqual( listId ) );	
		}
		
		Result<Record> result = select.fetch();
		return result.size() <= 0;
	}
	
	private Set<String> extractVariables( String equation ) throws IOException {
		Set<String> variables = new HashSet<String>();

		if( StringUtils.isNotBlank(equation) ) {
			Matcher matcher = VARIABLE_PATTERN.matcher( equation );
			while( matcher.find() ) {
				String var = matcher.group( 1 );
				if( !r.getBaseFunctions().contains( var ) ) {
					variables.add( var );
				}
			}
		}
		
		return variables;
	}

}
