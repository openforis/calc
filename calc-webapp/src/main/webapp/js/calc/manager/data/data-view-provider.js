/**
 * Data provider for entity views 
 * @author Mino Togna
 */
function DataViewProvider(entityId, variables) {
	// base context path for rest call
	this.contextPath = "rest/data/entity/";

	// current entity to query
	this.entityId = entityId;
	this.variables = variables;
	
//	this._init();
}

DataViewProvider.prototype = (function() {
	
	/**
	 * 
	 */
	var count = function(success) {
		$.ajax({
			url : this.contextPath + this.entityId + "/count.json",
			dataType : "json"
		}).done(function(response) {
			var cnt = response.fields.count;
			success(cnt);
		});
	};
	
	/**
	 * query the server to get the data
	 */
	var data = function(offset , numberOfItems , excludeNulls, success) {
		var params = { offset:offset, numberOfRows:numberOfItems, fields:this.variables.join(','), excludeNulls:excludeNulls};
		
		$.ajax({
			url : this.contextPath + this.entityId + "/query.json" ,
			dataType : "json",
			data : params
		}).done(function(response) {
			success(response);
		});
	};
	
	
	//prototype
	return {
		constructor : DataViewProvider,
		
		//public methods
//		_init : init,
		
		count : count ,
		
		data : data
		
	};
})();
