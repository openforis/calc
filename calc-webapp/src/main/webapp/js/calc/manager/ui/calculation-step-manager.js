/**
 * Manager for CalculationStep model class 
 */
function CalculationStepManager() {
	
}

CalculationStepManager.prototype = (function() {

	var BASE_URL = "rest/calculationstep";
	
	/**
	 * Load all the calculation steps associated to the default processing chain in the active workspace
	 * and call the specified callback function
	 *  
	 * @param callback
	 */
	var loadAll = function(callback) {
		$.ajax({
			url: BASE_URL + "/load.json",
			dataType:"json"
		}).done(function(response){
			callback(response);
		});
	};
	
	/**
	 * Load the calculation step with the specified id and call the callback function
	 * 
	 * @param id
	 * @param callback
	 */
	var load = function(id, callback) {
		$.ajax({
			url: BASE_URL + "/"+id+"/load.json",
			dataType:"json"
		})
		.done(function(response){
			if ( callback ) {
				callback(response);
			}
		});
	};
	
	/**
	 * Inserts or updates a calculation step 
	 */
	var save = function($step, successCallback, errorCallback, completeCallback) {
		$.ajax({
			url: BASE_URL + "/save.json",
			dataType: "json",
			data: $step,
			type: "POST"
		})
		.done(function(response) {
    		if(successCallback) {
	    		successCallback(response);
    		};
		})
		.error(function(e) {
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
	 * Execute the calculation step with the specified id and call the callback function 
	 * 
	 * @param id
	 * @param callback
	 */
	var execute = function(id, totalItems, callback) {
		var params = {};
		if( !isNaN(totalItems) ){ 
			params.totalItems = totalItems; 
		}

		$.ajax({
			url: BASE_URL + "/"+id+"/run.json",
			dataType:"json",
			data: params
//			,
//			async: false 
		})
		.done(function(response){
			callback(response);
		});
//		.error(function(e){
//			console.log("error!!! on exec");
//			console.log(e);
//		});
	};
	
	/**
	 * Delete the calculation step with the specified id
	 * 
	 * @param id
	 * @param callback
	 */
	var deleteStep = function(id, callback) {
		$.ajax({
			url: BASE_URL + "/"+id+"/delete.json"
		})
		.done(function(response){
			if ( callback ) {
				callback(response);
			}
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
		execute : execute
		,
		deleteStep : deleteStep
	};
})();

//singleton instance of workspace manager
var _calculationStepManager = null;
CalculationStepManager.getInstance = function() { 
	if(!_calculationStepManager){
		_calculationStepManager = new CalculationStepManager();
	}
	return _calculationStepManager;
};
