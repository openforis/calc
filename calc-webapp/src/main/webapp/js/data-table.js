/**
 * Data table
 */

function DataTable($container) {
	/*
	 * ui components
	 */
	this.container = $container;
	this.dataTablePagination = this.container.find(".data-table-pagination");
	this.tableResults = this.container.find(".table-results");
	
	/*
	 * used for table settings
	 */
	this.rows = 50;
	
	this.offset = 0;
	this.max = 0;
	this.entity = null;
	this.fields = null;

	this.job = null;
};

DataTable.prototype = (function(){
	
	//setting the job, table gets reset
	var setJob = function(job) {
		if( job ) {
			this.job = job;
			
			this.offset = 0;
			
			this.max = this.job.tasks[0].totalItems;
			this.entity = this.job.tasks[0].calculationStep.outputEntityId;
			this.fields = this.job.tasks[0].calculationStep.variables;
			
			$.proxy( emptyTable, this );
		}
	};
	
	//empty the html table
	var emptyTable = function() {
		if(this.offset == 0) {
			var $thead = this.tableResults.find('thead');
			$thead.empty();
		}
		var $tbody = this.tableResults.find('tbody');
		$tbody.empty();
		
	};
	
	var showResults = function(job) {
		console.log(job);
		$.proxy(setJob , this , job);
		
    	$.ajax({
    		url:"rest/job/"+job.id+"/results.json",
    		dataType:"json",
    		data:{offset:this.offset, numberOfRows:this.rows},
    		
    		success: $.proxy(function(response) {
//    			this.showResults(response);
//    			refresh(response);
    			$.proxy(refresh , this , response);
    		}, this)
    		
    	});

	};
	
	// refresh html table with data given in input
	var refresh = function(data) {
		
//		this.data = data;
		this.container.show();
		//update paging
		var paging = (this.offset+1) + " - " + (this.offset + data.length) + " of " + this.max;
		this.dataTablePagination.html( paging );
		
		//update table headers
		if(this.offset == 0) {
			$thead = this.tableResults.find('thead');
			var $tr = $("<tr></tr>");
			$tr.hide();
			$thead.append($tr);
			var $th = $("<th></th>");
			$th.html("Record");
			$tr.append($th);
			$.each(this.fields, function(i,field) {
				$th = $("<th></th>");
				$th.html(field);
				$tr.append($th);
			});
			$tr.fadeIn(100);
		}
		
		// show items
		var delay = 0;
		var rowNum = this.offset + 1;
		
		var dataManager = this;
		$.each(data, function(i,record) {
			var $tbody = dataManager.tableResults.find('tbody');
			var $tr = $("<tr></tr>");
			$tr.hide();
			$tbody.append($tr);
			var $td = $("<td></td>");
			$td.html((rowNum++));
			$tr.append($td);
			$.each(record.fields,function(j,field){
				var $td = $("<td></td>");
				$td.html(field);
				$tr.append($td);
			});
			setTimeout(function() {
				$tr.fadeIn(100);
			}, (delay += 50) );
		});

		
	};
	
	return {
		
		constructor : DataTable,
		
		show : function() {
			this.container.fadeIn();
		},

		hide : function(){
			this.container.hide();
		},
		
		//show job results
        showJobResults : function(job) {
        	console.log(this);
//        	this._(showJobResults)();
        	$.proxy(showResults , this , job);
        	console.log("ok did it call it?");
        }
//		,
//		
//		_:function(callback) {
//			// instance referer
//			var self = this;
//			// callback that will be used
//			return function() {
//				return callback.apply(self, arguments);
//			};
//		}
	
	};

})();