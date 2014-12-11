/**
 * Manager for data. query, charts, etc..
 *
 * @author Mino Togna
 */
function DataVisualisationManager( container ) {
	//init container
	this.container = $dataVisualization.clone();
	container.append( this.container );

	/**
	 * Ui components
	 */
//	options ui
	this.dataVisOptions = this.container.find( ".data-vis-options" );
//	this.dataVisOptions.attr( "id","data-vis-options-"+ new Date().getTime() );
	
	// table options
	this.tableOptionsUi	= this.dataVisOptions.find( '.data-table-options' );
	this.tableOptions	= new DataVisualisationOptions( this.tableOptionsUi );

	// scatter chart options
	this.scatterOptionsUi 					= this.dataVisOptions.find( '.scatter-options' );
	this.scatterOptions						= new DataVisualisationOptions( this.scatterOptionsUi );
	this.scatterOptions.maxVariables 		= 2;
	this.scatterOptions.disableCategories 	= true;
	
	// accordion option buttons
	this.dataTableBtn 	= this.dataVisOptions.find(".table-btn");
	this.scatterPlotBtn	= this.dataVisOptions.find('.scatter-btn');
	
	// ui result components
	this.dataTable 		= new DataTable( this.container.find(".data-table") );
	this.scatterPlot 	= new ScatterPlot( this.container.find('.scatter-plot') );
	
	this.init();
};

	// init
DataVisualisationManager.prototype.init = function() {
	var $this = this;
	
	this.refresh();
	// init ui buttons
	this.dataTableBtn.addClass( 'selected' );
	this.scatterPlotBtn.removeClass( 'selected' );
	
	// events handlers
	this.dataTableBtn.click(function(e) {
		e.preventDefault();
		
		$this.tableOptions.show();
		$this.scatterOptions.hide();
		
		$this.dataTableBtn.addClass( 'selected' );
		$this.scatterPlotBtn.removeClass( 'selected' );
	});

	this.scatterPlotBtn.click(function(e) {
		e.preventDefault();
		$this.scatterOptions.show();
		$this.tableOptions.hide();
		
		$this.dataTableBtn.removeClass( 'selected' );
		$this.scatterPlotBtn.addClass( 'selected' );
	});
	
	this.tableOptions.viewResults = function() {
		$this.dataTable.setDataProvider( $this.tableOptions.dataProvider );
		$this.showDataTable();	
	};
	
	this.scatterOptions.viewResults = function() {
		$this.scatterPlot.setDataProvider( $this.scatterOptions.dataProvider);
		$this.showScatterPlot();
	};
	
//	this.dataTableBtn.click();
};

/**
 * Refresh ui states
 */
DataVisualisationManager.prototype.refresh = function() {
	this.dataTable.hide();
	this.scatterPlot.hide();
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.tableOptions.refresh();
		$this.scatterOptions.refresh();
	});
};

// show
DataVisualisationManager.prototype.show = function(dataProvider) {
	// in case it's still hidden
	this.container.show();

	if( dataProvider ) {
		this.dataProvider = dataProvider;
		
		// set data provider
		this.dataTable.setDataProvider( dataProvider );
		this.scatterPlot.setDataProvider( dataProvider );
		
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