/**
 * 
 */
ERDTable = function( parent , dataProvider ){
	this.parent 		= parent;
	this.dataProvider 	= dataProvider;
	
	// editable properties
	this.tableName = '';
	
	// init html based on dataProvider
	this.update();
};

ERDTable.prototype.update = function(){
	this.parent.empty();
	
	var html = $( '<div class="row height100 erd-table"><div class="col-md-12 container height100"></div></div>' );
	this.parent.append( html );
	
	var htmlHeader = $( '<div class="row height10 row-header"><div class="col-md-12 header"></div></div>' );
	html.find( '.container' ).append( htmlHeader );
	var htmlBody = $( '<div class="row height90 row-body"><div class="col-md-10 col-md-offset-1 body"></div></div>' );
	html.find( '.container' ).append( htmlBody );
	
	this.table 	= html;
	this.header = html.find( '.header' );
	this.body 	= html.find( '.body' );
	
	this.tableNameHtml = $( '<div class="table-name text-center"></div>' );
	this.header.append( this.tableNameHtml );
	
	if( this.dataProvider instanceof CsvFileDataProvider ){
		
		this.tableNameHtml.addClass( 'width60' );
		
		this.uploadCsvHtml 	= 
			$( '<div class="button-container upload-form-container width20 text-center">'+
					'<form  method="post" enctype="multipart/form-data" action="rest/csv-upload.json">'+
						'<button type="button" class="btn btn-default" name="upload-btn"><i class="fa fa-upload"></i></button>'+
					    '<input type="file" name="file" style="display:none" />'+
					'</form>'+
				'</div>');
//		this.uploadCsvHtml 	= $( uploadCsvHtmlFormContainer );
		this.header.append( this.uploadCsvHtml );
		
		this.viewDataHtml 	= $( '<div class="button-container width20 text-center"><button type="button" class="btn btn-default" name="view-btn"><i class="fa fa-table"></i></button></div>' );
		this.viewDataButton = this.viewDataHtml.find( 'button[name=view-btn]' );
		this.header.append( this.viewDataHtml );
		if( this.dataProvider.getTableInfo() !== null){
			UI.enable( this.viewDataButton );
		} else {
			UI.disable ( this.viewDataButton );
		}
		
		this.dataProvider.initUploadForm( this.uploadCsvHtml );
		
		this.setTableName( this.dataProvider.tableAlias );
		
	} else if( this.dataProvider instanceof EntityDataProvider ){
		
		this.tableNameHtml.addClass( 'width100' );
		
		this.tableNameSelectorHtml = $( '<div class="width100"></div>' );
		this.header.append( this.tableNameSelectorHtml  );
		
		var select = $( '<select class="form-control width100"></select>' );
		this.tableNameSelectorHtml.append( select );
		this.dataProvider.createCombo( select );
		
	}
	
	this.setEditMode( false );
};

ERDTable.prototype.setEditMode = function( editMode ){
	
	if( this.dataProvider instanceof CsvFileDataProvider ){ 
		this.setCsvUpladEditMode( editMode );
	} else if( this.dataProvider instanceof EntityDataProvider ){
		this.setEntityEditMode( editMode );
	}
	
	if( editMode === true ){
		this.highlight();
	}
	
};

ERDTable.prototype.highlight = function(){
	this.table.stop().animate( {backgroundColor : 'rgba(51, 157, 166, 0.2)'}, 400 , $.proxy(function(){
		this.table.animate( {backgroundColor : 'transparent'}, 600);
	}, this) );
};

ERDTable.prototype.setEntityEditMode = function( editMode ){
	var wsManager = WorkspaceManager.getInstance();
	
	wsManager.activeWorkspace( $.proxy( function(ws){ 
		
		if( editMode === true ){
			
			this.tableNameSelectorHtml.show();
			this.tableNameHtml.hide();
			
		} else {
			var tableInfo = this.dataProvider.getTableInfo() ;
			var tableName = ( tableInfo ) ? tableInfo.fields.table : '';
			this.setTableName(tableName);
			
			this.tableNameSelectorHtml.hide();
			this.tableNameHtml.show();

		}
		
	} , this ) );
};


ERDTable.prototype.setCsvUpladEditMode = function( editMode ){
	if( editMode === true ){
		this.uploadCsvHtml.visible();
	} else {
		this.uploadCsvHtml.invisible();
	}
};

ERDTable.prototype.setTableName = function( tableName ){
	this.tableNameHtml.empty();
	this.tableName = tableName;
	this.tableNameHtml.html( this.tableName );
};
	
	
