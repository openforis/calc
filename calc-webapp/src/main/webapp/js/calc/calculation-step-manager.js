/**
 * 
 * Manages the editing of the CalculationStep
 *  
 */
function CalculationStepManager($form) {
	
	this.$form = $form;
	this.$entityCombo = $form.find("[name='entityId']").combobox();
	this.$variableCombo = $form.find("[name='variableId']").combobox();
	this.$addVariableButton = $form.find("[name=add-variable]");
	this.currentCalculationStep = null;

	this.workspaceManager = WorkspaceManager.getInstance();
	
	//initialized in the init method
	//R script component manager
	this.$RScript = null;
	
	this._init();
}

CalculationStepManager.prototype = (function() {
	
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
				$this.$RScript.selectedEntity = entity;
			});
		});
		
		// on submit button click 
		$this.$form.find("button[type='submit']").click(function(event){
			event.preventDefault();
			$.proxy(save, $this)(function(){
				UI.Form.showResultMessage("Calculation step successfully saved.",true);
			});
		});
	};
	
	var loadStepAndUpdateForm = function(stepId) {
		var $this = this;
		CalculationStepManager.load(stepId, function(response) {
			$this.currentCalculationStep = response;
			$.proxy(updateForm, $this)();
			$.proxy(getSelectedEntity, $this)(function(entity) {
				$this.$RScript.selectedEntity = entity;
			});
		});
	};
	
	/**
	 * Execute the calculation step with the specified id and call the callback function 
	 * 
	 * @param id
	 * @param callback
	 */
	var execute = function(id, totalItems, callback) {
		var params = {};
		if( !isNaN(totalItems) ){ 
			params.totalItems = totalItems; 
		}

		$.ajax({
			url:"rest/calculationstep/"+id+"/run.json",
			dataType:"json",
			data: params
//			,
//			async: false 
		})
		.done(function(response){
			callback(response);
		});
//		.error(function(e){
//			console.log("error!!! on exec");
//			console.log(e);
//		});
	};
	
	/**
	 * Create or update the calculation step according to the field values in the form 
	 */
	var save = function(successCallback, errorCallback) {
		UI.lock();
		var $this = this;
		var $step = $this.$form.serialize();
		$.ajax({
			url: "rest/calculationstep/save.json",
			dataType: "json",
			data: $step,
			type: "POST"
		})
		.done(function(response) {
	    	UI.Form.updateErrors($this.$form, response.errors);
	    	if(response.status == "ERROR" ) {
	    		UI.Form.showResultMessage("There are errors in the form. Please fix them before proceeding.",false);
	    		
	    		if ( errorCallback ) {
	    			errorCallback();
	    		}
	    	} else {
	    		$this.currentCalculationStep = response.fields.calculationStep;
	    		$.proxy(updateForm, $this)();
		    	
	    		CalculationStepManager.refreshHome($this.currentCalculationStep);
		    	
	    		if(successCallback) {
		    		successCallback($this.currentCalculationStep);
	    		};
	    	}
		})
		.error(function(e) {
	    	UI.Form.showResultMessage("An error occured. Please check the log file.",false );
	
			if ( errorCallback ) {
				errorCallback(e);
			}
		})
		.complete(function() {
			UI.unlock();
		});
		
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
		constructor : CalculationStepManager,
		
		//public methods
		_init : init,
		refreshVariableSelect : refreshVariableSelect,
		getSelectedEntityId : getSelectedEntityId,
		getSelectedEntity : getSelectedEntity,
		save : save,
		execute : execute
	};
})();

/**
 * Load all the calculation steps associated to the default processing chain in the active workspace
 * and call the specified callback function
 *  
 * @param callback
 */
CalculationStepManager.loadAll = function(callback) {
	$.ajax({
		url:"rest/calculationstep/load.json",
		dataType:"json"
	}).done(function(response){
		callback(response);
	});
};

/**
 * Load the calculation step with the specified id and call the callback function
 * 
 * @param id
 * @param callback
 */
CalculationStepManager.load = function(id, callback) {
	$.ajax({
		url:"rest/calculationstep/"+id+"/load.json",
		dataType:"json"
	})
	.done(function(response){
		if ( callback ) {
			callback(response);
		}
	});
};

/**
 * Delete the calculation step with the specified id
 * 
 * @param id
 * @param callback
 */
CalculationStepManager.deleteStep = function(id, callback) {
	$.ajax({
		url:"rest/calculationstep/"+id+"/delete.json"
	})
	.done(function(response){
		if ( callback ) {
			callback(response);
		}
	});
};



/**
 * Refresh the home page updating the element related to the specified step or
 * reloading all the step elements
 * 
 * @param callback
 * @param $step
 */
CalculationStepManager.refreshHome = function($step, callback) {
	var $calculationContainer = $("#calculation");
	if ( $step ) {
		//update specified step
		var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
		if ( $el.length == 0 ) {
			CalculationStepManager._addStepToHome($step);
		} else {
			$el.text($step.caption);
		}
		if ( callback ) {
			callback();
		}
	} else {
		//update all steps
		var $stepElContainer = $calculationContainer.find('.steps-container');
		var $stepElems = $stepElContainer.find(".step");
		$stepElems.remove();
		
		CalculationStepManager.loadAll(function(response){
			var $steps = response;
			$.each($steps, function(i, $step){
				CalculationStepManager._addStepToHome($step);
			});
			if ( callback ) {
				callback();
			}
		});
	}
};

/**
 * Private function: create a home page calculation step element and add it to the dom
 *  
 * @param $step
 */
CalculationStepManager._addStepToHome = function($step) {
	var $calculationContainer = $("#calculation");
	
	//create button from template
	var $stepEl = $calculationContainer.find(".calculation-button.template").clone();
	$stepEl.removeClass("template");
	$stepEl.attr("id", "calculation-step-el-" + $step.id);
	
	$stepEl.text($step.caption);
	$stepEl.attr("href","step-edit.html?id="+$step.id);
	
	$stepEl.click(function(event) {
		homeButtonClick(event);
	});
	/*
	var mouseDownStartDate = null;
	var timer = null;
	var LONG_PRESS_DURATION = 1000;
	$button.mousedown(function(event) {
		mouseDownStartDate = new Date();
		timer = window.setTimeout(function() {
			var $stepOptions = $stepEl.find(".options");
			$stepOptions.removeClass("hide");
		}, LONG_PRESS_DURATION);
	});
	$button.mouseup(function(event) {
		var elapsedMillis = mouseDownStartDate == null ? 0: new Date().getTime() - mouseDownStartDate.getTime();
		if ( elapsedMillis < LONG_PRESS_DURATION ) {
			if ( timer != null ) {
				window.clearTimeout(timer);
			}
			homeButtonClick(event);
		}
	});
	var $deleteButton = $stepEl.find(".delete");
	$deleteButton.click(function(event) {
		if ( confirm("Delete the step '" + $step.caption + "' ?") ) {
			CalculationStepManager.deleteStep($step.id, function() {
				CalculationStepManager._removeStepFromHome($step);
			});
		}
	});
	*/
	$stepEl.css("display", "inline-block");
	
	var $stepElContainer = $calculationContainer.find('.steps-container');
	$stepElContainer.append($stepEl);
	return $stepEl;
};

CalculationStepManager._removeStepFromHome = function($step) {
	var $calculationContainer = $("#calculation");
	var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
	$el.remove();
};
