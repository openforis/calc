/**
 * manager for Strata settings page
 * @author Mino Togna
 */

StratumManager = function(container , sdManager) {
	
	this.container = $(container);
	this.sdManager = sdManager;
	
	// upload csv ui components
	this.uploadSection = this.container.find(".upload-section");
	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	
	this.tableColumnSelector = new TableColumnSelector( this.container.find(".table-column-selector") );
	
	this.init();
};

StratumManager.prototype.init = function(){
	var $this = this;

	// upload csv form methods 
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	$this.import( response.fields.filepath );
	    },
	    error: function (e) {
			Calc.error.apply( this , arguments );
	    	UI.unlock();
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
	    }
	});	
	
	this.uploadBtn.click(function(event) {
		event.preventDefault();
		$this.file.click();
	});
	
	this.file.change(function(event) {
		event.preventDefault();
		$this.form.submit();
	});
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
		
		var tableInfo = ( this.sdManager.samplingDesign.twoPhases === true ) ? this.sdManager.phase1TableInfo : this.sdManager.samplingUnitTableInfo;
		var header = ( this.sdManager.samplingDesign.twoPhases === true ) ? "phase 1 table join column" : this.sdManager.samplingUnitTableInfo.table + " table join column";
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
