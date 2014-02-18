/**
 * Data provider for entity views 
 * 
 * @author Mino Togna
 *
 */
DataViewProvider = function( entityId, variables, exportEnabled ) {
	// base context path for rest call
	this.contextPath = "rest/data/entity/";

	// current entity to query
	this.entityId = entityId;
	this.variables = ( variables ) ? variables : [] ;
	this.exportEnabled = exportEnabled == true;
};

/**
 * 
 */
DataViewProvider.prototype.count = function(success) {
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
DataViewProvider.prototype.data = function(offset , numberOfItems , excludeNulls, variables, success) {
	// prepare request parameters
	var vars = (variables) ? variables : this.variables;
	var params = { offset:offset, fields:vars.join(',') };
	if(numberOfItems) {
		params.numberOfRows = numberOfItems;
	}
	if(excludeNulls){
		params.excludeNulls = excludeNulls;
	}
	
	$.ajax({
		url : this.contextPath + this.entityId + "/query.json" ,
		dataType : "json",
		data : params
	}).done(function(response) {
		success(response);
	});
};

DataViewProvider.prototype.addVariable = function( variable ) {
	this.variables.push( variable );
};

DataViewProvider.prototype.deleteVariable = function( variable ) {
	for( var i in this.variables ){
		var v = this.variables[i];
		if( v === variable ){
			this.variables.splice(i, 1);
		}
	}
};

DataViewProvider.prototype.exportToCsv = function(excludeNulls) {
	var url = this.contextPath + this.entityId + "/data.csv";
	var params = $.param({
		fields: this.variables.join(','),
		excludeNulls: this.excludeNulls == true
	});
	url = url + "?" + params;
	window.open(url, '_blank');
};
