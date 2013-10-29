/**
 * Data table for showing job results
 * 
 * @author Mino Togna
 */

function DataTable($container) {
	/*
	 * ui components
	 */
	this.container = $container;
	this.dataTablePagination = this.container.find(".data-table-pagination");
	this.dataTableButtons = this.container.find(".data-table-buttons");
	this.tableResults = this.container.find(".table-results");
	this.nextButton = this.dataTableButtons.find(".next");
	this.prevButton = this.dataTableButtons.find(".prev");

	/*
	 * used for table settings
	 */
	this.rows = 50;
	
	this.offset = 0;
	this.totalItems = 0;
	this.entity = null;
	this.variables = null;

	// the job to show the results
	this.job = null;
	
	//data currently loaded
	this.data = null;
	
	//event handlers
	this.nextButton.click( $.proxy(this._nextPage , this) );
	this.prevButton.click( $.proxy(this._prevPage , this) );
};

DataTable.prototype = (function(){
	
	var updateJob = function(job) {
		if( this.job.id == job.id ){
			this.job = job;
		}
	};
	
	//start the process of showing job results
	var start = function(job) {
		if( job ) {
			var $this = this;
//			console.log(job);
			this.job = job;
			
			// reset count and data
			this.offset = 0;
			this.totalItems = this.job.tasks[0].totalItems;
			this.entity = this.job.tasks[0].calculationStep.outputEntityId;
			this.variables = this.job.tasks[0].calculationStep.variables;
			//update table headers
			$.proxy(updateHeaders, this)();
			
			//update table with data loaded 
			//this is a temporary fix. job logic should be moved to a level up.
			var task = this.job.tasks[0];
			if(task.itemsProcessed == 0) {
				//we wait one second so that job can process some items
				setTimeout(function(e){
					$.proxy(refresh, $this)();
				}, 1000);
			} else {
				$.proxy(refresh, this)();
			}
//			console.log($job);
//			console.log($job.tasks[0]);
			
		}
	};
	
	//update table headers
	var updateHeaders = function() {
		$thead = this.tableResults.find('thead');
		// empty first
		$thead.empty();
		
		var $tr = $("<tr></tr>");
		$tr.hide();
		$thead.append($tr);
		var $th = $("<th></th>");
		$th.html("Row #");
		$tr.append($th);
		$.each(this.variables, function(i,field) {
			$th = $("<th></th>");
			$th.html(field);
			$tr.append($th);
		});
		$tr.fadeIn(100);
	};
	
	//refresh the table by getting data from the server
	var refresh = function() {
		var $this = this;
    	$.ajax({
    		url:"rest/job/"+$this.job.id+"/results.json",
    		dataType:"json",
    		data:{offset:this.offset, numberOfRows:this.rows},
    		
    		success: function(response) {
    			$.proxy(updateButtons , $this)();
    			$.proxy(updateData , $this)(response);
    		}
    		
    	});

	};
	
	// disable / enable prev/next buttons
	var updateButtons = function(){
		if(this.offset == 0){
			UI.disable(this.prevButton);
//			this.prevButton.prop('disabled', true);
		} else {
			UI.enable(this.prevButton);
//			this.prevButton.prop('disabled', false);
		}
		if( (this.offset+this.rows) >= this.totalItems){
			UI.disable(this.nextButton);
//			this.nextButton.prop('disabled', true);
		} else {
			UI.enable(this.nextButton);
//			this.nextButton.prop('disabled', false);
		}
	};
	
	// update html table with data given as parameter
	var updateData = function(data) {
		//set the data
		this.data = data;
		//empty table before showing results
		var $tbody = this.tableResults.find('tbody');
		$tbody.empty();
		
		//update paging text
		var paging = (this.offset+1) + " - " + (this.offset + data.length) + " of " + this.totalItems;
		this.dataTablePagination.html( paging );
		
		// show items
		var delay = 0;
		var rowNum = this.offset + 1;
		// add rows to table
		$.each(data, function(i,record) {
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
		// handlers for next and prev buttons
		_nextPage : function(e) {
			this.offset = this.offset += this.rows;
			$.proxy(refresh , this)();
		},
		
		_prevPage : function(e) {
			this.offset = this.offset -= this.rows;
			$.proxy(refresh , this)();
		}, 
		
		//show job results
        showJobResults : function(job) {
        	$.proxy(start , this)(job);
        },
        
        updateJob : updateJob
	
	};

})();