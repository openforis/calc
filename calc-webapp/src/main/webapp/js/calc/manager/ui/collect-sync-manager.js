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
	var uploadSuccess = function ( job ) {
	    JobManager.getInstance().start( job , function() {

	    	WorkspaceManager.getInstance().refreshActiveWorkspace(function(ws) {
				Calc.workspaceChange();
	    	});
	    	
	    });
	};
	//form file upload manager
	this.formFileUpload = new FormFileUpload(this.formSection, this.progressSection, uploadSuccess);
	
};