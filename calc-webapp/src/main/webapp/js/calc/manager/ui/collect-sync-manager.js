/**
 * Manages Collect sync page
 */
CollectSyncManager = function(container) {
	
	//main container
	this.container 			= $(container);
	
	//upload form container
	this.formSection 		= this.container.find(".upload-form-section");
	//upload progress container
	this.progressSection 	= this.container.find(".upload-progress-section");
	
	//form file upload manager (to be initialized in the init method)
	this.formFileUpload = null;
	
	this.init();
};

CollectSyncManager.prototype.init = function () {
	
	//file upload success handler
	var uploadSuccess = function ( response ) {
		if ( response.status == "OK" ) {
			var job = response.fields.job;
			
			JobManager.getInstance().start( job , function() {
				Calc.workspaceChange();
			});
		} else {
			var errors = response.errors;
			var message = UI.Form.getFieldErrorMessage(errors);
			UI.showError(message);
		}
	};
	//form file upload manager
	this.formFileUpload = new FormFileUpload(this.formSection, this.progressSection, uploadSuccess);
	this.formFileUpload.showHideForm = false;
	
};