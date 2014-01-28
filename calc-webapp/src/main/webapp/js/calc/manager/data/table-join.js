/**
 * it manages the join between two table
 * @author Mino Togna
 */

TableJoin = function(container) {
	
	this.container = $( container );
	
	this.settings = null;
	
	this.rows = [];
	
	this.init();
};

TableJoin.prototype.init = function(){
	// append headers
	var row = $( '<div class="row no-margin table-join-header"></div>' );
	var col = $( '<div class="col-md-12 ">Join Options</div>' );
	row.append( col );
	this.container.append( row );
	
	row = $( '<div class="row no-margin tables-header"></div>' );
	var col1 = $( '<div class="col-md-5 left-table-header"></div>' );
	row.append( col1 );
	var col2 = $( '<div class="col-md-5 right-table-header"></div>' );
	row.append( col2 );
	this.container.append( row );
};

/**
 * show and hide methods
 */
TableJoin.prototype.show = function(){
	this.initRows();
	this.container.fadeIn(200);
};
TableJoin.prototype.hide = function(){
	this.container.hide();
};
/**
 * set the table info used for the join
 */
TableJoin.prototype.setTableInfo = function( leftTable, rightTable , leftTableHeader, rightTableHeader ){
	this.leftTable = leftTable;
	this.rightTable = rightTable;
	this.leftTableHeader = leftTableHeader;
	this.rightTableHeader = rightTableHeader;
};
TableJoin.prototype.jsonSettings = function(){
	var settings = {};
	settings.leftTable = {};
	settings.leftTable.table = this.leftTable.fields.table;
	settings.leftTable.schema = this.leftTable.fields.schema;
	settings.rightTable = {};
	settings.rightTable.table = this.rightTable.fields.table;
	settings.rightTable.schema = this.rightTable.fields.schema;
	settings.columns = [];
	for( var i in this.rows ){
		var row = this.rows[i];
		settings.columns.push( row.jsonSettings() );
	}
	return settings;
};
TableJoin.prototype.initRows = function() {
	this.empty();
	// init headers
	var leftHeader = this.leftTableHeader ? this.leftTableHeader : this.leftTable.fields.table + " table";
	this.container.find(".left-table-header").html( leftHeader );
	var rightHeader = this.rightTableHeader ? this.rightTableHeader : this.rightTable.fields.table + " table";
	this.container.find(".right-table-header").html( rightHeader );

	if( this.settings && this.settings.columns ){
		// append headers
		for( var i in this.settings.columns ){
			var col = this.settings.columns[i];
			this.addRow(col.left, col.right);
		}
	} else {
		// append empty row
		this.addRow();
	}
};
TableJoin.prototype.empty = function() {
	this.rows = [];
	this.container.find(".join-row").remove();
};
/**
 * add row 
 */
TableJoin.prototype.addRow = function(leftValue, rightValue){
	var row = new TableJoinRow( this, leftValue, rightValue );
	this.rows.push(row);
	this.updateRowButtons();
};
TableJoin.prototype.deleteRow = function(row){
	for(var i in this.rows){
		var r = this.rows[i];
		if( r == row ){
			row.row.fadeOut(150);
			setTimeout( $.proxy( function(){
				row.row.remove();
				this.updateRowButtons();
			} , this ) , 140);
			this.rows.splice(i, 1);
			break;
		}
	}
	
};
TableJoin.prototype.updateRowButtons = function(){
//	var r = new TableJoinRow(this);
	this.container.find(".join-row").find("[name=delete-btn]").visible();
	if( this.rows.length == 1 ){
		this.container.find(".join-row:first").find("[name=delete-btn]").invisible();
	}

	this.container.find(".join-row:last").find("[name=add-btn]").visible();
	this.container.find(".join-row:not(:last)").find("[name=add-btn]").invisible();
	
};
TableJoinRow = function( tableJoin, leftValue, rightValue ) {
	// append html elements
	this.row = $( '<div class="row no-margin join-row"></div>' );
	this.row.hide();
	tableJoin.container.append( this.row );
	
	var leftInput = $( '<input type="text" autocomplete="off" class="form-control">' );
	var col1 = $( '<div class="col-md-5"></div>' );
	col1.append( leftInput );
	this.row.append( col1 );
	
	var leftCombo =  leftInput.combobox();
	leftCombo.data( tableJoin.leftTable.fields.columns, 'column_name','column_name' );
	leftCombo.change( $.proxy(function(){
		this.leftColumn = leftCombo.val();
	} , this ) );
	if(leftValue){
		leftCombo.val( leftValue );
	}
	
	var rightInput = $( '<input type="text" autocomplete="off" class="form-control">' );
	var col2 = $( '<div class="col-md-5"></div>' );
	col2.append( rightInput );
	this.row.append( col2 );
	
	var rightCombo = rightInput.combobox();
	rightCombo.data( tableJoin.rightTable.fields.columns, 'column_name','column_name' );
	rightCombo.change( $.proxy(function(){
		this.rightColumn = rightCombo.val();
	} , this ) );
	if(rightValue){
		rightCombo.val( rightValue );
	}
	
	var deleteBtn = $( '<button type="button" class="btn no-background col-md-1" name="delete-btn"></button>' );
	deleteBtn.append( $('<i class="fa fa-minus-square-o"></i>') );
	deleteBtn.click( $.proxy( function(e){
		e.preventDefault();
		tableJoin.deleteRow(this);
	} , this ) );
	this.row.append( deleteBtn );
	
	var addBtn = $( '<button type="button" class="btn no-background col-md-1" name="add-btn"></button>' );
	addBtn.append( $('<i class="fa fa-plus-square-o"></i>') );
	addBtn.click( function(e){
		e.preventDefault();
		tableJoin.addRow();
	});
	this.row.append( addBtn );
	
	this.row.fadeIn();
};

TableJoinRow.prototype.isFilled = function(){
	return this.leftColumn && this.rightColumn;
};

TableJoinRow.prototype.jsonSettings = function(){
	var settings = {};
	settings.left = this.leftColumn;
	settings.right = this.rightColumn;
	return settings;
};

