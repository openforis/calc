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
	 $this = this;
	 
	 
	 
	 // ui component
	 
	 //data table
	 this.dataTable = this.container.find(".data-table");
	 this.dataTablePagination = this.dataTable.find(".data-table-pagination");
	 this.tableResults = this.dataTable.find(".table-results");
//	 console.log(this.dataTable);
//	 console.log(this.tableResults);
//	 this.tableTa
};

// methods for datatable
DataManager.prototype.showTable = function(entity, fields, count) {
	this.max = count;
	this.fields = fields;
	
	//console.log("test");
//	rest/data/tree/query.json?fields=land_use,dbh&offset=0&numberOfRows=10
	$.ajax({
		url:"rest/data/"+entity+"/query.json",
		dataType:"json",
		data:{fields:this.fields.join(),offset:this.offset,numberOfRows:this.rows}
	})
	.done(function(response) {
//		console.log(response);
//		console.log($this);
		$this.clearTable();
		$this.showResults(response);
//		$data = response;
//		dataManager.showTable(response.entityName, $calculationStep.variables, response.count);
	});
	
//	this.container.show();
};

DataManager.prototype.showTable = function(job, entity, fields, count) {
	this.max = count;
	this.fields = fields;
	
	//console.log("test");
//	rest/data/tree/query.json?fields=land_use,dbh&offset=0&numberOfRows=10
	$.ajax({
		url:"rest/job/"+job.id+"/results.json",
		dataType:"json",
//		data:{fields:this.fields.join(),offset:this.offset,numberOfRows:this.rows}
		data:{offset:this.offset, numberOfRows:this.rows}
	})
	.done(function(response) {
//		console.log(response);
//		console.log($this);
		$this.clearTable();
		$this.showResults(response);
//		$data = response;
//		dataManager.showTable(response.entityName, $calculationStep.variables, response.count);
	});
	
//	this.container.show();
};

DataManager.prototype.clearTable = function() {
	if(this.offset == 0) {
		var $thead = this.tableResults.find('thead');
		$thead.empty();
	}
	var $tbody = this.tableResults.find('tbody');
	$tbody.empty();
};

DataManager.prototype.showResults = function(data) {
	this.container.show();
	//update paging
//	var start = $this.offset + 1;
	var paging = ($this.offset+1) + " - " + ($this.offset + data.length) + " of " + $this.max;
	this.dataTablePagination.html( paging );
	
	//update table headers
	if(this.offset == 0) {
		$thead = $this.tableResults.find('thead');
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
	$.each(data, function(i,record) {
		var $tbody = $this.tableResults.find('tbody');
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
	
	this.offset += numberOfRows;
	
	//update rows
	
};