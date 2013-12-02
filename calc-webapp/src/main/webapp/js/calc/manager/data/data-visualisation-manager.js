/**
 * Manager for data. query, charts, etc..
 * TODO rename to DataVisualisationManager
 * @author Mino Togna
 */
function DataVisualisationManager($container) {
	//init container
	this.container = $dataVisualization.clone();
	this.container.hide();
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
		
		this.container.fadeIn(400);
		
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
	
	
	
	
	
	
	
	// DEPRECATED
	//start the process of showing results
	var start = function(job) {
		this.job = job;
		
		this.container.fadeIn();
		// show data table to ui
		$.proxy(showDataTable, this)();
		//set job to scatter plot
		this.scatterPlot.setJob(job);
		// update data table with job data
		this.dataTable.showJobResults(job);
	};
	// DEPRECATED
	var updateJob = function(job) {
		this.job = job;
		this.scatterPlot.updateJob(job);
		this.dataTable.updateJob(job);
		
		//disable/enable buttons if job completed
		if(job.status=="COMPLETED"){
			UI.enable( this.scatterPlotBtn );
		} else {
			UI.disable( this.scatterPlotBtn );
		}
	};
	
	return {

		constructor : DataVisualisationManager,
		
		_init : init,
		
		show : show,
		
		
		
		
		// DEPRECATED
		showJobResults : function(job) {
			$.proxy( start , this )(job);
		},
		
		updateJob : updateJob
	};

})();
