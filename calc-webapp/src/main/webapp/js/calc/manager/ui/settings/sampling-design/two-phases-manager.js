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
		
		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
		EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.update, this );
	} , this ) );
	
};
TwoPhasesManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
TwoPhasesManager.prototype.constructor 	= TwoPhasesManager;


TwoPhasesManager.prototype.update = function(){
	var sd = this.sdERDManager.samplingDesign;
	
	if( sd.twoPhases === true ){
		this.container.fadeIn();
		if( this.sdERDManager.editMode === true && this.currentStepNo == this.stepNo ){
			this.table.highlight();
		}
	} else {
		this.container.hide();
	}

};


TwoPhasesManager.prototype.loadPhase1TableInfo = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws.phase1PlotTable ) {

			new TableDataProvider( "calc" , ws.phase1PlotTable ).tableInfo( function(response) {
				var phase1TableInfo = response;
//				Utils.applyFunction( callback );
			});
			
		}
	});
};


TwoPhasesManager.prototype.uploadCallback = function( schema , table ){
//	console.log( schema + '    '  + table  );
	WorkspaceManager.getInstance().activeWorkspaceSetPhase1PlotsTable(table, function(ws){
		
		// update join settings
		EventBus.dispatch( "calc.sampling-design.phase1-table-change", null );
	});
};