/**
 * Manager for Two Stages w/SRS sampling (or two phases) step
 * @author M. Togna
 */
PSUTwoStagesManager = function( container , sdERDManager , stepNo ){
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
		var uploadCallback 	= $.proxy(this.loadPSUTable , this );
		var dataProvider 	= new CsvFileDataProvider( uploadCallback , true );
		dataProvider.tableAlias = "PSU";
		dataProvider.tableName 	= ws.primarySUTableName;
		dataProvider.extSchema	= true;
		
//		this.baseUnitPhase1Join	= new ERDTableJoin( 'baseUnitPhase1Join' );
		
		this.areaColumn 		= new ERDTableColumnSelector( 'psu_area_column', 'Area column' );
		this.noBaseUnitColumn 	= new ERDTableColumnSelector( 'no_base_unit_column', 'No. base unit column' );
		
		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
		this.addColumnSelector( this.areaColumn );
		this.addColumnSelector( this.noBaseUnitColumn );
		
		EventBus.addEventListener( "calc.sampling-design.two-stages-change", 	this.update, this );
		EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
		
		this.loadPSUTable();

	} , this ) );
	
	
};
PSUTwoStagesManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
PSUTwoStagesManager.prototype.constructor 	= PSUTwoStagesManager;


PSUTwoStagesManager.prototype.update = function(){
	
	if( this.sd().twoStages === true ){
		
		if( !this.sd().twoStagesSettings ){
			this.sd().twoStagesSettings = {};
		}
		
		$( '.two-stages-container' ).fadeIn();
		this.container.fadeIn();
//		this.baseUnitPhase1Join.show();
		this.highlight();
	} else {
		$( '.two-stages-container' ).hide();
		this.container.fadeOut();
//		this.baseUnitPhase1Join.hide();
	}

};

PSUTwoStagesManager.prototype.loadPSUTable = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		new TableDataProvider( ws.extendedSchemaName , ws.primarySUTableName ).tableInfo( function(response) {
			if( response.fields.columns ){
				var PSUTableInfo = response;
				$this.dataProvider.setTableInfo( PSUTableInfo );

				$this.table.updateView();
				$this.updateColumnSelectors();
				
				EventBus.dispatch( "calc.sampling-design.psu-table-change", null );
			}

		});
	});
};

PSUTwoStagesManager.prototype.updateColumnSelectors = function(){
	this.areaColumn.disconnect();
	this.noBaseUnitColumn.disconnect();
	
	var $this = this;
	if( this.dataProvider.getTableInfo() ){
		this.areaColumn.connect( this.table , this.sd().twoStagesSettings.areaColumn , function(){
			$this.sd().twoStagesSettings.areaColumn = $this.areaColumn.value;
		});
		this.noBaseUnitColumn.connect( this.table , this.sd().twoStagesSettings.noBaseUnitColumn , function(){
			$this.sd().twoStagesSettings.noBaseUnitColumn = $this.noBaseUnitColumn.value;
		});
	
		this.updateEditMode();
	}
};

PSUTwoStagesManager.prototype.updateJoins = function(){
	if( this.sd().twoPhases === true ){
		this.baseUnitPhase1Join.setRightTable( this.sdERDManager.baseUnitManager.table );
		this.baseUnitPhase1Join.setLeftTable( this.table );
		
		if( this.dataProvider.getTableInfo() ){
			this.baseUnitPhase1Join.connect( this.sd().phase1JoinSettings );
		}
	} else {
		this.baseUnitPhase1Join.reset();
		this.baseUnitPhase1Join.hide();
	}
	
};

//PSUTwoStagesManager.prototype.uploadCallback = function( schema , table ){
//	var $this = this;
//	this.loadPSUTable();
//};

PSUTwoStagesManager.prototype.baseUnitChange = function(){
	//Reset phase 1 join settings
//	this.sd().phase1JoinSettings = {};
//	this.baseUnitPhase1Join.reset();
	
};