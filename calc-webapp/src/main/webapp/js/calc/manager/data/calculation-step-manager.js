/**
 * Manager for CalculationStep model class 
 */
function CalculationStepManager() {
	this.contextPath = "rest/calculationstep";
	
}

CalculationStepManager.prototype = (function() {
	
	// why not using an instance variable?!
	var contextPath = "rest/calculationstep";
	
	/**
	 * Load all the calculation steps for the active workspace
	 */
	var loadAll = function( callback ) {
		$.ajax({
			url: contextPath + "/load.json",
			dataType:"json"
		}).done(function(response){
			callback(response);
		}).error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Load the calculation step with the specified id and call the callback function
	 */
	var load = function(id, callback) {
		$.ajax({
			url: contextPath + "/"+id+"/load.json",
			dataType:"json"
		})
		.done(function(response){
			if ( callback ) {
				callback(response);
			}
		}).error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Inserts or updates a calculation step 
	 */
	var save = function( $step, successCallback, errorCallback, completeCallback ){
		var $this = this;
		$.ajax({
			url: contextPath + "/save.json",
			dataType: "json",
			data: $step,
			type: "POST"
		})
		.done(function(response) {
			if( response.status == "OK" ){
				$this.updateWorkspaceStatus( response.fields , function(){
//					var variable = response.fields.variable;
					
					if ( successCallback ) {
						successCallback(response);
					}
				});
			} else if( successCallback ){
				successCallback(response);
			}

		})
		.error(function(e) {
			Calc.error.apply( this , arguments );
			if ( errorCallback ) {
				errorCallback(e);
			}
		})
		.complete(function() {
			if ( completeCallback ) {
				completeCallback();
			}
		});
		
	};
	
	
	/**
	 * Delete the calculation step with the specified id
	 */
	var remove = function(id, callback) {
		var $this = this;
		$.ajax({
			url: contextPath + "/"+id+"/delete.json",
			dataType:"json",
			type: "POST"
		})
		.done(function(response) {
			$this.updateWorkspaceStatus( response.fields , function(){
				if ( callback ) {
					callback(response);
				}
			});
		}).error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Updates the calculation step sortOrder with the specified one
	 */
	var updateStepNumber = function(id, stepNo, callback) {
		$.ajax({
			url: contextPath + "/"+id+"/stepno/" + stepNo + ".json",
			dataType:"json",
			type: "POST"
		})
		.done(function(response){
			if ( callback ) {
				callback(response);
			}
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	/**
	 * Executes a job for the calculation step test with id stepId
	 */
	var test = function(stepId, parameters, success){
		$.ajax({
			url : contextPath + "/"+stepId+"/test.json",
			type: "POST", 
			data: JSON.stringify(parameters),
			dataType: "json",
			contentType: "application/json"
		})
		.done(function(job) {
			if ( success ) {
				success(job);
			}
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	//prototype
	return {
		constructor : CalculationStepManager,
		
		//public methods
		loadAll : loadAll
		,
		load : load
		,
		save : save
		,
		remove : remove
		,
		updateStepNumber: updateStepNumber
		,
		test : test
	};
})();

CalculationStepManager.prototype.updateWorkspaceStatus = function( obj ,  callback ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		var chain = obj.processingChain;
		// update processing chain
		for( var i in ws.processingChains ) {
			var wsChain = ws.processingChains[i];
			if( wsChain.id == chain.id ){
				ws.processingChains[i] = chain;
			}
		}
		// update variable if there is
		if( obj.addedVariable ){
			var entity = ws.getEntityById( obj.addedVariable.entityId );
			entity.addVariable( obj.addedVariable );
		}
		if( obj.deletedVariableId ){
			var variable = ws.getVariableById( obj.deletedVariableId );
			var entity = ws.getEntityById( variable.entityId );
			entity.deleteVariable( obj.deletedVariableId );
		}
		
		if( callback ) {
			callback.apply( $this );
		}
		Calc.homeCalculationManager.updateSteps();
		Calc.updateButtonStatus();
		Calc.homeDataManager.refresh();
	});
};

/**
 * Set the active property of the calculation step passed as argument
 * @param step
 * @param active
 * @param callback
 * 
 * @author Mino Togna
 */
CalculationStepManager.prototype.updateActive = function( step ,  active , callback ){
	var $this = this;
	UI.lock();
	WorkspaceManager.getInstance().activeWorkspace(function ( ws ) {
		
		$.ajax({
			url			: $this.contextPath + "/" + step.id + "/active/" + active + ".json",
			dataType	:"json" ,
			type		: "POST" 
		})
		.done(function(response){
			var step = response.fields.calculationStep;
			ws.updateCalculationStep( step );
			
			UI.unlock();
			
			Utils.applyFunction( callback , step );
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		});
	});
	
}

//singleton instance
var _calculationStepManager = null;
CalculationStepManager.getInstance = function() { 
	if(!_calculationStepManager){
		_calculationStepManager = new CalculationStepManager();
	}
	return _calculationStepManager;
};
