/**
 * 
 */
ERDTable = function( parent ){
	this.parent = parent;
	
	var html = $( '<div class="row height100 erd-table"><div class="col-md-12 height100"></div></div>' );
	this.parent.append( html );
	
	var htmlHeader = $( '<div class="row height10 row-header"><div class="col-md-10 col-md-offset-1 header"></div></div>' );
	html.find('.col-md-12').append( htmlHeader );
	var htmlBody = $( '<div class="row height90 row-body"><div class="col-md-10 col-md-offset-1 body"></div></div>' );
	html.find('.col-md-12').append( htmlBody );
	
	this.header = html.find( '.header' );
	this.body 	= html.find( '.body' );
	
	// editable properties
	this.tableName = '';
};

ERDTable.prototype.setTableName = function( tableName ){
	this.header.empty();
	this.tableName = tableName;
	this.header.html( this.tableName );
};

ERDTable.prototype.setTableNameOptions = function( items, valueKey, labelKey , defaultValue, onChange ){
	this.header.empty();
	
	var select = $( '<select class="form-control width100"></select>' );
	this.header.append( select );
	
	var selectCombo = select.combobox();
	selectCombo.data( items, valueKey, labelKey );
	if( defaultValue !== undefined && defaultValue !== null ){
		selectCombo.val( defaultValue );
	}
	selectCombo.change( function(e,args){
		Utils.applyFunction( onChange , selectCombo.val() );
	});
};