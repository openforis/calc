/**
 * Data table
 */

function DataTable($container) {
	/*
	 * ui components
	 */
	this.container = $container;
	this.dataTablePagination = this.container.find(".data-table-pagination");
	this.dataTableButtons = this.container.find(".data-table-buttons");
	this.tableResults = this.container.find(".table-results");
	
//	this.dataTablePagination.hide();
//	this.tableResults.hide();
	
	/*
	 * used for table settings
	 */
	this.rows = 50;
	
	this.offset = 0;
	this.max = 0;
	this.entity = null;
	this.fields = null;

	this.job = null;
	
	//data currently loaded
	this.data = null;
	
	
	//event handlers
	this.dataTableButtons.find(".next").click( $.proxy(this.nextPage , this) );
	this.dataTableButtons.find(".prev").click( $.proxy(this.prevPage , this) );
};

DataTable.prototype = (function(){
	//empty the html table
	var emptyTable = function() {
		if(this.offset == 0) {
			var $thead = this.tableResults.find('thead');
			$thead.empty();
		}
		var $tbody = this.tableResults.find('tbody');
		$tbody.empty();
		
	};
	
	//setting the job, table gets reset
	var setJob = function(job) {
		if( job ) {
			this.job = job;
			
			this.offset = 0;
			
			this.max = this.job.tasks[0].totalItems;
			this.entity = this.job.tasks[0].calculationStep.outputEntityId;
			this.fields = this.job.tasks[0].calculationStep.variables;
			
		}
	};
	
	var showResults = function() {
		var $this = this;
		
    	$.ajax({
    		url:"rest/job/"+$this.job.id+"/results.json",
    		dataType:"json",
    		data:{offset:this.offset, numberOfRows:this.rows},
    		
    		success: function(response) {
//    			this.showResults(response);
//    			refresh(response);
    			$.proxy(refresh , $this)(response);
    		}
    		
    	});

	};
	
	// refresh html table with data given in input
	var refresh = function(data) {
		
		console.log( "refresh starting from " + this.offset );
		
		//empty table before showing results
		$.proxy( emptyTable, this )();
		
		this.data = data;
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
			$th.html("Row #");
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
		
		//TODO enable/disable prev/next buttons
		
	};
	
	return {
		
		constructor : DataTable,
		
		show : function() {
			this.container.fadeIn();
		},

		hide : function(){
			this.container.hide();
		},
		
		nextPage : function(e) {
			this.offset = this.offset += this.rows;
			$.proxy(showResults , this)();
		},
		
		prevPage : function(e) {
			console.log("prev: TODO");
//			$.proxy(showResults , this)();
			this.offset = this.offset -= this.rows;
			$.proxy(showResults , this)();
		}, 
		
		//show job results
        showJobResults : function(job) {
        	$.proxy(setJob , this)(job);
        	$.proxy(showResults , this)(job);
        }
	
	};

})();