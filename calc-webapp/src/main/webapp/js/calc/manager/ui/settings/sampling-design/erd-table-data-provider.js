/**
 * Data Provider for ERD table
 */
ERDTableDataProvider = function(){
	this._tableInfo = null;
};

ERDTableDataProvider.prototype.getTableInfo = function( ){
	return this._tableInfo;
};

ERDTableDataProvider.prototype.setTableInfo = function( tableInfo ){
	this._tableInfo = tableInfo;
};

/**
 *Entity Data provider 
 * @returns
 */
EntityDataProvider = function( onChange ){
	ERDTableDataProvider.call( this );
	
	this.onChange = onChange;
	
	this._entityId = null;
};

EntityDataProvider.prototype 				= Object.create(ERDTableDataProvider.prototype);
EntityDataProvider.prototype.constructor 	= EntityDataProvider;

EntityDataProvider.prototype.setEntityId = function( entityId ){
	this._entityId 	= entityId;
	if( this._entityId ){
		WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
			var entity = ws.getEntityById( this._entityId );
			
			var tableInfo = function(){
				this.table 			= entity.name;
				this.fields 		= {};
				this.fields.table 	= entity.name;
				this.fields.columns = [];
				
				var vars = entity.hierarchyVariables();
				for( var i in vars ){
					var variable = vars[ i ];
					this.fields.columns.push( { 'column_name' : variable.name } );
				}
			};
			
			this.setTableInfo( new tableInfo() );
		} , this ) );
	} else {
		this.setTableInfo( null );
	} 
};

EntityDataProvider.prototype.createCombo = function( select ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		
		var selectCombo = $( select ).combobox();
		selectCombo.data( ws.entities, 'id','name' );
		
		if( $this._entityId !== undefined && $this._entityId !== null ){
			selectCombo.val( $this._entityId );
		}
		selectCombo.change( function(e){
			Utils.applyFunction( $this.onChange, selectCombo.val() );
		});
		
	});
};

/**
 * CSV file upload data provider
 * @returns
 */
CsvFileDataProvider = function(uploadCallback , selectColumnsToImport){
	ERDTableDataProvider.call( this );
	
	this.uploadCallback 			= uploadCallback;
	this.selectColumnsToImport 	= ( selectColumnsToImport === true );
	
	this.tableAlias = '';
	
	this.tableName 	= '';
	this.extSchema 	= false;
};
CsvFileDataProvider.prototype				= Object.create(ERDTableDataProvider.prototype);
CsvFileDataProvider.prototype.constructor 	= CsvFileDataProvider;

CsvFileDataProvider.prototype.initUploadForm	= function( formContainer ){
	var callback		= this.selectColumnsToImport ? $.proxy( this.showImportColumns, this ) : this.uploadCallback;
	this.formFileUpload = new FormFileUpload( $(formContainer), null, callback );
};

CsvFileDataProvider.prototype.showImportColumns = function( response ){
	var path = response.fields.filepath;
	var cols = response.fields.headers;

	var modalDiv 		= $( '#csv-import-column-selector' );
	var importer   		= new CsvTableImport( modalDiv, path, cols , this.tableName );
	importer.extSchema 	= this.extSchema;
	
	importer.import( $.proxy( function(schema , table ){
		modalDiv.modal( 'hide' );
		Utils.applyFunction(this.uploadCallback , schema , table )
	} , this ) );
	
	modalDiv.modal( 'show' );
};