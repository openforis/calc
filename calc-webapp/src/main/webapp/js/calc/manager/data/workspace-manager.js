/**
 * Manager for workspace and its metadata
 * @author Mino Togna
 */

WorkspaceManager = function() {
	
	this._activeWorkspace = null;

};

WorkspaceManager.prototype = (function(){
	
	/**
	 * Returns the active workspace.
	 * It gets loaded from the server if the instance variable is null
	 */
	var activeWorkspace = function(success) {
		var $this = this;
		if($this._activeWorkspace) {
			success( $this._activeWorkspace );
		} else {
			$.proxy(refreshActiveWorkspace, $this)( success );
		}
	};
	
	/**
	 * Loads the active workspace from the server and stores it as instance variable.
	 */
	var refreshActiveWorkspace = function(success) {
		var $this = this;
		UI.lock();
		$.ajax({
			url:"rest/workspace/active.json",
			dataType:"json"
		}).done(function(response) {
			var workspace = response.fields.workspace;
			$.proxy(setActiveWorkspace, $this)( workspace, success );
			UI.unlock();
		}).error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Changes the active workspace with the workspace id passed as argument
	 */
	var changeActiveWorkspace = function( workspaceId , success ) {
		var $this = this;
		$.ajax({
			url		: "rest/workspace/" + workspaceId + "/activate.json",
			dataType: "json",
			method 	: "GET"
		}).done( function(response) {
			Calc.workspaceChange( success );
		}).error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Set the sampling unit to the workspace
	 */
	var activeWorkspaceSetSamplingUnit = function(entity, success){
		var $this = this;
		$this.activeWorkspace(function(ws){
			var entityId = (entity) ? entity.id : -1;
			$.ajax({
				url:"rest/workspace/active/samplingDesign/samplingUnit/"+entityId+".json",
				dataType:"json",
				method:"POST"
			}).done(function(response){
				$.proxy(setActiveWorkspace, $this)( response, success );
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	/**
	 * Add a variable aggregate associated for the given variable of given entity
	 * TODO USE IDS not objects
	 */
	var activeWorkspaceCreateVariableAggregate = function(entity, variable, agg, success){
		$.proxy(activeWorkspaceUpdateVariableAggregate, this)(entity, variable, agg, "POST", success);
	};
	/**
	 * delete a variable aggregate associated for the given variable of given entity
	 */
	var activeWorkspaceDeleteVariableAggregate = function(entity, variable, agg, success){
		$.proxy(activeWorkspaceUpdateVariableAggregate, this)(entity, variable, agg, "DELETE", success);
	};
	/**
	 * private: add or delete a variable aggregate associated for the given variable of given entity
	 */
	var activeWorkspaceUpdateVariableAggregate = function(entity, variable, agg, method, success){
		var $this = this;
		$this.activeWorkspace(function(ws){
			
			$.ajax({
				url:"rest/workspace/active/entity/"+entity.id+"/variable/"+variable.id+"/aggregates/"+agg+".json",
				dataType:"json",
				method: method
			}).done(function(response){
				var variableToUpdate = response;
				// replace old qty variable with the new one
				variableToUpdate = entity.replaceVariable(variableToUpdate);
				success(variableToUpdate);
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Add a new Variable per Ha associated with given variable of given entity
	 */
	var activeWorkspaceAddVariablePerHa = function(entityId, variableId, success){
		$.proxy(activeWorkspaceUpdateVariablePerHa, this)(entityId, variableId, success, "POST");
	};
	
	/**
	 * Delete a variable per Ha associated with given variable of given entity
	 */
	var activeWorkspaceDeleteVariablePerHa = function(entityId, variableId, success){
		$.proxy(activeWorkspaceUpdateVariablePerHa, this)(entityId, variableId, success, "DELETE");
	};
	/**
	 * private. Delete or add variable per Ha associated with given variable of given entity
	 */
	var activeWorkspaceUpdateVariablePerHa = function(entityId, variableId, success, method) {
		var $this = this;
		
		$this.activeWorkspace(function(ws){
			var entity = ws.getEntityById(entityId);
			var variable = entity.getQuantitativeVariableById(variableId);
			
			$.ajax({
				url:"rest/workspace/active/entity/"+entityId+"/variable/"+variableId+"/variable-per-ha.json",
				dataType:"json",
				method: method
			}).done(function(response){
				
				$this.activeWorkspace(function(ws) {
					var variableToUpdate = response;
					// replace old qty variable with the new one
					variableToUpdate = entity.replaceVariable(variableToUpdate);
					success(variableToUpdate);
				});
				
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
		});
	};
	
	var activeWorkspaceAddQuantitativeVariable = function(variable, success, error, complete) {
		var $this = this;
		
		$.ajax({
			url:  "rest/workspace/active/entity/" + variable.entityId + "/variable/quantitative.json",
			dataType: "json",
			type: "POST",
			data: variable
		})
		.done(function(response) {
			if ( response.status == 'OK' ) {
				$this.activeWorkspace(function(ws) {
					var newVariable = response.fields.variable;
					var entity = ws.getEntityById(variable.entityId);
					entity.addQuantitativeVariable(newVariable);
				});
			}
			if ( success ) {
				success(response);
			}
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		})
		.complete(function() {
			if ( complete ) {
				complete();
			}
		});
	};
	
	/**
	 * Set for given entity the plot area
	 */
	var activeWorkspaceSetEntityPlotArea = function(entityId, plotArea, success){
		var $this = this;
		$this.activeWorkspace(function(ws){
			
			$.ajax({
				url : "rest/workspace/active/entity/"+entityId+"/plot-area.json",
				dataType : "json",
				method : "POST",
				data : { 'plot-area-script':plotArea }
			}).done(function(response){
				// update entity to workspace
				var entity = new Entity(ws, response);
				ws.updateEntity(entity);
				success(ws);
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * check if the active workspace is locked
	 */
	var activeWorkspaceIsLocked = function(complete) {
		var $this = this;
		$this.activeWorkspace(function(ws){
			
			$.ajax({
				url : "rest/workspace/active/locked.json",
				dataType : "json",
				method : "GET"
			}).done(function(response) {
				var locked = response.fields.locked;
				complete(locked);
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Import aois for the active workspace
	 */
	var activeWorkspaceImportAoi = function(filepath, captions, complete) {
		var $this = this;
		$this.activeWorkspace(function(ws){
			UI.lock();
			$.ajax({
				url : "rest/workspace/active/aoi/import.json",
				dataType : "json",
				method : "POST",
				data : { "filepath":filepath, "captions":captions.join(",") } 
			}).done(function(response) {
				var aoiHierarchy = response;
				ws.aoiHierarchies[0] = response;
				complete(ws);
				UI.unlock();
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Import strata for the active workspace
	 */
	var activeWorkspaceImportStrata = function(filepath, complete) {
		var $this = this;
		$this.activeWorkspace(function(ws){
			UI.lock();
			$.ajax({
				url : "rest/workspace/active/strata/import.json",
				dataType : "json",
				method : "POST",
				data : { "filepath":filepath } 
			}).done(function(response) {
				var strata = response;
				ws.strata = strata;
				complete(ws);
				UI.unlock();
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Set the phase1 plot table to the active workspace
	 */
	var activeWorkspaceSetPhase1PlotsTable = function(table, success){
		var $this = this;
		$this.activeWorkspace( function(ws){

			$.ajax({
				url : "rest/workspace/active/phase1plotstable.json",
				dataType : "json",
				method : "POST",
				data : { "table":table } 
			}).done(function(response) {
//				var ws = response;
				$.proxy(setActiveWorkspace, $this)( response, success );
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Set the sampling desing for the active workspace
	 * the sampling desing param must be a json object
	 */
	var activeWorkspaceSetSamplingDesign = function(samplingDesign, success){
		var $this = this;
		UI.lock();
		$this.activeWorkspace(function(ws){
			
			var params = { "samplingDesign" : JSON.stringify(samplingDesign) };
			$.ajax({
				url : "rest/workspace/active/samplingDesign.json",
				dataType : "json",
				method : "POST",
				data : params
			}).done(function(response) {
				var sd = response.fields.samplingDesign;
				ws.samplingDesign = sd;
				success( ws );
				UI.unlock();
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};
	
	/**
	 * Import an equation list previously uploaded as csv file
	 */
	var activeWorkspaceImportEquationList = function( filePath , listName , success) {
		var $this = this;
		UI.lock();
		$this.activeWorkspace(function(ws){
			
			var data = { "filePath":filePath , "listName":listName };
			$.ajax({
				url : "rest/workspace/active/settings/equationList.json",
				dataType : "json",
				method : "POST",
				data : data
			}).done( function(response) {
				var lists = response.fields.equationLists;
				ws.equationLists = lists;
				success( ws );
				UI.unlock();
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
			
		});
	};

	/**
	 * Private function to
	 * Set the active workspace and calls the callback function if present
	 */
	var setActiveWorkspace = function( data, callback ) {
		var $this = this;
		$this._activeWorkspace = data ? new Workspace( data ) : null;
		if( callback ) {
			callback($this._activeWorkspace);
		}
	};
	
	return {
		constructor : WorkspaceManager
		,
		activeWorkspace : activeWorkspace
		,
		activeWorkspaceSetSamplingUnit : activeWorkspaceSetSamplingUnit
		,
		activeWorkspaceCreateVariableAggregate : activeWorkspaceCreateVariableAggregate
		,
		activeWorkspaceDeleteVariableAggregate : activeWorkspaceDeleteVariableAggregate
		,
		activeWorkspaceAddVariablePerHa : activeWorkspaceAddVariablePerHa
		,
		activeWorkspaceDeleteVariablePerHa : activeWorkspaceDeleteVariablePerHa
		,
		activeWorkspaceAddQuantitativeVariable : activeWorkspaceAddQuantitativeVariable
		,
		activeWorkspaceSetEntityPlotArea : activeWorkspaceSetEntityPlotArea
		,
		activeWorkspaceIsLocked : activeWorkspaceIsLocked
		,
		activeWorkspaceImportAoi : activeWorkspaceImportAoi
		,
		activeWorkspaceImportStrata : activeWorkspaceImportStrata
		,
		activeWorkspaceSetPhase1PlotsTable : activeWorkspaceSetPhase1PlotsTable
		,
		activeWorkspaceSetSamplingDesign : activeWorkspaceSetSamplingDesign
		,
		refreshActiveWorkspace : refreshActiveWorkspace
		,
		activeWorkspaceImportEquationList : activeWorkspaceImportEquationList
		,
		changeActiveWorkspace : changeActiveWorkspace
	};
	
})();

// singleton instance of workspace manager
var _workspaceManager = null;
WorkspaceManager.getInstance = function() { 
	if(!_workspaceManager){
		_workspaceManager = new WorkspaceManager();
	}
	return _workspaceManager;
};

