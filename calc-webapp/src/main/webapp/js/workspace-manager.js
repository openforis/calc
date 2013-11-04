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
				
//				$this.activeWorkspace(function(ws){
					var variableToUpdate = response;
				// replace old qty variable with the new one
					variableToUpdate = ws.replaceVariable(entity.id, variableToUpdate);
					success(variableToUpdate);
//				});
				
			});
			
		});
	};
	
	var activeWorkspaceAddVariablePerHa = function(entityId, variableId, success){
		$.proxy(activeWorkspaceUpdateVariablePerHa, this)(entityId, variableId, success, "POST");
	};
	
	var activeWorkspaceDeleteVariablePerHa = function(entityId, variableId, success){
		$.proxy(activeWorkspaceUpdateVariablePerHa, this)(entityId, variableId, success, "DELETE");
	};
	
	var activeWorkspaceUpdateVariablePerHa = function(entityId, variableId, success, method) {
		var $this = this;
		
		$this.activeWorkspace(function(ws){
			var variable = ws.getQuantitativeVariableById(entityId, variableId);
			
			$.ajax({
				url:"rest/workspace/active/entity/"+entityId+"/variable/"+variable.id+"/variable-per-ha.json",
				dataType:"json",
				method: method
			}).done(function(response){
				
				$this.activeWorkspace(function(ws) {
					var variableToUpdate = response;
					// replace old qty variable with the new one
					variableToUpdate = ws.replaceVariable(entityId, variableToUpdate);
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
			var newVariable = response.fields.variable;
			if ( response.status == 'OK' ) {
				$this.activeWorkspace(function(ws) {
					ws.addQuantitativeVariable(variable.entityId, newVariable);
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

