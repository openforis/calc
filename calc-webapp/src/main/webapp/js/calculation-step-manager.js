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
		
		$this._(initEventHandlers)();
	
		//load entities
		$this._(populateEntitySelect)(function() {
			var url = window.target;
			var stepId = $.url(url).param("id");
			if ( stepId ) {
				CalculationStepManager.load(stepId, function(response) {
					$this.currentCalculationStep = response;
					$this._(updateForm)();
				});
			} else {
				UI.unlock();
			}
		});
		
	};
	
	/**
	 * Init input fields event listeners
	 */
	var initEventHandlers = function() {
		//entity select change handler
		var $this = this;
		$this.$entitySelect.change(function(event) {
			$this._(populateVariableSelect)();
		});
		
		// on submit button click 
		$this.$form.find("button[type='submit']").click(function(event){
			event.preventDefault();
			$this._(save)(function(){
				UI.Form.showResultMessage("Calculation step successfully saved.",true);
			});
		});
	};
	
	/**
	 * Execute the calculation step with the specified id and call the callback function 
	 * 
	 * @param id
	 * @param callback
	 */
	var execute = function(id, callback) {
		$.ajax({
			url:"rest/calculationstep/"+id+"/run.json",
			dataType:"json",
			async: false 
		})
		.done(function(response){
			callback(response);
		});
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
	    		$this._(updateForm)();
		    	
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
		$this.$entitySelect.val($step.outputEntityId);
		
		$this._(populateVariableSelect)($step.outputVariableId, function() {
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
		var entityId = this.$entitySelect.val();
		return entityId;
	};
	
	/**
	 * Populate the "entity" select
	 * 
	 * @param callback
	 */
	var populateEntitySelect = function(callback) {
		var $this = this;
		$this.$variableSelect.empty();
		
		WorkspaceManager.loadEntities(function(response) {
			var entities = response;
			UI.Form.populateSelect($this.$entitySelect, entities, "id", "name");
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
	var populateVariableSelect = function(value, callback) {
		var $this = this;
		$this.$variableSelect.attr("disabled", "disabled");
		$this.$addVariableButton.attr("disabled", "disabled");
		
		var entityId = $this.getSelectedEntityId($this.$form);
		if ( entityId ) {
			WorkspaceManager.loadQuantitativeVariables(entityId, function(response) {
				var variables = response;
				
				UI.Form.populateSelect($this.$variableSelect, variables, "id", "name");
				
				$this.$variableSelect.removeAttr("disabled");
				$this.$addVariableButton.removeAttr("disabled");
				
				if ( value ) {
					$this.$variableSelect.val(value);
				}
				if ( callback ) {
					callback();
				}
			});
		} else {
			$this.$variableSelect.empty();
		}
	};
	
	//prototype
	return {
		constructor : CalculationStepManager,
		
		//public methods
		_init : init,
		populateVariableSelect : populateVariableSelect,
		getSelectedEntityId : getSelectedEntityId,
		save : save,
		execute : execute,
		
		// define private methods dedicated one
		_:function(callback) {
			// instance referer
			var self = this;
			// callback that will be used
			return function() {
				return callback.apply(self, arguments);
			};
		}
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
		callback(response);
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
			$el.html($step.caption);
		}
		if ( callback ) {
			callback();
		}
	} else {
		//update all steps
		var $stepElContainer = $calculationContainer.find('.button-container');
		var $stepElems = $stepElContainer.find(".calculation-button");
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
