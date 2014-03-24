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
	
	this.filters 	= new VariableFilters(); 
};

/**
 * 
 */
DataViewProvider.prototype.count = function(success) {
    	var params 	= {};
    	params.filters 	= this.filters.getConditions();
    	
	$.ajax({
		url	: this.contextPath + this.entityId + "/count.json",
		dataType: "json",
		data	: params,
		method	: "POST"
	}).done(function(response) {
		var cnt = response.fields.count;
		success(cnt);
	}).error( function() {
		Calc.error.apply( this , arguments );
	});
};

/**
 * query the server to get the data
 */
DataViewProvider.prototype.data = function(offset , numberOfItems , excludeNulls, variables, success) {
	// prepare request parameters
	var vars = (variables) ? variables : this.variables;
	var params = { offset:offset, fields:vars.join(',') };
	params.filters = this.filters.getConditions();
	
	if(numberOfItems) {
		params.numberOfRows = numberOfItems;
	}
	if(excludeNulls) {
		params.excludeNulls = excludeNulls;
	}
	
	$.ajax({
		url 		: this.contextPath + this.entityId + "/query.json" ,
		dataType 	: "json",
		data 		: params,
		method 		: "POST"
	}).done(function(response) {
		success(response);
	}).error( function() {
		Calc.error.apply( this , arguments );
	});
};

DataViewProvider.prototype.addVariable = function( variable ) {
	this.variables.push( variable );
};

DataViewProvider.prototype.deleteVariable = function( variable ) {
	for( var i in this.variables ){
		var v = this.variables[i];
		if( v === variable ) {
			this.variables.splice(i, 1);
		}
	}
};

DataViewProvider.prototype.exportToCsv = function(excludeNulls) {
	var url = this.contextPath + this.entityId + "/data.csv";
	var data = {
			fields		: this.variables.join(','),
			excludeNulls	: this.excludeNulls == true,
			filters 	: this.filters.getConditions()
	};
	UI.Form.download(url, data);
};
