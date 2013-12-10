/**
 * Data provider for data test
 *  
 * @author S. Ricci
 */
function DataTestViewProvider(entityId, variables) {
	// base context path for rest call
	this.contextPath = "rest/job/active/step/test/";
	
	this.entityId = entityId;
	this.variables = variables;
}

DataTestViewProvider.prototype = (function() {
	
	/**
	 * 
	 */
	var count = function(success) {
		$.ajax({
			url : this.contextPath + "count.json",
			dataType : "json"
		}).done(function(response) {
			var cnt = response.fields.count;
			success(cnt);
		});
	};
	
	/**
	 * query the server to get the data
	 */
	var data = function(offset , numberOfItems , excludeNulls, variables, success) {
		// prepare request parameters
		var params = { offset:offset };
		if(numberOfItems) {
			params.numberOfRows = numberOfItems;
		}
		if(excludeNulls){
			params.excludeNulls = excludeNulls;
		}
		
		$.ajax({
			url : this.contextPath + "query.json" ,
			dataType : "json",
			data : params
		}).done(function(response) {
			success(response);
		});
	};
	
	
	//prototype
	return {
		constructor : DataTestViewProvider,
		
		//public methods
//		_init : init,
		
		count : count ,
		
		data : data
		
	};
})();
