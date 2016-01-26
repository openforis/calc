/**
 * manager for Strata settings page
 * @author Mino Togna
 */

StratumManager = function(container , sdManager) {
	
	this.container = $(container);
	this.sdManager = sdManager;
	
	// upload csv ui components
	this.uploadSection = this.container.find(".upload-section");
	
	this.tableColumnSelector = new TableColumnSelector( this.container.find(".table-column-selector") );
	
	//form file upload manager (to be initialized in the init method)
	this.formFileUpload = null;
	
	this.init();
};

StratumManager.prototype.init = function(){
	var $this = this;

	//file upload success handler
	var uploadSuccess = function ( response ) {
		$this.import( response.fields.filepath );
	};
	
	//form file upload manager
	this.formFileUpload = new FormFileUpload(this.uploadSection, null, uploadSuccess);
};

StratumManager.prototype.show = function() {
	this.container.fadeIn(200);
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
		this.updateStrata(ws);
	} , this ) );
};

StratumManager.prototype.hide = function() {
	this.container.hide();
};

StratumManager.prototype.import = function(filepath) {
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspaceImportStrata(filepath, function(ws){
		UI.unlock();
		UI.showSuccess( ws.strata.length +" strata successfully imported", true);
		$this.updateStrata(ws);
	});
	
};

StratumManager.prototype.updateStrata = function(ws) {
	if( ws.strata && ws.strata.length > 0 ) {
		
		var tableInfo 	= null;
		var header 		= null;
		if(  this.sdManager.samplingDesign.twoPhases === true  ){
			tableInfo 	= this.sdManager.phase1TableInfo;
			header 		= "phase 1 table join column";
		} else if(  this.sdManager.samplingDesign.twoStages === true  ){
			tableInfo 	= this.sdManager.primarySUTableInfo;
			header 		= "PSU table join column";
		} else {
			tableInfo 	= this.sdManager.samplingUnitTableInfo;
			header		= this.sdManager.samplingUnitTableInfo.table + " table join column";	
		}

		this.tableColumnSelector.setTableInfo( tableInfo , header);
		
		var options = this.sdManager.samplingDesign.stratumJoinSettings; 
		if( options ){
//			this.stratumManager.setJoinOptions( this.sdManager.samplingDesign.stratumJoinSettings );
			this.tableColumnSelector.settings = options;
		}
		this.tableColumnSelector.show();
	} else {
		this.tableColumnSelector.hide();

	}
};

StratumManager.prototype.validate = function() {
	if( this.tableColumnSelector.joinColumn && this.tableColumnSelector.joinColumn !== "" ) {
		return true;
	} else {
		UI.showError("Join column must be specified", false);
		return false;
	}
};

StratumManager.prototype.joinOptions = function() {
	return this.tableColumnSelector.jsonSettings();
};

StratumManager.prototype.setJoinOptions = function(options){
	this.tableColumnSelector.settings = options;
};
