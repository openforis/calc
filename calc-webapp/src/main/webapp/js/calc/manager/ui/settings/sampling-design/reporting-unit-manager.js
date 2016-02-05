/**
 * Manager for Stratified step
 * @author M. Togna
 */
ReportingUnitManager = function( container , sdERDManager , stepNo ){
	
	var dataProvider 	= new CsvFileDataProvider( null , false);
	dataProvider.setTableInfo( new ReportingUnitManager.prototype.tableInfo() );
	dataProvider.tableAlias = 'Reporting Unit (AOI)';
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
	
	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.phase1-table-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design.two-stages-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.psu-table-change", this.updateJoin , this );
	
	this.join						= new ERDTableJoin( 'aoi-join' );
	this.join.multiple 				= false;
	this.join.rightColumnsReadOnly 	= true;
	this.join.leftJoinPointCssClass = 'anchor-right';
	
	this.addJoin( this.join );
	
	this.update();
};
ReportingUnitManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
ReportingUnitManager.prototype.constructor 	= ReportingUnitManager;


ReportingUnitManager.prototype.update = function(){
	
	if( this.sd().samplingUnitId  ){
		this.container.fadeIn();
		this.highlight();
		this.updateJoin();
		this.join.show();
	} else {
		this.container.hide();
		this.join.hide();
	}

};

ReportingUnitManager.prototype.erdJoinSettings = function(){
//	{
//		"rightTable": {"table":"plot"},
//		"columns"	:[{"left":"cluster","right":"id"},{"left":"plot","right":"no"}],
//		"leftTable"	:{"schema":"calc","table":"_phase1_plot_naforma_20151216"}
//	}
	
	var joinSettings = {};
	joinSettings.rightTable = {"table":"aoi" , "schema":"calc"};
	joinSettings.columns 	= [ {"left": null, "right": this.joinColumnName()} ];
	
	if( this.sd().aoiJoinSettings ){
		joinSettings.leftTable 			= { "table":this.sd().aoiJoinSettings.table, "schema":this.sd().aoiJoinSettings.schema };
		joinSettings.columns[0].left 	= this.sd().aoiJoinSettings.column;
	}
	
	return joinSettings;
};
ReportingUnitManager.prototype.updateJoin = function(){

	this.join.disconnect();
		
	var leftTable = null;
	if( this.sd().twoPhases === true ){
		leftTable = this.sdERDManager.twoPhasesManager.table;
	} else if( this.sd().twoStages === true ){
		leftTable = this.sdERDManager.psuManager.table;
	} else {
		leftTable = this.sdERDManager.baseUnitManager.table;
	}
	
	var firstEdit 	= !this.sd().id;
	var show 		= ( this.stepNo == this.currentStepNo ) || !firstEdit;
	
	if( leftTable && leftTable.dataProvider.getTableInfo() && show){
		this.join.setRightTable( this.table );
		this.join.setLeftTable( leftTable );
		
		this.join.connect( this.erdJoinSettings() );
		
		this.updateEditMode();
	}
		
};
ReportingUnitManager.prototype.showEditStep = function( evt, stepNo ){
	
	SamplingDesignStepManager.prototype.showEditStep.call( this , evt , stepNo );

	if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo && !this.sd().id){
		this.update();
	}
};
ReportingUnitManager.prototype.joinColumnName = function(){
	var colName = ';'
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		if( ws.aoiHierarchies && ws.aoiHierarchies.length > 0 ){
			var levels = ws.aoiHierarchies[ 0 ].levels;
			var level = levels[ levels.length -1 ];
			colName = level.caption + ' Code';
		}
	});
	
	return colName;
};

ReportingUnitManager.prototype.tableInfo = function(){
	this.table 			= "aoi";
	this.schema			= 'calc';
	this.fields 		= {};
	this.fields.table 	= "aoi";
	this.fields.schema 	= 'calc';
	this.fields.columns = [];
	
	this.fields.columns.push( { 'column_name' : ReportingUnitManager.prototype.joinColumnName() } );
};