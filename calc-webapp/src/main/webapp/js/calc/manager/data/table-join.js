/**
 * it manages the join between two table
 * @author Mino Togna
 */

TableJoin = function(container) {
	
	this.container = $( container );
	
//	this.init();
};

//TableJoin.prototype.init = function(){
//	
//};

/**
 * show and hide methods
 */
TableJoin.prototype.show = function(){
	this.container.fadeIn(200);
	
	var r = new TableJoinRow(this);
	
};
TableJoin.prototype.hide = function(){
	this.container.hide();
};
/**
 * set the table info used for the join
 */
TableJoin.prototype.setTableInfo = function( leftTable, rightTable ){
	this.leftTable = leftTable;
	this.rightTable = rightTable;
};


TableJoinRow = function( tableJoin ) {
	// append html elements
	this.row = $( '<div class="row no-margin"></div>' );
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
	
	var rightInput = $( '<input type="text" autocomplete="off" class="form-control">' );
	var col2 = $( '<div class="col-md-5"></div>' );
	col2.append( rightInput );
	this.row.append( col2 );
	
	var rightCombo = rightInput.combobox();
	rightCombo.data( tableJoin.rightTable.fields.columns, 'column_name','column_name' );
	rightCombo.change( $.proxy(function(){
		this.rightColumn = rightCombo.val();
	} , this ) );
	
	var deleteBtn = $( '<button type="button" class="btn no-background col-md-1" name="delete-btn"></button>' );
	deleteBtn.append( $('<i class="fa fa-minus-square-o"></i>') );
	this.row.append( deleteBtn );
	
	var addBtn = $( '<button type="button" class="btn no-background col-md-1" name="add-btn"></button>' );
	addBtn.append( $('<i class="fa fa-plus-square-o"></i>') );
	this.row.append( addBtn );
	
	this.row.fadeIn();
};

TableJoinRow.prototype.isValid = function(){
	return this.leftColumn && this.rightColumn;
};

