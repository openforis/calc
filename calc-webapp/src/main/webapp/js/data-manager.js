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

	this.data = null;

	/**
	 * Ui components
	 */
	this.dataTable = new DataTable(this.container.find(".data-table"));
	this.scatterPlot = new ScatterPlot(this.container.find('.scatter-plot'));

	this._init();
};

DataManager.prototype = (function() {

	// shows the scatter plot
	var showScatterPlot = function() {
		this.dataTable.hide();
		this.scatterPlot.show();

		// temp solution
		// refresh scatter plot if 2 vars in output
		if (this.dataTable.variables.length == 2) {
			this.scatterPlot.setAxes( this.dataTable.variables[0], this.dataTable.variables[1] );
			this.scatterPlot.refresh( this.dataTable.data );
		}
	};
	
	// shows the data table
	var showDataTable = function() {
		this.scatterPlot.hide();
		this.dataTable.show();
	};


	/**
	 * Declare event handlers
	 */
	var initEventHandlers = function() {
		// events handlers
		var $this = this;
		this.container.find(".table-btn").click(function(e) {
			e.preventDefault();
			$.proxy(showDataTable , $this)();
		});

		this.container.find(".scatter-plot-btn").click(function(e) {
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
			this.container.fadeIn();
			// show data table to ui
			$.proxy(showDataTable, this)();
			// update data table with job data
			this.dataTable.showJobResults(job);
		}
	};

})();
