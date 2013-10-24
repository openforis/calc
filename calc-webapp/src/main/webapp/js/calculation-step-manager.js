/**
 * 
 * Manages the editing of the CalculationStep
 *  
 */
function CalculationStepManager($form) {
	
	this.$form = $form;
	this.$entitySelect = $form.find("[name='entityId']");
	this.$variableSelect = $form.find("[name='variableId']");
	this.$addVariableButton = $form.find("[name=add-variable]");
	
	this.currentCalculationStep = null;
	
	this.init();
}

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
		callback(response);
	});
};

/**
 * Load all the calculation steps and populate the container in the home page
 * 
 * @param callback
 */
CalculationStepManager.updateHomePage = function(callback) {
	var $calculationContainer = $("#calculation");
	var $stepElContainer = $calculationContainer.find('.button-container');
	var $stepElems = $stepElContainer.find(".calculation-button");
	$stepElems.remove();
	
	CalculationStepManager.loadAll(function(response){
		var $steps = response;
		$.each($steps, function(i, $step){
			CalculationStepManager._addHomePageStepElement($step);
		});
	});
};

/**
 * Update only the element in the home page corresponding to the specified step
 * 
 * @param $step
 */
CalculationStepManager.updateHomePageStepElement = function($step) {
	var $calculationContainer = $("#calculation");
	var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
	if ( $el.length == 0 ) {
		CalculationStepManager._addHomePageStepElement($step);
	} else {
		$el.html($step.caption);
	}
};

/**
 * Private function: create a home page calculation step element and add it to the dom
 *  
 * @param $step
 */
CalculationStepManager._addHomePageStepElement = function($step) {
	var $calculationContainer = $("#calculation");
	
	//create button from template
	var $button = $calculationContainer.find(".calculation-button.template").clone();
	$button.removeClass("template");
	$button.attr("id", "calculation-step-el-" + $step.id);
	$button.html($step.caption);
	$button.attr("href","step-edit.html?id="+$step.id);
	$button.click(homeButtonClick);
	$button.fadeIn(100);
	
	var $stepElContainer = $calculationContainer.find('.button-container');
	$stepElContainer.append($button);
	return $button;
};

/**
 * Initialize the event handlers and populate the form with initial data
 *  
 * @param callback
 */
CalculationStepManager.prototype.init = function(callback) {
	UI.lock();
	var manager = this;
	
	var initEventHandlers = function() {
		//entity select change handler
		manager.$entitySelect.change(function(event) {
			manager.populateVariableSelect();
		});
		
		// on submit button click 
		manager.$form.find("button[type='submit']").click(function(event){
			event.preventDefault();
			manager.save(function(){
				UI.Form.showResultMessage("Calculation step successfully saved.",true);
			});
		});
	};

	initEventHandlers();

	//load entities
	manager.populateEntitySelect(function() {
		var url = window.target;
		var stepId = $.url(url).param("id");
		if ( stepId ) {
			CalculationStepManager.load(stepId, function(response) {
				manager.currentCalculationStep = response;
				manager.updateForm();
			});
		} else {
			UI.unlock();
		}
	});
	
};

/**
 * Execute the calculation step with the specified id and call the callback function 
 * 
 * @param id
 * @param callback
 */
CalculationStepManager.prototype.execute = function(id, callback) {
	$.ajax({
		url:"rest/calculationstep/"+id+"/run.json",
		dataType:"json",
		async: false 
	})
	.done(function(response){
		callback(response);
	});
};

CalculationStepManager.prototype.save = function(successCallback, errorCallback) {
	UI.lock();
	var manager = this;
	var $step = manager.$form.serialize();
	$.ajax({
		url: "rest/calculationstep/save.json",
		dataType: "json",
		data: $step,
		type: "POST"
	})
	.done(function(response) {
    	UI.Form.updateErrors(manager.$form, response.errors);
    	if(response.status == "ERROR" ) {
    		UI.Form.showResultMessage("There are errors in the form. Please fix them before proceeding.",false);
    		
    		if ( errorCallback ) {
    			errorCallback();
    		}
    	} else {
    		manager.currentCalculationStep = response.fields.calculationStep;
    		manager.updateForm();
	    	
    		CalculationStepManager.updateHomePageStepElement(manager.currentCalculationStep);
	    	
    		if(successCallback) {
	    		successCallback(manager.currentCalculationStep);
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
CalculationStepManager.prototype.updateForm = function(callback) {
	var manager = this;
	var $step = manager.currentCalculationStep;
	manager.$entitySelect.val($step.outputEntityId);
	
	manager.populateVariableSelect($step.outputVariableId, function() {
		UI.Form.setFieldValues(manager.$form, $step);

		UI.unlock();

		if (callback ) {
			callback();
		}
	});
};

/**
 * Returns the selected entity in the form
 * 
 * @returns
 */
CalculationStepManager.prototype.getSelectedEntityId = function() {
	var entityId = this.$entitySelect.val();
	return entityId;
};

/**
 * Populate the "entity" select
 * 
 * @param callback
 */
CalculationStepManager.prototype.populateEntitySelect = function(callback) {
	var manager = this;
	manager.$variableSelect.empty();
	
	WorkspaceManager.loadEntities(function(response) {
		var entities = response;
		UI.Form.populateSelect(manager.$entitySelect, entities, "id", "name");
		if ( callback ) {
			callback(response);
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
CalculationStepManager.prototype.populateVariableSelect = function(value, callback) {
	var manager = this;
	manager.$variableSelect.attr("disabled", "disabled");
	manager.$addVariableButton.attr("disabled", "disabled");
	
	var entityId = manager.getSelectedEntityId(manager.$form);
	if ( entityId ) {
		WorkspaceManager.loadQuantitativeVariables(entityId, function(response) {
			var variables = response;
			
			UI.Form.populateSelect(manager.$variableSelect, variables, "id", "name");
			
			manager.$variableSelect.removeAttr("disabled");
			manager.$addVariableButton.removeAttr("disabled");
			
			if ( value ) {
				manager.$variableSelect.val(value);
			}
			if ( callback ) {
				callback();
			}
		});
	} else {
		manager.$variableSelect.empty();
	}
};
