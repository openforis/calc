/**
 * 
 * Manages the editing of the CalculationStep
 *  
 */
function CalculationStepEditManager(container) {
	this.container = container;
	this.$form = this.container.find('#step-form');
//	this.$form = $form;
	this.$entityCombo = this.$form.find("[name='entityId']").combobox();
	this.$variableCombo = this.$form.find("[name='variableId']").combobox();
	this.$addVariableButton = this.$form.find("[name=add-variable]");
	this.currentCalculationStep = null;

	this.calculationStepManager = CalculationStepManager.getInstance();
	this.workspaceManager = WorkspaceManager.getInstance();
	
	//initialized in the init method
	//R script component manager
	this.$RScript = null;
	
	// add variable ui elements
	this.$addVariableButton = this.$form.find("button[name='add-variable']");
	this.$addVariableModal = $('#add-variable-modal');
	this.$addVariableForm = this.$addVariableModal.find('form');
	this.$saveVariableButton = this.$addVariableModal.find('.save');
	
	
	this._init();
}

CalculationStepEditManager.prototype = (function() {
	
	/**
	 * Initialize the event handlers and populate the form with initial data
	 *  
	 * @param callback
	 */
	var init = function(callback) {
		UI.lock();
		var $this = this;
		
		var rScriptField = this.$form.find("[name=script]");
		$this.$RScript = new RScript(rScriptField);

		$.proxy(initEventHandlers, $this)();
	
		$.proxy(refreshEntitySelect, $this)(function() {
			var url = window.sectionUrl;
			var stepId = $.url(url).param("id");
			if ( stepId ) {
				$.proxy(loadStepAndUpdateForm, $this)(stepId);
			} else {
				UI.unlock();
			}
		});
		
	};
	
	/**
	 * Init input fields event listeners
	 */
	var initEventHandlers = function() {
		var $this = this;
		
		$this.$form.submit(function(event) {
			event.preventDefault();
		});
		
		//entity select change handler
		$this.$entityCombo.change(function(event) {
			$.proxy(refreshVariableSelect, $this)();
			$.proxy(getSelectedEntity, $this)(function(entity) {
				$this.$RScript.entity = entity;
			});
		});
		
		// on submit button click 
		$this.$form.find("button[type='submit']").click(function(event){
			event.preventDefault();
			$.proxy(save, $this)(function(){
				
				UI.showSuccess("Saved!",true);
			});
		});
		
		// at input change, it keeps track that the form has changed
		$this.$form.find(":input").change(function() {
			$this.$form.data('changed', true);
		});
		
		//add variable button click
		$this.$addVariableButton.click(function(event){
			event.preventDefault();
			
			UI.Form.reset($this.$addVariableForm);
			
			//set entityId hidden field value
			var selectedEntityId = $this.getSelectedEntityId();
			$this.$addVariableForm.find("[name=entityId]").val(selectedEntityId);
			
			$this.$addVariableModal.modal({keyboard: true, backdrop: "static"});
	 		UI.Form.setFocus($this.$addVariableForm);
		});
		
		// add variable form: on save button click, it submits the form
		$this.$saveVariableButton.click(function(event) {
			event.preventDefault();
			$this.$addVariableForm.submit();
		});
		//submits the add variable form
		$this.$addVariableForm.submit(function(event) {
			event.preventDefault();
			UI.lock();
			var variable = UI.Form.toJSON($this.$addVariableForm);
			
			var successCallback = function(response) {
				UI.Form.updateErrors($this.$addVariableForm, response.errors);
		    	if(response.status == "ERROR" ) {
		    		var errors = response.errors;
		    		var errorMessage = UI.Form.getFormErrorMessage($this.$addVariableForm, errors);
		    		UI.showError(errorMessage, true);
		    	} else {
		    		var variable = response.fields.variable;
		    		$this.refreshVariableSelect(variable.id);
		    		
		    		$this.$addVariableModal.modal('hide');
		    		$this.$addVariableModal.modal('removeBackdrop');
		    	}
			};
			var errorCallback = function (e) {
		    	UI.showError("An error occured. Please check the log file.", false);
			};
			var completeCallback = function() {
				UI.unlock();
			}
			$this.workspaceManager.activeWorkspaceAddQuantitativeVariable(variable, successCallback, errorCallback, completeCallback);
		});
		
	};
	
	var loadStepAndUpdateForm = function(stepId) {
		var $this = this;
		$this.calculationStepManager.load(stepId, function(response) {
			$this.currentCalculationStep = response;
			$.proxy(updateForm, $this)();
			$.proxy(getSelectedEntity, $this)(function(entity) {
				$this.$RScript.entity = entity;
			});
			//reset changed state 
			$this.$form.data('changed', false);
		});
	};
	
	/**
	 * Create or update the calculation step according to the field values in the form 
	 */
	var save = function(successCallback, errorCallback) {
		UI.lock();
		var $this = this;
		var $step = $this.$form.serialize();
		$this.calculationStepManager.save($step,
			//success
			function(response) {
		    	UI.Form.updateErrors($this.$form, response.errors);
		    	if(response.status == "ERROR" ) {
		    		UI.showError("There are errors in the form. Please fix them before proceeding.", true);
		    		
		    		if ( errorCallback ) {
		    			errorCallback();
		    		}
		    	} else {
		    		$this.currentCalculationStep = response.fields.calculationStep;
		    		$.proxy(updateForm, $this)();
			    	
		    		homeCalculationManager.refreshHome($this.currentCalculationStep);
			    	
		    		if(successCallback) {
			    		successCallback($this.currentCalculationStep);
		    		};
		    	}
			},
			//error
			function(e) {
		    	UI.showError("An error occured. Please check the log file.", false);
		
				if ( errorCallback ) {
					errorCallback(e);
				}
			},
			//complete
			function() {
				//reset changed state
				$this.$form.data('changed', false);
				UI.unlock();
			}
		);
	};
	
	/**
	 * Save the calculation step form only if it has changed, else calls the callback synchronously
	 */
	var saveIfChanged = function(success) {
		if( this.$form.data('changed') ) {
			$.proxy(save,this)(success);
		} else {
			success(this.currentCalculationStep);
		}
		
	};
	
	/**
	 * Update the form with values from the currentCalculationStep instance
	 * 
	 * @param callback
	 */
	var updateForm = function(callback) {
		var $this = this;
		var $step = $this.currentCalculationStep;
		
		$this.$entityCombo.val($step.outputEntityId);
		
		$.proxy(refreshVariableSelect, $this)($step.outputVariableId, function() {
			UI.Form.setFieldValues($this.$form, $step);
	
			UI.unlock();
	
			if ( callback ) {
				callback();
			}
		});
	};
	
	/**
	 * Returns the selected entity in the form
	 * 
	 * @returns
	 */
	var getSelectedEntityId = function() {
		var entityId = this.$entityCombo.val();
		return entityId;
	};
	
	var getSelectedEntity = function(callback) {
		var $this = this;
		var entityId = $this.getSelectedEntityId();
		if ( entityId ) {
			$this.workspaceManager.activeWorkspace(function(ws) {
				var entity = ws.getEntityById(entityId);
				callback(entity);
			});
		} else {
			callback(null);
		}

	};
	
	/**
	 * Populate the "entity" select
	 * 
	 * @param callback
	 */
	var refreshEntitySelect = function(callback) {
		var $this = this;
		$this.$variableCombo.reset();
		
		$this.workspaceManager.activeWorkspace(function(ws) {
			var entities = ws.entities;
			
			$this.$entityCombo.data(entities, "id", "name");
			
			if ( callback ) {
				callback(ws);
			}
		});
	};
	
	/**
	 * Populate the "output variable" select according to the selected parent entityId.
	 * 
	 * The option corresponding to the value specified will be selected.
	
	 * @param value Value to select in the select input control
	 * @param callback
	 */
	var refreshVariableSelect = function(value, callback) {
		var $this = this;

		$this.$variableCombo.reset();
		$this.$variableCombo.disable();
		
		UI.disable($this.$addVariableButton);
		
		var entityId = $this.getSelectedEntityId($this.$form);
		if ( entityId ) {
			$this.workspaceManager.activeWorkspace(function(ws) {
				var entity = ws.getEntityById(entityId);
				var variables = entity.quantitativeVariables;
				
				$this.$variableCombo.data(variables, "id", "name");
				$this.$variableCombo.val(value);
				$this.$variableCombo.enable();

				UI.enable($this.$addVariableButton);
				
				if ( value ) {
					$this.$variableCombo.val(value);
				}
				if ( callback ) {
					callback();
				}
			});
		} else {
			$this.$variableCombo.reset();
		}
	};
	
	//prototype
	return {
		constructor : CalculationStepEditManager,
		
		//public methods
		_init : init,
		refreshVariableSelect : refreshVariableSelect,
		getSelectedEntityId : getSelectedEntityId,
		getSelectedEntity : getSelectedEntity,
		save : save,
		saveIfChanged : saveIfChanged
	};
})();
