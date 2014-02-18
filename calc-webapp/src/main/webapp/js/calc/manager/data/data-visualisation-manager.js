/**
 * Manager for data. query, charts, etc..
 *
 * @author Mino Togna
 */
function DataVisualisationManager($container) {
	//init container
	this.container = $dataVisualization.clone();
	$container.append(this.container);

	/**
	 * Ui components
	 */
//	options ui
	this.dataVisOptions = this.container.find( ".data-vis-options" );
//	this.dataVisOptions.attr( "id","data-vis-options-"+ new Date().getTime() );
	
	// table options ui
	this.tableDataProvider 		= null;
	this.tableOptions 		= this.dataVisOptions.find( '.data-table-options' );
	this.tableEntityCombo 		= this.tableOptions.find( '.entity-select select' ).combobox();
	this.tableQuantities 		= this.tableOptions.find( ".quantities" );
	this.tableCategories 		= this.tableOptions.find( ".categories" );
	this.qBtn 			= this.tableOptions.find( "[name=q-btn]" ); 
	this.cBtn 			= this.tableOptions.find( "[name=c-btn]" ); 
	this.tableViewResultsBtn 	= this.tableOptions.find( ".view-results-btn button" ); 

	// scatter chart options ui
	this.scatterDataProvider 	= null;
	this.scatterOptions 		= this.dataVisOptions.find( '.scatter-options' );
	this.scatterEntityCombo 	= this.scatterOptions.find( '.entity-select select' ).combobox();
	this.scatterQuantities 		= this.scatterOptions.find( ".quantities" );
	this.scatterViewResultsBtn 	= this.scatterOptions.find( ".view-results-btn button" ); 
	
	// accordion option buttons
	this.dataTableBtn 	= this.dataVisOptions.find(".table-btn");
	this.scatterPlotBtn	= this.dataVisOptions.find('.scatter-btn');
	
	
	this.dataTable 		= new DataTable( this.container.find(".data-table") );
	this.scatterPlot 	= new ScatterPlot( this.container.find('.scatter-plot') );
	
	// variable filter options
	this.tableFilters 	= new VariableFilterButton();
	this.scatterFilters 	= new VariableFilterButton();
	
	this.init();
};

	// init
DataVisualisationManager.prototype.init = function() {
	var $this = this;
	// init ui states
	this.tableCategories.hide();
	
	this.refresh();
	
	// events handlers
	this.dataTableBtn.click(function(e) {
		e.preventDefault();
		$this.tableOptions.collapse("show");
		$this.scatterOptions.collapse("hide");
	});

	this.scatterPlotBtn.click(function(e) {
		e.preventDefault();
		$this.scatterOptions.collapse("show");
		$this.tableOptions.collapse("hide");
	});
	
	// table options 
	this.tableEntityCombo.change(function(e){
		var entityId = $this.tableEntityCombo.val();
		// empty sections
		$this.tableQuantities.empty();
		$this.tableCategories.empty();
		if ( entityId ){
			// create data provider
			$this.tableDataProvider = new DataViewProvider( entityId );
			
			// update variable sections
			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				var entity = ws.getEntityById( entityId );
				
				$this.addVariableOptionButtons( entity.quantitativeVariables , $this.tableQuantities , $this.tableDataProvider , $this.tableFilters );
				$this.addVariableOptionButtons( entity.categoricalVariables , $this.tableCategories , $this.tableDataProvider , $this.tableFilters );
			});

		}
	});
	
	this.qBtn.click(function(e){
		$this.tableQuantities.show();
		$this.tableCategories.hide();
	});
	this.cBtn.click(function(e){
		$this.tableCategories.show();
		$this.tableQuantities.hide();
	});
	
	this.tableViewResultsBtn.click( function(e){
		e.preventDefault();
		$this.dataTable.setDataProvider( $this.tableDataProvider );
		$this.showDataTable();
	});
	
	// scatter optiions
	this.scatterEntityCombo.change(function(e){
		var entityId = $this.scatterEntityCombo.val();
		// empty sections
		$this.scatterQuantities.empty();
		if ( entityId ){
			// create data provider
			$this.scatterDataProvider = new DataViewProvider( entityId );
			
			// update variable sections
			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				var entity = ws.getEntityById( entityId );
				
				var select = function( v , optBtn ) {
					if( $this.scatterDataProvider.variables.length == 2 ) {
						UI.showError( "You can select max two variables", true );
						optBtn.deselect();
					} else {
						$this.scatterDataProvider.addVariable( v.name );
					}
				};
				
				$this.addVariableOptionButtons( entity.quantitativeVariables , $this.scatterQuantities , $this.scatterDataProvider , $this.scatterFilters, select );

			});

		}
	});
	
	this.scatterViewResultsBtn.click( function(e){
		e.preventDefault();
		$this.scatterPlot.setDataProvider( $this.scatterDataProvider );
		$this.showScatterPlot();
	});
};

DataVisualisationManager.prototype.addVariableOptionButtons = function( variables , uiContainer, dataProvider , filters , select ) {
    for( var i in variables ) {
	var v = variables[i];
	
	var div = $( '<div class="width100 clearfix"></div>' );
	var divVarBtn = $( '<div class="width90 float-left"></div>' );
	var divFilterBtn = $( '<div class="width10 float-left filter-div"></div>' );
	div.append( divVarBtn );
	div.append( divFilterBtn );
	uiContainer.append( div );
	
	var btn = $( '<button class="btn option-btn"></button>' );
	btn.html( v.name );
	divVarBtn.append( btn );
	
	var optBtn = new OptionButton( btn );
	var selectFunction = ( select ) ? select : function( v , optBtn ) {
		dataProvider.addVariable( v.name );
	};
	var deselectFunction = function( v ) {
		dataProvider.deleteVariable( v.name );
	};
	optBtn.select( selectFunction , v  , optBtn );
	optBtn.deselect( deselectFunction , v );
	
	var addFilterBtn = function() {
	    var filterBtn = $( '<button class="btn no-background filter-btn"><i class="fa fa-filter"></i></button>' );  
	    filterBtn.data( "variable" , v );
	    
	    filterBtn.click( function(e) {
		filters.show( filterBtn );
	    });
		    
	    divFilterBtn.append( filterBtn );
	};
	addFilterBtn();
    }
};

/**
 * Refresh ui states
 */
DataVisualisationManager.prototype.refresh = function() {
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws ) {
			$this.tableCategories.empty();
			$this.tableQuantities.empty();
			$this.scatterQuantities.empty();
			
			$this.tableEntityCombo.data( ws.entities, 'id' , 'name' );
			$this.scatterEntityCombo.data( ws.entities, 'id' , 'name' );
		}
	});
};

// show
DataVisualisationManager.prototype.show = function(dataProvider) {
	// in case it's still hidden
	this.container.show();

	if( dataProvider ) {
		this.dataProvider = dataProvider;
		
		// set data provider
		this.dataTable.setDataProvider(dataProvider);
		this.scatterPlot.setDataProvider(dataProvider);
		
		// by default shows data table
		this.showDataTable();
	}
	
};

// shows the scatter plot
DataVisualisationManager.prototype.showScatterPlot = function() {
	this.dataTable.hide();
	this.scatterPlot.show();
};

// shows the data table
DataVisualisationManager.prototype.showDataTable = function() {
	this.scatterPlot.hide();
	this.dataTable.show();
};

DataVisualisationManager.prototype.hideOptions = function(){
    this.dataVisOptions.hide();
};

DataVisualisationManager.prototype.showOptions = function(){
    this.dataVisOptions.show();
};