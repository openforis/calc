/**
 * Manager for data. query, charts, etc..
 * 
 * @author Mino Togna
 */
function DataManager($container) {
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

DataManager.prototype = (function() {

	// shows the scatter plot
	var showScatterPlot = function() {
		this.dataTable.hide();
		this.scatterPlot.show();

		// temp solution
		// refresh scatter plot if 2 vars in output
//		if (this.dataTable.variables.length == 2) {
//			this.scatterPlot.setAxes( this.dataTable.variables[0], this.dataTable.variables[1] );
//			this.scatterPlot.refresh( this.dataTable.data );
//		}
	};
	
	// shows the data table
	var showDataTable = function() {
		this.scatterPlot.hide();
		this.dataTable.show();
	};
	
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

	var updateJob = function(job){
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
	
	/**
	 * Declare event handlers
	 */
	var initEventHandlers = function() {
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

	return {

		constructor : DataManager,

		_init : function() {
			$.proxy(initEventHandlers , this)();
		},

		showJobResults : function(job) {
			$.proxy( start , this )(job);
		},
		
		updateJob : updateJob
	};

})();
