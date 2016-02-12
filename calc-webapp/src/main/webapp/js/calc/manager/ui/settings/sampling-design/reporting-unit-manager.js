/**
\ * Manager for Stratified step
 * @author M. Togna
 */
ReportingUnitManager = function( container , sdERDManager , stepNo ){
	
	var onUpload = $.proxy( this.showImport , this );
	var dataProvider 	= new CsvFileDataProvider( onUpload , false);
	dataProvider.setTableInfo( new ReportingUnitManager.prototype.tableInfo() );
	dataProvider.tableAlias = 'Reporting Unit (AOI)';
	dataProvider.tableType	= CsvFileDataProvider.AOI_TABLE_TYPE;
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
	
	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design.stratified-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.phase1-table-change", this.updateJoin , this );
	EventBus.addEventListener( "calc.sampling-design.two-stages-change", this.updateJoin, this );
	EventBus.addEventListener( "calc.sampling-design.psu-table-change", this.updateJoin , this );
	
	this.join						= new ERDTableJoin( 'aoi-join' );
	this.join.multiple 				= false;
	this.join.rightColumnsReadOnly 	= true;
	this.join.leftJoinPointCssClass = 'anchor-right';
	this.join.onChange				= $.proxy( this.onJoinChange , this );
	
	this.joinColumnStepNo = -1;
	
	this.addJoin( this.join );
	
	this.update();
};
ReportingUnitManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
ReportingUnitManager.prototype.constructor 	= ReportingUnitManager;

ReportingUnitManager.prototype.show = function(){
	this.container.fadeIn();
	this.highlight();
};

ReportingUnitManager.prototype.update = function(){
	
	if( this.sd().samplingUnitId  ){
		this.show();
		this.updateJoin();
//		this.join.show();
	} else {
//		this.container.hide();
//		this.join.hide();
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
		leftTable = (this.sdERDManager.twoPhasesManager) ? this.sdERDManager.twoPhasesManager.table : null;
	} else if( this.sd().twoStages === true ){
		leftTable = (this.sdERDManager.psuManager) ? this.sdERDManager.psuManager.table : null;
	} else if( this.sdERDManager.baseUnitManager ){
		leftTable = this.sdERDManager.baseUnitManager.table;
	}
	
	var firstEdit 	= !this.sd().id;
	var show 		= ( this.joinColumnStepNo == this.currentStepNo ) || !firstEdit;
	
	if( leftTable && leftTable.dataProvider.getTableInfo() && show){
		this.join.setRightTable( this.table );
		this.join.setLeftTable( leftTable );
		
		this.join.connect( this.erdJoinSettings() );
		
		this.updateEditMode();
	}
		
};
ReportingUnitManager.prototype.showEditStep = function( evt, stepNo ){
	
	SamplingDesignStepManager.prototype.showEditStep.call( this , evt , stepNo );

//	if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo && !this.sd().id){
//		this.update();
//	}
	
	if( stepNo == this.joinColumnStepNo ){
		this.update();
		this.join.highlight();
		this.join.setEditMode( true );
	} else {
		this.join.setEditMode( false );
	}
};
ReportingUnitManager.prototype.joinColumnName = function(){
	var colName = ''
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		if( ws.aoiHierarchies && ws.aoiHierarchies.length > 0 ){
			var levels = ws.aoiHierarchies[ 0 ].levels;
			var level = levels[ levels.length -1 ];
			colName = level.caption + ' Code';
		}
	});
	
	return colName;
};

ReportingUnitManager.prototype.onJoinChange = function(){
	var sdStratumJoinSettings = {};
	
	var erdJoinSettings = this.join.jsonSettings();
	if( erdJoinSettings.leftTable ){
		sdStratumJoinSettings.table 	= erdJoinSettings.leftTable.table;
		sdStratumJoinSettings.schema 	= erdJoinSettings.leftTable.schema;
		sdStratumJoinSettings.column 	= '';
		if( erdJoinSettings.columns && erdJoinSettings.columns.length >0 ){
			var col = erdJoinSettings.columns[ 0 ];
			sdStratumJoinSettings.column 	= col.left;
		}
	}
	
	 this.sd().aoiJoinSettings = sdStratumJoinSettings;
	
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


ReportingUnitManager.prototype.showImport = function(response){
	var filePath = response.fields.filepath;
	
	var $this = this;
	var modalDiv 		= $( '#aoi-import-column-selector' );
	var onImport		= function(){
		modalDiv.modal( 'hide' );
		EventBus.dispatch( "calc.sampling-design.reporting-unit-table-change", null );
		$this.dataProvider.setTableInfo( new ReportingUnitManager.prototype.tableInfo() );
		$this.updateJoin();
	};
	var importer		= new ReportingUnitImportManager( modalDiv , onImport , response );
	
	modalDiv.modal( 'show' );
};