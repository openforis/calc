/**
 * Manager for Stratified step
 * @author M. Togna
 */
StratumManager = function( container , sdERDManager , stepNo ){
	
	var uploadCallback 	= $.proxy(this.uploadCallback , this );
	var dataProvider 	= new CsvFileDataProvider( uploadCallback , false);
	dataProvider.setTableInfo( new StratumManager.prototype.tableInfo() );
	dataProvider.tableAlias = 'Stratum Labels';
	dataProvider.selectColumnsToImport = false;
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
	
	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.update, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design.phase1-table-change", this.updateJoin , this );
	
	this.join						= new ERDTableJoin( 'stratum-join' );
	this.join.multiple 				= false;
	this.join.rightColumnsReadOnly 	= true;
	this.join.leftJoinPointCssClass = 'anchor-right';
	this.join.onChange				= $.proxy( this.onJoinChange , this );
	this.addJoin( this.join );
	
	this.update();
	
};
StratumManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
StratumManager.prototype.constructor 	= StratumManager;


StratumManager.prototype.update = function(){
	
	if( this.sd().stratified === true  ){
		this.container.fadeIn();
		this.highlight();
		this.updateJoin();
//		this.join.show();
	} else {
		this.join.disconnect();
		this.container.hide();
	}

};

StratumManager.prototype.erdJoinSettings = function(){
//	{
//		"rightTable": {"table":"plot"},
//		"columns"	:[{"left":"cluster","right":"id"},{"left":"plot","right":"no"}],
//		"leftTable"	:{"schema":"calc","table":"_phase1_plot_naforma_20151216"}
//	}
	
	var joinSettings = {};
	joinSettings.rightTable = {"table":"stratum" , "schema":"calc"};
	joinSettings.columns 	= [ {"left": null,"right":"stratum_no"} ];
	
	if( this.sd().stratumJoinSettings ){
		joinSettings.leftTable 			= { "table":this.sd().stratumJoinSettings.table, "schema":this.sd().stratumJoinSettings.schema };
		joinSettings.columns[0].left 	= this.sd().stratumJoinSettings.column;
	}
	
	return joinSettings;
};
StratumManager.prototype.updateJoin = function(){
	
	this.join.disconnect();
	
	if( this.sd().stratified === true ){
		
		var stratumJoinleftTable = null;
		if( this.sd().twoPhases === true ){
			stratumJoinleftTable = this.sdERDManager.twoPhasesManager.table;
		} else {
			stratumJoinleftTable = this.sdERDManager.baseUnitManager.table;
		}
		
		if( stratumJoinleftTable && stratumJoinleftTable.dataProvider.getTableInfo() ){
			this.join.setRightTable( this.table );
			this.join.setLeftTable( stratumJoinleftTable );
			
			this.join.connect( this.erdJoinSettings() );
			
			this.updateEditMode();
		}
		
	} else {
//		this.join.hide();
	}
};

StratumManager.prototype.onJoinChange = function(){
	var sdStratumJoinSettings = {};

	var erdJoinSettings = this.join.jsonSettings();
	if( erdJoinSettings.leftTable ){
		sdStratumJoinSettings.table 	= erdJoinSettings.leftTable.table;
		sdStratumJoinSettings.schema 	= erdJoinSettings.leftTable.schema;
		sdStratumJoinSettings.column	= '';
		if( erdJoinSettings.columns && erdJoinSettings.columns.length >0 ){
			var col = erdJoinSettings.columns[ 0 ];
			sdStratumJoinSettings.column 	= col.left;
		}
	}
	
	 this.sd().stratumJoinSettings = sdStratumJoinSettings;
};

StratumManager.prototype.uploadCallback = function( response ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspaceImportStrata( response.fields.filepath , function(ws){
		UI.unlock();
		UI.showSuccess( ws.strata.length +" strata successfully imported", true);
//		$this.updateStrata(ws);
	});
};

StratumManager.prototype.tableInfo = function(){
	this.table 			= "stratum";
	this.schema			= 'calc';
	this.fields 		= {};
	this.fields.table 	= "stratum";
	this.fields.schema 	= 'calc';
	this.fields.columns = [];
	
	this.fields.columns.push( { 'column_name' : "stratum_no" } );
	this.fields.columns.push( { 'column_name' : "stratum_label" } );
};