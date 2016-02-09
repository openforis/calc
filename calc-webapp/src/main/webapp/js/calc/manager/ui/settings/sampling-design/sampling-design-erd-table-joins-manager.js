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
	
	if( this.table && this.table.dataProvider.getTableInfo()){
		this.table.columnSelectors.find( '.'+this.id ).remove();

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
			Utils.applyFunction( onChange  );
		} , this ) );
		if(value){
			combo.val( value , true );
		}
		this.combo = combo;
		
		html.find( '.table-column-selector-body' ).append( row );
		
		this.table.columnSelectors.append( html );
		
		this.html = html;
	}
	
};

ERDTableColumnSelector.prototype.disconnect = function( ){
	if( this.html ){
		this.combo.val( '' );
		this.table.columnSelectors.find( '.'+this.id ).remove();
	}
	this.table 	= null;
	this.html	= null;
};
ERDTableColumnSelector.prototype.highlight = function(){
	if( this.html ){
		
		this.html.stop().animate( {backgroundColor : 'rgba(106, 145, 111, 0.2)'}, 500 , $.proxy(function(){
			this.html.animate( {backgroundColor : 'rgba(210, 217, 249, 0.02)'}, 600);
		}, this) );
		
	}
};

ERDTableColumnSelector.prototype.setEditMode = function(editMode){
	this.editMode = editMode;
	
	if( this.html ){
		
		if( this.editMode === true ){
			UI.enable( this.html.find( 'input:not(.read-only)' ) );
			this.html.find( 'button[name=add-btn],button[name=delete-btn],span').visible() ;
		} else {
			UI.disable( this.html.find( 'input:not(.read-only)' ) );
			this.html.find( 'button[name=add-btn],button[name=delete-btn],span').invisible() ;
		}
	
	}
}

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
	
	this.leftColumnsReadOnly 	= false;
	this.rightColumnsReadOnly 	= false;
	
	this.leftJoinPointCssClass = 'anchor-left';
	this.rightJoinPointCssClass = 'anchor-left';
	
	this.onChange = function(){};
	
	EventBus.addEventListener( "calc.sampling-design.cluster-change", this.updateConnections , this );
};

ERDTableJoin.prototype.setLeftTable = function( leftTable ){
	this.leftTable = leftTable;
};
ERDTableJoin.prototype.setRightTable = function( rightTable ){
	this.rightTable = rightTable;
};
ERDTableJoin.prototype.connect	= function( joinSettings ){
	if( this.leftTable && this.rightTable ){
		
		this.leftHtmlCssId 	= this.id+"_l";
		this.rightHtmlCssId = this.id+"_r";
		this.leftTable.tableJoins.find( '.'+this.leftHtmlCssId ); 
		this.rightTable.tableJoins.find( '.'+this.rightHtmlCssId ); 
		
		var leftHeader 		= StringUtils.isNotBlank( this.rightTable.dataProvider.tableAlias ) ? this.rightTable.dataProvider.tableAlias :  this.rightTable.dataProvider.tableName; 
		this.leftHtml 		= this.getHtml( leftHeader + ' join' , this.leftColumnsReadOnly );
		this.leftHtml.addClass( this.leftHtmlCssId );
		this.leftRows		= this.leftHtml.find( '.table-join-body' );
		
		var rightHeader 		= StringUtils.isNotBlank( this.leftTable.dataProvider.tableAlias ) ? this.leftTable.dataProvider.tableAlias :  this.leftTable.dataProvider.tableName;
		this.rightHtml 			= this.getHtml( rightHeader + ' join' , this.rightColumnsReadOnly );
		this.rightHtml.addClass( this.rightHtmlCssId );
		this.rightRows			= this.rightHtml.find( '.table-join-body' );
		
		if( this.leftTable.tableJoins.find( '.'+this.leftHtmlCssId ).length  ){
			this.leftTable.tableJoins.find( '.'+this.leftHtmlCssId ).replaceWith( this.leftHtml );
		} else {
			this.leftTable.tableJoins.append( this.leftHtml ); 
		}
		if( this.rightTable.tableJoins.find( '.'+this.rightHtmlCssId ).length  ){
			this.rightTable.tableJoins.find( '.'+this.rightHtmlCssId ).replaceWith( this.rightHtml );
		} else {
			this.rightTable.tableJoins.append( this.rightHtml ); 
		}
		
		if( joinSettings && joinSettings.columns && joinSettings.columns.length > 0 ){

			for( var i in joinSettings.columns ){
				var col = joinSettings.columns[i];
				this.addRow(col.left, col.right);
			}
			
		} else {
			// append empty row
			this.addRow();
		}
		
		this.leftTable.table.on( 'scroll' , $.proxy(this.updateConnections, this) );
		this.rightTable.table.on( 'scroll' , $.proxy(this.updateConnections, this) );
		
	}
};
ERDTableJoin.prototype.updateConnections	= function( ){
	EventBus.dispatch( 'calc.sampling-design.update-connections', null , this.id );
};
ERDTableJoin.prototype.disconnect	= function(){
	this.reset();
	if( this.leftTable ){
		this.leftTable.tableJoins.find( '.'+this.leftHtmlCssId ).empty().hide();
	}
	if( this.rightTable ){
		this.rightTable.tableJoins.find( '.'+this.rightHtmlCssId ).empty().hide();
	}
	this.leftTable = this.rightTable = this.leftHtml = this.rightHtml = null;
};
ERDTableJoin.prototype.setEditMode = function(editMode){
	this.editMode = editMode;
	if( this.leftHtml && this.rightHtml ){
		
		if( this.editMode === true ){
			UI.enable( this.leftHtml.find( 'input:not(.read-only)' ) );
			UI.enable( this.rightHtml.find( 'input:not(.read-only)' ) );
			this.leftHtml.find( 'button[name=add-btn],button[name=delete-btn],span').visible() ;
			this.rightHtml.find( 'button[name=add-btn],button[name=delete-btn],span').visible() ;
			this.highlight();
		} else {
			UI.disable( this.leftHtml.find( 'input:not(.read-only)' ) );
			UI.disable( this.rightHtml.find( 'input:not(.read-only)' ) );
			this.leftHtml.find( 'button[name=add-btn],button[name=delete-btn],span').invisible() ;
			this.rightHtml.find( 'button[name=add-btn],button[name=delete-btn],span').invisible() ;
		}
		
	}
};

ERDTableJoin.prototype.getHtml = function( header , readOnly ){
	 var div = 
		 $( '<div class="row table-join-row">'+
				 '<div class="col-md-12 table-join">'+
				 	'<div class="row row-table-join-header">'+
				 		'<div class="col-md-3 table-join-header  text-center">'+
				 			'<button type="button" class="btn no-background" name="add-btn">'+
				 				'<i class="fa fa-plus-circle"></i>'+
				 			'</button>'+
				 		'</div>'+
				 		'<div class="col-md-9 table-join-header no-padding">'+
				 		header +
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
	 if( this.multiple === true && !readOnly ){

		 addBtn.click( $.proxy(function(){
			 this.addRow();
		 } , this) );
		 
	 } else {
		 addBtn.remove();
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
	
	EventBus.dispatch( 'calc.sampling-design.update-connections', null );
	
	this.rowsChange();
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
				
//				this.updateConnections();
				EventBus.dispatch( 'calc.sampling-design.update-connections', null );
			} , this ) , 140 );
			
			this.rows.splice( i, 1 );
			
			this.rowsChange();
			
			break;
		}
	}
};

ERDTableJoin.prototype.rowsChange = function (){
	if( this.rows.length == 4 ){
	 	UI.disable( this.leftHtml.find( 'button[name=add-btn]' ) );
	 	UI.disable( this.rightHtml.find( 'button[name=add-btn]' ) );
	} else {
		UI.enable( this.leftHtml.find( 'button[name=add-btn]' ) );
		UI.enable( this.rightHtml.find( 'button[name=add-btn]' ) );
	}
	
	Utils.applyFunction( this.onChange );
}

ERDTableJoin.prototype.jsonSettings = function(){
	var settings = {};
	if( this.leftTable && this.rightTable && this.leftTable.dataProvider.getTableInfo() && this.rightTable.dataProvider.getTableInfo() ){
		
		var leftInfo 	= this.leftTable.dataProvider.getTableInfo();
		var rightInfo 	= this.rightTable.dataProvider.getTableInfo();
		
		settings.leftTable 			= {};
		settings.leftTable.table 	= leftInfo.fields.table;
		settings.leftTable.schema 	= leftInfo.fields.schema;
		settings.rightTable 		= {};
		settings.rightTable.table 	= rightInfo.fields.table;
		settings.rightTable.schema 	= rightInfo.fields.schema;
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

ERDTableJoin.prototype.highlight = function(){
	if( this.leftHtml ){
		
		this.leftHtml.stop().animate( {backgroundColor : 'rgba(106, 145, 111, 0.2)'}, 500 , $.proxy(function(){
			this.leftHtml.animate( {backgroundColor : 'rgba(210, 217, 249, 0.02)'}, 600);
		}, this) );
		
	}
	if( this.rightHtml ){
		
		this.rightHtml.stop().animate( {backgroundColor : 'rgba(106, 145, 111, 0.2)'}, 500 , $.proxy(function(){
			this.rightHtml.animate( {backgroundColor : 'rgba(210, 217, 249, 0.02)'}, 600);
		}, this) );
		
	}
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
		$this.tableJoin.rowsChange();
	};
	var onChangeRight = function( val ){
		$this.rightColumn = val;
		$this.tableJoin.rowsChange();
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
	var col 	= $( '<div class="col-md-10"><span class="anchor '+cssClass+'_c ' + anchorCssClass +'"/></div>' );
	row.append( col );
	
	var select 	= $( '<select class="form-control">' );
	col.append( select );
	
	var combo   = select.combobox();
	combo.data( columns, 'column_name','column_name' );
	combo.change( function(){
		onChange( combo.val() );
	});
	if(value){
		combo.val( value , true );
	}
	if( readOnly === true ){
		combo.disable();
		combo.addClass( 'read-only' );
	}
	if( this.tableJoin.multiple === true ) {
		
		var colDeleteBtn 	= $( '<div class="col-md-1 no-padding"></div>' );
		row.append( colDeleteBtn );
		var deleteBtn 		= $( '<button type="button" class="btn no-background" name="delete-btn"><i class="fa fa-times-circle"></i></button>' );
		colDeleteBtn.append( deleteBtn );
		deleteBtn.click( $.proxy( function(e){
			e.preventDefault();
			this.tableJoin.deleteRow(this);
		} , this ) );

	}
	
	return row;
};