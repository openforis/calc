/**
 * manager for the visualization of sampling design table 
 * @author Mino Togna
 */

ViewTableDataManager = function(container) {
	
	this.container 		= $( container );
	
	this.dataProvider 	= null;
	this.dataTable		= null;
};


ViewTableDataManager.prototype.show = function() {
	var show = false;
	
	var schema 	= this.dataProvider.schema;
	var table 	= this.dataProvider.table;
	if( this.dataProvider && StringUtils.isNotBlank(schema) && StringUtils.isNotBlank(table)){
		show = true;
		
		this.initDataTable();
		
	} else {
		show = false;
	}
	
	return show;
};


ViewTableDataManager.prototype.initDataTable = function(){
	var $dataTable = $('<div class="row data-table no-margin">'+
			'<div class="row no-margin">'+
				'<div class="col-md-1 data-table-buttons no-padding">'+
					'<button class="btn default-btn csv-export"><i class="fa fa-download"> CSV</i></button>'+
				'</div>'+
				'<div class="col-md-3 col-md-offset-7 data-table-pagination">'+
				'</div>'+
				'<div class="col-md-1 data-table-buttons no-padding">'+
					'<button class="btn default-btn prev"><i class="icon-angle-left"></i></button>'+
					'<button class="btn default-btn next"><i class="icon-angle-right"></i></button>'+		
				'</div>'+
			'</div>'+
			'<div class="row no-margin height94" style="overflow: auto">'+
					'<table class="table table-results">'+
				        '<thead>'+
				        '</thead>'+
				        '<tbody>'+
				        '</tbody>'+
			      	'</table>'+
			'</div>'+
		'</div>');
	this.container.append( $dataTable );
	
	this.dataTable = new DataTable( $dataTable );
	this.dataTable.dataProvider = this.dataProvider;
	this.dataTable.show();
};

