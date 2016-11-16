/**
 * Equation list manager 
 * @author Mino Togna
 */

AuxiliaryTablesManager = function( container ) {
	this.BASE_URI = "rest/workspace/active/auxiliary-table";
	
	/* UI components */
	this.container = $( container );
	// currently selected table
	this.activeTable 	= null;
	// View section
	this.viewManager 	= new AuxiliaryTablesViewManager( this.container.find( ".view-section" ) , this );
	// Edit section
	this.editManager 	= new AuxiliaryTablesEditManager( this.container.find( ".import-section" ), this );
	
	this.init();
};


AuxiliaryTablesManager.prototype.init = function() {
	var $this = this;
	
	this.formFileUpload = new FormFileUpload(
			this.container.find( ".upload-csv-form-section" ), 
			null, 
			function ( response ) {
				$this.showEdit( response.fields.filepath, response.fields.headers );
			}		
	);
	
	this.showView( true );
};

AuxiliaryTablesManager.prototype.showView = function( reloadTables ){
	this.editManager.hide();
	this.viewManager.show( reloadTables );
}

AuxiliaryTablesManager.prototype.showEdit = function(file , headers){
	this.viewManager.hide();
	
	var id = ( this.activeTable ) ? this.activeTable.id : null ;
//	console.log( id );
	this.editManager.show(id , file , headers);
}