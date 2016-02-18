/**
 * Manager for Double sampling (or two phases) step
 * @author M. Togna
 */
TwoPhasesManager = function( container , sdERDManager , stepNo ){
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
		var uploadCallback 	= $.proxy(this.uploadCallback , this );
		var dataProvider 	= new CsvFileDataProvider( uploadCallback , true );
		dataProvider.tableAlias = "Phase 1";
		dataProvider.tableName 	= ws.phase1PlotTableName;
		dataProvider.extSchema	= false;
		
		this.baseUnitPhase1Join	= new ERDTableJoin( 'baseUnitPhase1Join' );
		this.baseUnitPhase1Join.rightJoinPointCssClass = 'anchor-right';
		this.baseUnitPhase1Join.onChange = $.proxy( this.joinChange , this );
		
		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
		this.addJoin( this.baseUnitPhase1Join );
		
		EventBus.addEventListener( "calc.sampling-design.two-phases-change", 	this.update, this );
		EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
		
		this.update();
		this.loadPhase1Table();

	} , this ) );
	
	
};
TwoPhasesManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
TwoPhasesManager.prototype.constructor 	= TwoPhasesManager;


TwoPhasesManager.prototype.update = function(){
	
	if( this.sd().twoPhases === true ){
		this.container.parent().fadeIn();
//		this.container.fadeIn();
		this.baseUnitPhase1Join.show();
		this.highlight();
		this.loadPhase1Table();
	} else {
		this.container.parent().hide(0);
//		this.container.hide();
		this.baseUnitPhase1Join.hide(0);
	}

};

TwoPhasesManager.prototype.loadPhase1Table = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws.phase1PlotTable ) {
			
			new TableDataProvider( "calc" , ws.phase1PlotTable ).tableInfo( function(response) {
				var phase1TableInfo = response;
				$this.dataProvider.setTableInfo( phase1TableInfo );
				
				$this.table.updateView();
				$this.updateJoins();
				
				EventBus.dispatch( "calc.sampling-design.phase1-table-change", null );
				
			});
			
		}
	});
};

TwoPhasesManager.prototype.updateJoins = function(){
	this.baseUnitPhase1Join.disconnect();
	
	if( this.sd().twoPhases === true ){
		this.baseUnitPhase1Join.setRightTable( this.sdERDManager.baseUnitManager.table );
		this.baseUnitPhase1Join.setLeftTable( this.table );
		
		if( this.dataProvider.getTableInfo() && this.sdERDManager.baseUnitManager.dataProvider.getTableInfo() ){
			this.baseUnitPhase1Join.connect( this.sd().phase1JoinSettings );
			
			this.updateEditMode();
		}
	} else {
//		this.baseUnitPhase1Join.hide();
	}
	
};

TwoPhasesManager.prototype.uploadCallback = function( schema , table ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspaceSetPhase1PlotsTable(table, function(ws){
		$this.loadPhase1Table();
	});
};

TwoPhasesManager.prototype.baseUnitChange = function(){
	//Reset phase 1 join settings
	this.sd().phase1JoinSettings = {};
	this.updateJoins();
	
};

TwoPhasesManager.prototype.joinChange = function(){
	this.sd().phase1JoinSettings = this.baseUnitPhase1Join.jsonSettings();
};