/**
 * data provider for strata. 
 * 
 * @author Mino Togna
 */
StrataDataProvider = function (){
	this.exportEnabled 	= false;
	
	this.schema 		= '';
	this.table 			= '';
	this.variables		= [];
	
	this.count( $.proxy(function(cnt){
		if( cnt > 0 ){
			var tableInfo 	= new StratumManager.prototype.tableInfo();
			this.schema 	= tableInfo.fields.schema;
			this.table 		= tableInfo.fields.table;
			
			for(var i in tableInfo.fields.columns ){
				var col = tableInfo.fields.columns[ i ];
				this.variables.push( col.column_name );
			}
		}
	},this));
};

/**
 * Returns info on the current table
 */
StrataDataProvider.prototype.tableInfo = function(success) {
	Utils.applyFunction(success , new StratumManager.prototype.tableInfo() );
};

StrataDataProvider.prototype.count = function(success) {
	var cnt = 0;
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		if( ws.strata && ws.strata.length > 0 ){
			cnt = ws.strata.length;
		}
		Utils.applyFunction( success , cnt );
	} , this));
	
};
	
StrataDataProvider.prototype.data = function(offset , numberOfItems , excludeNulls, variables, success) {
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
		var records = [];

		if( ws.strata && ws.strata.length > 0 ){
			var end 	= offset + numberOfItems;
			var max 	= ( ws.strata.length <= end ) ? ws.strata.length : end;
			var strata 	= ws.strata.slice( offset , max );
			
			for( var i in strata ){
				var stratum = strata[ i ];
				
				var record					= {};
				record.id 					= i;
				record.fields 				= {};
				record.fields.stratum_no 	= stratum.stratumNo;
				record.fields.stratum_label	= stratum.caption;
				
				records.push( record );
			}
		}
		
		Utils.applyFunction( success , records );
	} , this));
	
};
