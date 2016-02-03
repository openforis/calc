/**
 * Manager for creating table joins for the current sampling design 
 * 
 * @author M. Togna
 */
//SamplingDesignERDTableJoinsManager = function( samplingDesignErdManager ){
//	
//	this.samplingDesignErdManager 	= samplingDesignErdManager;
//	
//	this.baseUnitPhase1Join			= new ERDTableJoin( 'baseUnitPhase1Join' );
//	this.stratumJoin				= new ERDTableJoin( 'stratumJoin' );
//	this.aoiJoin					= new ERDTableJoin( 'aoiJoin' );
//	
//	this.initView();
//	
//	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
//	
//	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.twoPhasesChange , this );
//	EventBus.addEventListener( "calc.sampling-design.phase1-table-change", this.twoPhasesChange , this );
//	
//	EventBus.addEventListener( "calc.sampling-design.two-stages-change", this.twoStagesChange , this );
//	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.stratifiedChange , this );
//	
//};
//SamplingDesignERDTableJoinsManager.prototype.sd = function(){
//	return this.samplingDesignErdManager.samplingDesign;
//};
//
//SamplingDesignERDTableJoinsManager.prototype.initView = function(){
//};
//
///**
// * Sampling design change event listeners 
// */
//SamplingDesignERDTableJoinsManager.prototype.baseUnitChange = function(){
//	//Reset phase 1 join settings
//	this.sd().phase1JoinSettings = {};
//	this.baseUnitPhase1Join.reset();
//	
//};
//SamplingDesignERDTableJoinsManager.prototype.twoPhasesChange = function(){
//	if( this.sd().twoPhases === true && this.samplingDesignErdManager.twoPhasesManager.table.dataProvider.getTableInfo() ){
//		this.baseUnitPhase1Join.setRightTable( this.samplingDesignErdManager.baseUnitManager.table );
//		this.baseUnitPhase1Join.setLeftTable( this.samplingDesignErdManager.twoPhasesManager.table );
//		
//		this.baseUnitPhase1Join.connect( this.sd().phase1JoinSettings );
//	} else {
//		this.baseUnitPhase1Join.reset();
//	}
//};
//SamplingDesignERDTableJoinsManager.prototype.twoStagesChange = function(){
//	//TODO
//};
//SamplingDesignERDTableJoinsManager.prototype.stratifiedChange = function(){
//	//TODO
//};


ERDTableColumnSelector = function( id , label ){
	this.id 	= id;
	this.label 	= label;
	this.value	= '';
};
//ERDTableColumnSelector.prototype.setTable = function( table ){
//	this.table = table;
//};
ERDTableColumnSelector.prototype.connect = function( table , value , onChange ){
	this.table = table;
	
	if( this.table ){
		
		var html = $( '<div class="row table-column-selector-row">'+
			 '<div class="col-md-12 table-join">'+
			 	'<div class="row row-table-column-selector-header">'+
			 		'<div class="col-md-11 col-md-offset-1 table-column-selector-header">'+
			 		'</div>'+
		 		'</div>'+ 
		 		'<div class="row row-table-column-selector-body">'+
		 			'<div class="col-md-12 table-column-selector-body">'+
		 			'</div>'+
		 		'</div>'+ 
			 '</div>'+
		 '</div>' 
		);
		html.addClass( this.id );
		html.find( '.table-column-selector-header' ).html( this.label );
		
		var row 	= $( '<div class="row no-margin join-row"></div>' );
		var col 	= $( '<div class="col-md-12 text-center"></div>' );
		row.append( col );
		var select 	= $( '<select class="form-control">' );
		col.append( select );
		var combo   = select.combobox();
		combo.data( this.table.dataProvider.getTableInfo().fields.columns, 'column_name','column_name' );
		combo.change( $.proxy(function(){
			this.value = combo.val();
			Utils.applyFunction( onChange , this );
		} , this ) );
		if(value){
			combo.val( value );
		}
		this.combo = combo;
		
		html.find( '.table-column-selector-body' ).append( row );
		
		this.table.columnSelectors.append( html );
	}
	
};

ERDTableColumnSelector.prototype.disconnect = function( ){
	if( this.table ){
		this.combo.val( '' );
		this.table.columnSelectors.find( '.'+this.id ).remove();
	}
};
//ERDTableColumnSelector.prototype.reset = function( ){
//};

ERDTableJoin = function( id ){
	this.leftTable 		= null;
	this.rightTable 	= null;
	
	this.leftHtml 		= null;
	this.rightHtml 		= null;
	
	this.leftHtmlCssId	= null;
	this.rightHtmlCssId	= null;
	
	this.id 			= id;
	
	this.rows 			= new Array();
	
	// by default multiple columns can be used to join the two tables
	this.multiple 		= true;
	
	this.rightColumnsReadOnly 	= false;
	this.leftColumnsReadOnly 	= false;
	
	this.leftJoinPointCssClass = 'anchor-left';
	this.rightJoinPointCssClass = 'anchor-left';
};

ERDTableJoin.prototype.setLeftTable = function( leftTable ){
	this.leftTable = leftTable;
};
ERDTableJoin.prototype.setRightTable = function( rightTable ){
	this.rightTable = rightTable;
};
ERDTableJoin.prototype.connect	= function( joinSettings ){
	if( this.leftTable && this.rightTable ){
		this.leftHtml 		= this.getHtml();
		this.leftHtmlCssId 	= this.id+"_l";
		this.leftHtml.addClass( this.leftHtmlCssId );
		this.leftRows		= this.leftHtml.find( '.table-join-body' );
		
		this.rightHtml 			= this.getHtml();
		this.rightHtmlCssId 	= this.id+"_r";
		this.rightHtml.addClass( this.rightHtmlCssId );
		this.rightRows			= this.rightHtml.find( '.table-join-body' );
		
		this.leftTable.tableJoins.append( this.leftHtml ); 
		this.rightTable.tableJoins.append( this.rightHtml ); 
		
		if( joinSettings && joinSettings.columns && joinSettings.columns.length > 0 ){

			for( var i in joinSettings.columns ){
				var col = joinSettings.columns[i];
				this.addRow(col.left, col.right);
			}
			
		} else {
			// append empty row
			this.addRow();
		}
		
		this.leftTable.table.parent().on( 'scroll' , $.proxy(this.updateConnections, this) );
		this.rightTable.table.parent().on( 'scroll' , $.proxy(this.updateConnections, this) );
		
	}
};
ERDTableJoin.prototype.updateConnections	= function( ){
	$( 'connection.'+this.id ).connections('update');
};
ERDTableJoin.prototype.disconnect	= function(){
	this.reset();
	if( this.leftTable ){
		this.leftTable.tableJoins.find( '.'+this.leftHtmlCssId ).remove();
	}
	if( this.rightTable ){
		this.rightTable.tableJoins.find( '.'+this.rightHtmlCssId ).remove();
	}
};

ERDTableJoin.prototype.getHtml = function(){
	 var div = 
		 $( '<div class="row table-join-row">'+
				 '<div class="col-md-12 table-join">'+
				 	'<div class="row row-table-join-header">'+
				 		'<div class="col-md-9 table-join-header">'+
				 		'</div>'+
				 		'<div class="col-md-3 table-join-header  text-center">'+
				 			'<button type="button" class="btn no-background" name="add-btn">'+
				 				'<i class="fa fa-plus-square"></i>'+
				 			'</button>'+
				 		'</div>'+
			 		'</div>'+ 
			 		'<div class="row row-table-join-body">'+
			 			'<div class="col-md-12 table-join-body">'+
			 			'</div>'+
			 		'</div>'+ 
				 '</div>'+
			 '</div>' 
		 );
	 
	 div.addClass( this.containerCssClass );
	 
	 var addBtn = div.find('button[name=add-btn]');
	 if( this.multiple === true ){

		 addBtn.click( $.proxy(function(){
			 this.addRow();
		 } , this) );
		 
	 } else {
		 addBtn.hide();
	 }
	 
	 return div;
};
ERDTableJoin.prototype.show = function(){
	if( this.leftTable ) 
		this.leftTable.body.find( '.' + this.leftHtmlCssId ).show();
	if( this.rightTable )
		this.rightTable.body.find( '.' + this.rightHtmlCssId ).show();
	$( 'connection.'+this.id ).show();
};
ERDTableJoin.prototype.hide = function(){
	if( this.leftTable )
		this.leftTable.body.find( '.' + this.leftHtmlCssId ).hide();
	if( this.rightTable )
		this.rightTable.body.find( '.' + this.rightHtmlCssId ).hide();
	$( 'connection.'+this.id ).hide();
};
ERDTableJoin.prototype.reset = function(){
	//remove rows
	for(var i in this.rows){
		var row = this.rows[i];
		row.leftRow.remove();
		row.rightRow.remove();
	}
//	$( 'connection.'+this.id ).connections('update');
	this.rows = new Array();
};

ERDTableJoin.prototype.addRow = function( leftValue, rightValue ){
	var row = new ERDTableJoinRow( this, leftValue, rightValue );
	this.rows.push(row);
	
	this.leftRows.append( row.leftRow );
	this.rightRows.append( row.rightRow );
	
	$( "."+row.leftCssClass +'_c').connections( {to:'.'+row.rightCssClass+'_c' , 'class': 'table-join-connection '+this.id} );

};

ERDTableJoin.prototype.deleteRow = function( row ){
	for(var i in this.rows){
		var r = this.rows[i];
		if( r == row ){
			row.leftRow.fadeOut(140);
			row.rightRow.fadeOut(140);
			setTimeout( $.proxy( function(){
				row.leftRow.remove();
				row.rightRow.remove();
				
				this.updateConnections();
			
			} , this ) , 140 );
			
			this.rows.splice( i, 1 );

			break;
		}
	}
};

ERDTableJoin.prototype.jsonSettings = function(){
	var settings = {};
	if( this.leftTable && this.rightTable ){
		settings.leftTable 			= {};
		settings.leftTable.table 	= this.leftTable.fields.table;
		settings.leftTable.schema 	= this.leftTable.fields.schema;
		settings.rightTable 		= {};
		settings.rightTable.table 	= this.rightTable.fields.table;
		settings.rightTable.schema 	= this.rightTable.fields.schema;
		settings.columns 			= [];
		
		for( var i in this.rows ){
			var row 			= this.rows[i];
			var rowSettings 	= {};
			rowSettings.left 	=  row.leftColumn;
			rowSettings.right 	=  row.rightColumn;
			settings.columns.push( rowSettings );
		}
	}
	return settings;
};

ERDTableJoinRow = function( tableJoin, leftValue, rightValue ) {
	var $this 			= this;
	
	this.tableJoin		= tableJoin;
	
	this.leftColumn 	= leftValue;
	this.rightColumn 	= rightValue;
	
	var now	= $.now();
	
	this.leftCssClass 	= tableJoin.leftHtmlCssId  + '_' + now;
	this.rightCssClass 	= tableJoin.rightHtmlCssId + '_' + now;
	
	var onChangeLeft = function( val ){
		$this.leftColumn = val;
	};
	var onChangeRight = function( val ){
		$this.rightColumn = val;
	};
	
	this.leftRow	= this.getHtmlRowJoin( 	this.leftCssClass , 
											this.tableJoin.leftTable.dataProvider.getTableInfo().fields.columns , 
											onChangeLeft , 
											leftValue ,
											this.tableJoin.leftColumnsReadOnly ,
											this.tableJoin.leftJoinPointCssClass );
	
	this.rightRow	= this.getHtmlRowJoin( 	this.rightCssClass , 
											this.tableJoin.rightTable.dataProvider.getTableInfo().fields.columns, 
											onChangeRight , 
											rightValue,
											this.tableJoin.rightColumnsReadOnly ,
											this.tableJoin.rightJoinPointCssClass );
	
};

ERDTableJoinRow.prototype.getHtmlRowJoin = function( cssClass , columns, onChange , value , readOnly , anchorCssClass ){
	var row 	= $( '<div class="row no-margin join-row"></div>' );
	row.addClass( cssClass );
	var col 	= $( '<div class="col-md-9"><span class="anchor '+cssClass+'_c ' + anchorCssClass +'"/></div>' );
	row.append( col );
	
	var select 	= $( '<select class="form-control">' );
	col.append( select );
	
	var combo   = select.combobox();
	combo.data( columns, 'column_name','column_name' );
	combo.change( function(){
		onChange( combo.val() );
	});
	if(value){
		combo.val( value );
	}
	if( readOnly === true ){
		combo.disable();
	}
	if( this.tableJoin.multiple === true ) {
		
		var colDeleteBtn 	= $( '<div class="col-md-1"></div>' );
		row.append( colDeleteBtn );
		var deleteBtn 		= $( '<button type="button" class="btn no-background" name="delete-btn"><i class="fa fa-minus-square-o"></i></button>' );
		colDeleteBtn.append( deleteBtn );
		deleteBtn.click( $.proxy( function(e){
			e.preventDefault();
			this.tableJoin.deleteRow(this);
		} , this ) );

	}
	
	return row;
};