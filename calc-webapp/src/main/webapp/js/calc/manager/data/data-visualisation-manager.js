/**
 * Manager for data. query, charts, etc..
 * TODO rename to DataVisualisationManager
 * @author Mino Togna
 */
function DataVisualisationManager($container) {
	//init container
	this.container = $dataVisualization.clone();
	this.container.attr("id","fuckthebuttons");
//	this.container.hide();
	$container.append(this.container);

	this.job = null;
	this.data = null;

	/**
	 * Ui components
	 */
	this.dataTable = new DataTable(this.container.find(".data-table"));
	this.scatterPlot = new ScatterPlot(this.container.find('.scatter-plot'));

	this.dataTableBtn 	=	this.container.find(".table-btn");
	this.scatterPlotBtn =	this.container.find('.scatter-plot-btn');
	
	this._init();
};

DataVisualisationManager.prototype = (function() {
	
	// init
	var init = function() {
		// events handlers
		var $this = this;
		this.dataTableBtn.click(function(e) {
			e.preventDefault();
			$.proxy(showDataTable , $this)();
		});

		this.scatterPlotBtn.click(function(e) {
			e.preventDefault();
			$.proxy(showScatterPlot , $this)();
		});
	};
	
	// show
	var show = function(dataProvider) {
		this.dataProvider = dataProvider;
		// in case it's still hidden
		this.container.show();

		// set data provider
		this.dataTable.setDataProvider(dataProvider);
		this.scatterPlot.setDataProvider(dataProvider);
		
		// by default shows data table
		$.proxy(showDataTable , this)();
	};
	
	// shows the scatter plot
	var showScatterPlot = function() {
		this.dataTable.hide();
		this.scatterPlot.show();
	};
	
	// shows the data table
	var showDataTable = function() {
		this.scatterPlot.hide();
		this.dataTable.show();
	};
	
	
	return {

		constructor : DataVisualisationManager,
		
		_init : init,
		
		show : show
	};

})();
