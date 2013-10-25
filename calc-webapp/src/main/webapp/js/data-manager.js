/**
 * Manager for data. query, charts, etc.. 
 */
function DataManager ($container) {
	 this.container = $dataVisualization.clone();

	 this.container.hide();
	 
	 $container.append(this.container);
	 
	 this.data = null;
	 
	 // ui component
	 //table
//	 this.offset = 0;
//	 this.rows = 50;
//	 this.max = 0;
//	 this.entity = null;
//	 this.fields = null;
	 
	 //data table TODO: move to a separate file
	 this.dataTable = new DataTable( this.container.find(".data-table") );
//	 this.dataTablePagination = this.dataTable.find(".data-table-pagination");
//	 this.tableResults = this.dataTable.find(".table-results");
	 
	 // scatter plot
	 this.scatterPlot = new ScatterPlot( this.container.find('.scatter-plot') );
	 
	 this._init();
};


DataManager.prototype = (function(){
	
	//private methods
	var showScatterPlot = function() {
		this.dataTable.hide();
		this.scatterPlot.show();
		
		//temp solution
		
		//refresh scatter plot if 2 vars in output
		if(this.dataTable.fields.length == 2){
			this.scatterPlot.setAxes(this.dataTable.fields[0], this.dataTable.fields[1]);
			this.scatterPlot.refresh(this.dataTable.data);
		}
	};
	
	var showDataTable = function() {
		this.dataTable.show();
		this.scatterPlot.hide();
	};

	
	var reset = function() {
		this._(emptyTable)();
		
		this.offset = 0;
		this.rows = 50;
		this.max = 0;
		this.entity = null;
		this.fields = null;
		this.data = null;
	};
	
//	var emptyTable = function() {
//		if(this.offset == 0) {
//			var $thead = this.tableResults.find('thead');
//			$thead.empty();
//		}
//		var $tbody = this.tableResults.find('tbody');
//		$tbody.empty();
//		
//	};
	
	
//	var showResults = function(data) {
//		
//		this.data = data;
//		this.container.show();
//		//update paging
//		var paging = (this.offset+1) + " - " + (this.offset + data.length) + " of " + this.max;
//		this.dataTablePagination.html( paging );
//		
//		//update table headers
//		if(this.offset == 0) {
//			$thead = this.tableResults.find('thead');
//			var $tr = $("<tr></tr>");
//			$tr.hide();
//			$thead.append($tr);
//			var $th = $("<th></th>");
//			$th.html("Record");
//			$tr.append($th);
//			$.each(this.fields, function(i,field) {
//				$th = $("<th></th>");
//				$th.html(field);
//				$tr.append($th);
//			});
//			$tr.fadeIn(100);
//		}
//		
//		// show items
//		var delay = 0;
//		var rowNum = this.offset + 1;
//		
//		var dataManager = this;
//		$.each(data, function(i,record) {
//			var $tbody = dataManager.tableResults.find('tbody');
//			var $tr = $("<tr></tr>");
//			$tr.hide();
//			$tbody.append($tr);
//			var $td = $("<td></td>");
//			$td.html((rowNum++));
//			$tr.append($td);
//			$.each(record.fields,function(j,field){
//				var $td = $("<td></td>");
//				$td.html(field);
//				$tr.append($td);
//			});
//			setTimeout(function() {
//				$tr.fadeIn(100);
//			}, (delay += 50) );
//		});
//
//		
//	};
	
	/**
	 * Declare event handlers
	 */
	var initEventHandlers = function() {
		//events handlers
		 var $this = this;
		 this.container.find(".table-btn").click(function(e){
			 e.preventDefault();
			 $this._(showDataTable)();
		 });
		 
		 this.container.find(".scatter-plot-btn").click(function(e){ 
			 e.preventDefault();
			 $this._(showScatterPlot)();
		 });
	};
	
	 // prototype
    return {

        constructor : DataManager,
        
        _init : function() {
        	this._(initEventHandlers)();
        },
        
        showJobResults : function(job) {
        	this.container.fadeIn();
        	$.proxy(showDataTable , this) ();
        	this.dataTable.showJobResults(job);
        },
        
        //show job results
//        showTable : function(job, entity, fields, count) {
//        	console.log(job);
//        	this._(reset)();
//        	
//        	this.max = count;
//        	this.fields = fields;
//        	
//        	$.ajax({
//        		url:"rest/job/"+job.id+"/results.json",
//        		dataType:"json",
//        		data:{offset:this.offset, numberOfRows:this.rows},
//        		
//        		success: $.proxy(function(response) {
////        			this.showResults(response);
//        			this._(showResults)(response);
//        		}, this)
//        		
//        	});
//        	
//        },
        
		 // define private methods dedicated one
		_:function(callback) {
			// instance referer
			var self = this;
			// callback that will be used
			return function() {
				return callback.apply(self, arguments);
			};
		}
        
    };
    
    
})();


// methods for datatable
// DataManager.prototype.showTable = function(entity, fields, count) {
//	this.emptyTable();
//
//	this.max = count;
//	this.fields = fields;
//	
//	$.ajax({
//		url:"rest/data/"+entity+"/query.json",
//		dataType:"json",
//		data:{fields:this.fields.join(),offset:this.offset,numberOfRows:this.rows}
//	})
//	.done($.proxy(function(response) {
//		this.showResults(response);
//	}), this);
//};

