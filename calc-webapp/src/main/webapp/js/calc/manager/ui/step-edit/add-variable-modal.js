/**
 * 
 */

AddVariableModal = function( triggerButton , editManager ) {
	// calculation step edit manager instance
	this.calcStepEditManager = editManager;
	// button that triggers the opening of the add variable modal form
	this.triggerButton = triggerButton;
	// modal container
	this.container = $( '#add-variable-modal' );
	// form to submit
	this.form = this.container.find('form');
	// save button form
	this.saveButton = this.container.find('.save');

	this.init();
};

AddVariableModal.prototype.init = function() {
	var $this = this;
	
	//submits the add variable form
	this.form.submit( function(event) {
		event.preventDefault();
		UI.lock();
		var variable = UI.Form.toJSON( $this.form );
		
		var successCallback = function(response) {
			UI.Form.updateErrors( $this.form, response.errors );
			
	    	if(response.status == "ERROR" ) {
	    		var errors = response.errors;
	    		var errorMessage = UI.Form.getFormErrorMessage( $this.form, errors );
	    		UI.showError( errorMessage, true );
	    	} else {
	    		var variable = response.fields.variable;
	    		
	    		$this.calcStepEditManager.updateVariableSelect();
	    		$this.calcStepEditManager.$variableCombo.val( variable.id );
	    		
	    		$this.container.modal('hide');
	    		$this.container.modal('removeBackdrop');
	    		
	    		Calc.homeDataManager.refresh();
	    	}
		};
		var errorCallback = function (e) {
	    	UI.showError("An error occured. Please check the log file.", false);
		};
		var completeCallback = function() {
			UI.unlock();
		};
		
		WorkspaceManager
			.getInstance()
			.activeWorkspaceAddQuantitativeVariable( variable, successCallback, errorCallback, completeCallback );
	});
	
	//add variable button click
	$this.triggerButton.click(function(event){
		event.preventDefault();
		
		UI.Form.reset( $this.form );
		
		//set entityId hidden field value
		var selectedEntityId = $this.calcStepEditManager.getSelectedEntityId();
		$this.form.find( "[name=entityId]" ).val( selectedEntityId );
		// open the modal 
		$this.container.modal( {keyboard: true, backdrop: "static"} );
		
		//set focus on first field in form
		setTimeout(function() {
			UI.Form.setFocus( $this.form );	
		}, 500);
	});
	
	// add variable form: on save button click, it submits the form
	$this.saveButton.click(function(event) {
		event.preventDefault();
		$this.form.submit();
	});

};