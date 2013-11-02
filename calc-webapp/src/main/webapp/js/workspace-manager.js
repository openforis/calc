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
			
			$.ajax({
				url:"rest/workspace/active/samplingDesign/samplingUnit/"+entity.id+".json",
				dataType:"json",
				method:"POST"
			}).done(function(response){
				$.proxy(setActiveWorkspace, $this)( response, success );
			});
			
		});
	};
	
	var activeWorkspaceCreateVariableAggregate = function(entity, variable, agg, success){
		$.proxy(activeWorkspaceUpdateVariableAggregate, this)(entity, variable, agg, "POST", success);
	};
	
	var activeWorkspaceDeleteVariableAggregate = function(entity, variable, agg, success){
		$.proxy(activeWorkspaceUpdateVariableAggregate, this)(entity, variable, agg, "DELETE", success);
	};
	
	var activeWorkspaceUpdateVariableAggregate = function(entity, variable, agg, method, success){
		var $this = this;
		$this.activeWorkspace(function(ws){
			
			$.ajax({
				url:"rest/workspace/active/entity/"+entity.id+"/variable/"+variable.id+"/aggregates/"+agg+".json",
				dataType:"json",
				method: method
			}).done(function(response){
				
				$this.activeWorkspace(function(ws){
					var variableToUpdate = response;

					// replace old qty variable with the new one
					var ent = ws.getEntityById(entity.id);
					var vars = ent.quantitativeVariables;
					for(var i in vars){
						var variableToReplace = vars[i];
						if( variableToReplace.id.toString() == variableToUpdate.id.toString() ){
							ent.quantitativeVariables[i] = variableToUpdate;
							success(variableToUpdate);
						}
					}
				});
				
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
/**
 * Load all the entities from the active workspace and call the callback function
 * 
 * @param callback
 */
WorkspaceManager.loadEntities = function(callback) {
	$.ajax({
		url: "rest/workspace/entities.json",
		dataType: "json"
	})
	.done(function(response) {
		callback(response);
	});
};

/**
 * Load all the quantitative variables of the specified entity and call the callback function
 * 
 * @param entityId
 * @param callback
 */
WorkspaceManager.loadQuantitativeVariables = function(entityId, callback) {
	$.ajax({
		url: "rest/workspace/entities/" + entityId + "/qtyvariables.json",
		dataType: "json"
	})
	.done(function(response) {
		callback(response);
	});
};

WorkspaceManager.saveVariable = function(variable, success, error, complete) {
	$.ajax({
		url: "rest/workspace/variable/save.json",
		dataType: "json",
		type: "POST",
		data: variable
	})
	.done(function(response) {
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