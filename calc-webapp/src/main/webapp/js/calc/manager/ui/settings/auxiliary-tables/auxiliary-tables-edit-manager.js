/**
 * @author M. Togna
 */
AuxiliaryTablesEditManager = function( container, manager ) {
	this.container 	= $( container );
	this.manager 	= manager;
	this.BASE_URI 	= this.manager.BASE_URI;
	
	this.init();
}

AuxiliaryTablesEditManager.prototype.init = function(){
	this.name 		= this.container.find("[name=name]");
	
	this.filePath 	= "";
	
	this.cancelBtn 	= this.container.find("[name=cancel-btn]");
	
	var $this = this;
	this.cancelBtn.click(function(e){
		e.preventDefault();
		$this.manager.showView();
	});
	
}

AuxiliaryTablesEditManager.prototype.show = function(tableId , file , columns) {
	this.container.fadeIn();
	
	this.tableId		= tableId;
	var readOnlyName	= ( this.tableId ) ? true : false;
	this.name.prop( "readonly" , readOnlyName );
	var name 			= ( this.manager.activeTable ) ? this.manager.activeTable.name : '';
	this.name.val( name );
	
	this.file 			= file;
	this.columns 		= columns;
	
	this.importer 					= new CsvTableImport( this.container, this.file, this.columns , this.name.val() );
	this.importer.extSchema			= true;
	this.importer.beforeImport		= $.proxy( this.validate , this );
	this.importer.importCallback 	= $.proxy( this.save , this );
}

AuxiliaryTablesEditManager.prototype.hide = function() {
	this.container.hide();
}

AuxiliaryTablesEditManager.prototype.validate = function() {
	var valid = true;
	
	var regex = /^[a-z][a-z0-9_]*/;
	var name = $.trim( this.name.val() );
	if( name == "" ) {
		valid = false;
		UI.showError( "Table name cannot be blank" , true );
	} else if( !regex.test(name) || name.includes(" ") ){
		valid = false;
		UI.showError( "Table name must start with a character and must contain only lowercase characters, numbers and underscores" , true );
	} else {
		this.importer.tableName = name;
	} 
	
	return valid;
}

AuxiliaryTablesEditManager.prototype.save = function(schema , table){
	var $this = this;
	UI.lock();

	var success = function(response){

		UI.unlock();
		
		if( response.status == "OK" ){
			UI.showSuccess( "Auxiliary table succesfully imported", true);
			
			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				ws.auxiliaryTables = response.fields.auxiliaryTables;
				$this.manager.showView( true );
			});
		} else {
			UI.showError( response.errors[0].defaultMessage , true );
		}
	}
	
	var params = {
			url 	: this.BASE_URI + "/save.json"
			,data	: { schema:schema, name:table, tableId:this.tableId }
			,success : success
	}
	EventBus.dispatch("ajax.post", null , params);
}