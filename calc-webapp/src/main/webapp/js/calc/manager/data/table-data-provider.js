/**
 * data provider for dynamic tables. 
 * constructor gets schema and table in order to send the requests
 * 
 * @author Mino Togna
 */
TableDataProvider = function ( schema, table ){
	// base context path for rest call
	this.contextPath = "rest/data";
	this.schema = schema;
	this.table = table;
	
	this.variables = null;
};

/**
 * Returns info on the current table
 */
TableDataProvider.prototype.tableInfo = function(success) {
	$.ajax({
		url : this.contextPath + "/table/info.json",
		dataType : "json",
		data : { "schema":this.schema, "table":this.table }
	}).done( $.proxy( function(response) {
		success(response);
	} , this ) )
	.error( function() {
		Calc.error.apply( this , arguments );
	});
};

TableDataProvider.prototype.count = function(success) {
	
	var $this = this;
//	$.ajax({
//		url : this.contextPath + "/table/info.json",
//		dataType : "json",
//		data : { "schema":this.schema, "table":this.table }
//	}).done(function(response) {
	this.tableInfo( function(response) {
		var cnt = response.fields.count;
		var cols = response.fields.columns;
		$this.variables = [];
		$.each(cols, function(i,d){
			var col = d.column_name;
			if( "id" != col){
				$this.variables.push(col);
			}
		});
		success(cnt);
	});
};
	
	/**
	 * query the server to get the data
	 */
TableDataProvider.prototype.data = function(offset , numberOfItems , excludeNulls, variables, success) {
	// prepare request parameters
	var vars = (variables)?variables:this.variables;
	var params = { "schema":this.schema, "table":this.table, offset:offset, fields:vars.join(',') };
	if(numberOfItems) {
		params.numberOfRows = numberOfItems;
	}
	if(excludeNulls){
		params.excludeNulls = excludeNulls;
	}
	
	$.ajax({
		url : this.contextPath + "/table/query.json" ,
		dataType : "json",
		data : params
	}).done(function(response) {
		success(response);
	}).error( function() {
		Calc.error.apply( this , arguments );
	});
};
