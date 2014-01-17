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
		$this = this;
		if($this._activeWorkspace) {
			success($this._activeWorkspace);
		} else {
			$.ajax({
				url:"rest/workspace/active.json",
				dataType:"json"
			}).done(function(response) {
				$.proxy(setActiveWorkspace, $this)( response, success );
			});
		}
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
		.error(function(e) {
			if ( error ) {
				error(e);
			}
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
			});
			
		});
	};
	/**
	 * Private function to
	 * Set the active workspace and calls the callback function if present
	 */
	var setActiveWorkspace = function(data, callback) {
		var $this = this;
		$this._activeWorkspace = new Workspace( data );
		if(callback) {
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

