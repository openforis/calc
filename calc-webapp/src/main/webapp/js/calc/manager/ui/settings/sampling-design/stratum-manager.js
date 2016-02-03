/**
 * Manager for Stratified step
 * @author M. Togna
 */
StratumManager = function( container , sdERDManager , stepNo ){
	
	var dataProvider 	= new CsvFileDataProvider( null , false);
	dataProvider.setTableInfo( new StratumManager.prototype.tableInfo() );
	dataProvider.tableAlias = 'Stratum Labels';
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
	
	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.update, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design.phase1-table-change", this.updateJoin , this );
	
	this.join						= new ERDTableJoin( 'stratum-join' );
	this.join.multiple 				= false;
	this.join.rightColumnsReadOnly 	= true;
	this.join.leftJoinPointCssClass = 'anchor-right';
};
StratumManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
StratumManager.prototype.constructor 	= StratumManager;


StratumManager.prototype.update = function(){
	
	if( this.sd().stratified === true  ){
		this.container.fadeIn();
		this.highlight();
		this.updateJoin();
		this.join.show();
	} else {
		this.container.hide();
		this.join.hide();
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
//	console.log( this.sd() );
//	console.log( this.dataProvider.getTableInfo() );
	
	this.join.disconnect();
	
	if( this.sd().stratified === true ){
		
		var leftTable = null;
		if( this.sd().twoPhases === true ){
			leftTable = this.sdERDManager.twoPhasesManager.table;
		} else {
			leftTable = this.sdERDManager.baseUnitManager.table;
		}
		
		if( leftTable && leftTable.dataProvider.getTableInfo() ){
			this.join.setRightTable( this.table );
			this.join.setLeftTable( leftTable );
			
			this.join.connect( this.erdJoinSettings() );
		}
		
	} else {
//		this.join.hide();
	}
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