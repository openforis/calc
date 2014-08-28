/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;

import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.ParameterMapConverter;
import org.openforis.calc.persistence.jooq.tables.pojos.ErrorSettingsBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class ErrorSettings extends ErrorSettingsBase {
	private static final String CATEGORIES = "categories";
	private static final String AOIS = "aois";

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Workspace workspace;
	
	public ErrorSettings() {
		super();
	}

	public ErrorSettings( String params ){
		ParameterMapConverter c = new ParameterMapConverter();
		ParameterMap parameters = c.from(params);
		setParameters( parameters );
	}

	public Workspace getWorkspace(){
		return workspace;
	}

	public void setWorkspace( Workspace workspace ){
		this.workspace = workspace;
		super.setWorkspaceId( workspace.getId().longValue() );
	}

	@Override
	public ParameterMap getParameters() {
		ParameterMap map = super.getParameters();
		if( map == null ){
			map = new ParameterHashMap();
			setParameters( map );
		}
		return map;
	}
	
	public boolean hasErrorSettings( long variableId ){
		return 	getErrorSettings( variableId ) != null &&
				getAois( variableId ).size() > 0 &&
				getCategoricalVariables( variableId ).size() > 0;				
	}
	
	@SuppressWarnings("unchecked")
	public Collection<? extends Number> getAois( long variableId ){
		ParameterMap map = getErrorSettings( variableId );
		Collection<? extends Object> array = map.getArray( AOIS );
		Collection<? extends Number> collection = null;
		if( array == null ){
			collection = new ArrayList<Number>();
		} else {
			collection = (Collection<? extends Number>) array;
		}
		return CollectionUtils.unmodifiableCollection( collection );
	}
	
	void setAois( long variableId , Collection<Long> aois ){
		ParameterMap errorSettings = getErrorSettings( variableId );
		errorSettings.setArray( AOIS , aois );
	}
	
	@SuppressWarnings("unchecked")
	public Collection<? extends Number> getCategoricalVariables( long variableId ){
		ParameterMap map = getErrorSettings( variableId );
		Collection<? extends Object> array = map.getArray( CATEGORIES );
		Collection<? extends Number> collection = null;
		if( array == null ){
			collection = new ArrayList<Number>();
		} else {
			collection = (Collection<? extends Number>) array;
		}
		return CollectionUtils.unmodifiableCollection( collection );
	}
	
	void setCategoricalVariables( long variableId , Collection<Long> categoricalVariables ){
		ParameterMap errorSettings = getErrorSettings( variableId );
		errorSettings.setArray( CATEGORIES , categoricalVariables );
	}
	
	void addErrorSettings( long variableId , Collection<? extends Number> aois , Collection<? extends Number> categoricalVariables ){
		ParameterHashMap map = new ParameterHashMap();
		map.setArray( AOIS, aois );
		map.setArray( CATEGORIES, categoricalVariables );
		
		getParameters().setMap( variableId+"" , map );
	}
	
	private ParameterMap getErrorSettings( long variableId ){
		ParameterMap parameters = this.getParameters();
		ParameterMap map = null;
		if( parameters != null ){
			map = parameters.getMap( variableId + "" );
		} else {
			map = new ParameterHashMap();
		}
		return map;
	}
	
}