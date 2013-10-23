/**
 * Manager for data. query, charts, etc.. 
 */
function DataManager ($container) {
//	 this.$tableContainer = $tableContainer;
	 this.container = $dataVisualization.clone();
	 //this.container.attr("id","");
	 this.container.hide();
	 
	 $container.append(this.container);
	 
	 this.offset = 0;
	 this.rows = 50;
	 this.max = 0;
	 this.entity = null;
	 this.fields = null;
	 this.data = null;
	 
	 // ui component
	 
	 //data table TODO: move to a separate file
	 this.dataTable = this.container.find(".data-table");
	 this.dataTablePagination = this.dataTable.find(".data-table-pagination");
	 this.tableResults = this.dataTable.find(".table-results");
	 
	 // scatter plot
	 this.scatterPlot = new ScatterPlot($container.find('.scatter-plot'));
	 
	 //events handler
	 var $this = this;
	 this.container.find(".table-btn").click(function(e){
		 e.preventDefault();
		 $this.showDataTable();
	 });
	 this.container.find(".scatter-plot-btn").click(function(e){ 
		 e.preventDefault();
		 $this.showScatterPlot();
	 });
	 
	 this.reset();
};
DataManager.prototype.showScatterPlot = function() {
	this.dataTable.hide();
	this.scatterPlot.show();
	
	//refresh scatter plot if 2 vars in output
	if(this.fields.length==2){
		this.scatterPlot.setAxes(this.fields[0], this.fields[1]);
		this.scatterPlot.refresh(this.data);
	}
};

DataManager.prototype.showDataTable = function() {
	this.dataTable.fadeIn();
	this.scatterPlot.hide();
};

// methods for datatable
DataManager.prototype.showTable = function(entity, fields, count) {
	this.emptyTable();

	this.max = count;
	this.fields = fields;
	
	//console.log("test");
//	rest/data/tree/query.json?fields=land_use,dbh&offset=0&numberOfRows=10
	$.ajax({
		url:"rest/data/"+entity+"/query.json",
		dataType:"json",
		data:{fields:this.fields.join(),offset:this.offset,numberOfRows:this.rows}
	})
	.done($.proxy(function(response) {
//		console.log(response);
//		console.log($this);
		this.showResults(response);
//		$data = response;
//		dataManager.showTable(response.entityName, $calculationStep.variables, response.count);
	}), this);
	
	
//	this.container.show();
};
//show job results
DataManager.prototype.showTable = function(job, entity, fields, count) {
	this.emptyTable();
	
	this.max = count;
	this.fields = fields;
	
	$.ajax({
		url:"rest/job/"+job.id+"/results.json",
		dataType:"json",
		data:{offset:this.offset, numberOfRows:this.rows},
		
		success: $.proxy(function(response) {
			this.showResults(response);
		}, this)
		
	});
	
};

DataManager.prototype.reset = function() {
	this.emptyTable();
	
	this.offset = 0;
	this.rows = 50;
	this.max = 0;
	this.entity = null;
	this.fields = null;
	this.data = null;
};
DataManager.prototype.emptyTable = function() {
	if(this.offset == 0) {
		var $thead = this.tableResults.find('thead');
		$thead.empty();
	}
	var $tbody = this.tableResults.find('tbody');
	$tbody.empty();
	
};

DataManager.prototype.showResults = function(data) {
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
//		console.log(this);
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
		setTimeout(function(){
			$tr.fadeIn(100);
//			processNextResult($job);
		}, (delay += 50) );
	});
	
//	console.log(this.offset);
//	this.offset += this.numberOfRows;
//	console.log(this.offset);
	//update rows
	
};