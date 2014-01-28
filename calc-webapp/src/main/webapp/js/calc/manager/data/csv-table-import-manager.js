/**
 * Manager for csv table import
 * @author Mino Togna 
 */

CsvTableImport = function(container, filepath, columns, tableName) {
	// ui container
	this.container = $(container);
	
	this.table = this.container.find("table");
	this.importBtn = this.container.find("[name=import-btn]");
	
	this.filepath = filepath;
	this.tableName = tableName;
	
	var $this = this;
	this.columns = [];
	$.each(columns, function(i,col){
		$this.columns[i] = new ImportColumnOption(col);
	});
	
	this.init();
};

//available data types
CsvTableImport.dataTypes = ["String","Integer","Real"];

CsvTableImport.prototype.init = function(){
	var $this = this;
	
	this.table.empty();
	
	// append headers
	var thead = $( "<thead></thead>" );
	this.table.append( thead );
	var tr = $( "<tr></tr>" );
	thead.append( tr );
	var th = $( "<th width='50%'>Column</th>" );
	tr.append( th );
	th = $( "<th width='20%'>Import</th>" );
	tr.append( th );
	th = $( "<th width='30%'>Data type</th>" );
	tr.append( th );
	
	//tbody
	var tbody = $( "<tbody></tbody>" );
	this.table.append( tbody );
	$.each(this.columns, function(i,col){
		
		var row = col.row();
		tbody.append( row );
		
	});
	
	this.importBtn.off("click");
	// import click btn handler
	this.importBtn.click(function(event){
		event.preventDefault();
		UI.lock();
		
		var cols = JSON.stringify( $this.columns );
		var params = { "filepath":$this.filepath, "table":$this.tableName, "columns":cols };
		
		$.ajax({
			url : "rest/workspace/active/import-table.json",
			dataType : "json",
			method : "POST",
			data : params 
		}).done(function(response){
			
			JobManager.getInstance().checkJobStatus( function(job){
				var task = job.tasks[0];				
				$this.importCallback(task.schema, task.table); 
			}, true );
			
			UI.unlock();
		});
		
	});
};

CsvTableImport.prototype.hide = function(){
	this.container.hide();
};

CsvTableImport.prototype.import = function(importCallback){
	this.importCallback = importCallback;
};

ImportColumnOption = function(column) {
	this.column = column;
	this.import = true;
	this.dataType = CsvTableImport.dataTypes[0]; 
};

ImportColumnOption.prototype.row = function() {
	var $this = this;
	
	var tr = $( "<tr></tr>" );
	var td = $( "<td></td>" );
	td.html( this.column );
	tr.append( td );
	
	// import checkbox
	td = $( "<td></td>" );
	tr.append( td );	
	var checkbox = $( '<input type="checkbox">' );
	checkbox.prop('checked', true);
	checkbox.change(function(e){
		e.preventDefault();
		$this.import = this.checked;
		
		var opacity = ($this.import) ? 1 : 0.2;
		setTimeout( function(e){ tr.fadeTo( 200, opacity ); 
		} , 50);
	});
	td.append( checkbox );
	
	// datatype select
	td = $( "<td></td>" );
	tr.append( td );	
	var select = $( '<select class="form-control"></select>' );
	$.each(CsvTableImport.dataTypes , function(i,type){
		var option = $( "<option></option>" );
		option.val( type );
		option.html( type );
		if( i == 0 ){
			option.prop( "selected", true );
		}
		select.append( option );
	});
	select.change(function(e){
		e.preventDefault();
		$this.dataType = $(this).val();
	});

	
	td.append( select );
	
	return tr;
	
};