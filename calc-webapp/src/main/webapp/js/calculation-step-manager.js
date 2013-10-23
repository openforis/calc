var CalculationStepManager = {
	/**
	 * Load all the calculation steps associated to the default processing chain in the active workspace
	 * and call the specified callback function
	 *  
	 * @param callback
	 */
	loadAll: function(callback) {
		$.ajax({
			url:"rest/calculationstep/load.json",
			dataType:"json"
		}).done(function(response){
			callback(response);
		});
	},
	/**
	 * Load the calculation step with the specified id and call the callback function
	 * 
	 * @param id
	 * @param callback
	 */
	load: function(id, callback) {
		$.ajax({
			url:"rest/calculationstep/"+id+"/load.json",
			dataType:"json"
		})
		.done(function(response){
			callback(response);
		});
	},
	/**
	 * Execute the calculation step with the specified id and call the callback function 
	 * 
	 * @param id
	 * @param callback
	 */
	execute: function(id, callback) {
		$.ajax({
			url:"rest/calculationstep/"+id+"/run.json",
			dataType:"json",
			async: false 
		})
		.done(function(response){
			callback(response);
		});
	},
	save: function($stepForm, successCallback, errorCallback) {
		UI.lock();
		var $step = $stepForm.serialize();
		$.ajax({
			url: "rest/calculationstep/save.json",
			dataType: "json",
			data: $step,
			type: "POST"
		})
		.done(function(response) {
	    	UI.Form.updateErrors($stepForm, response.errors);
	    	if(response.status == "ERROR" ) {
	    		UI.Form.showResultMessage("There are errors in the form. Please fix them before proceeding.",false);
	    		
	    		if ( errorCallback ) {
	    			errorCallback();
	    		}
	    	} else {
	    		var $calculationStep = response.fields.calculationStep;
	    		CalculationStepManager.updateForm($stepForm, $calculationStep);
		    	
	    		CalculationStepManager.updateHomePageStepElement($calculationStep);
		    	
	    		if(successCallback) {
		    		successCallback($calculationStep);
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
		
	},
	
	initForm: function($form, callback) {
		UI.lock();
		//load entities
		CalculationStepManager._populateEntitySelect($form, function() {
			var url = window.target;
			var stepId = $.url(url).param("id");
			if ( stepId ) {
				CalculationStepManager.load(stepId, function(response) {
					var $step = response;
					CalculationStepManager.updateForm($form, $step);
				});
			} else {
				UI.unlock();
			}
		});
		//entity select change handler
		var $entitySelect = CalculationStepManager._getEntitySelect($form);
		$entitySelect.change(function(event) {
			CalculationStepManager.populateVariableSelect($form);
		});
	},
	
	/**
	 * TODO
	 * 
	 * @param step
	 * @param form
	 */
	updateForm: function($form, $step, callback) {
		var $entitySelect = CalculationStepManager._getEntitySelect($form);
		$entitySelect.val($step.outputEntityId);
		
		CalculationStepManager.populateVariableSelect($form, $step.outputVariableId, function() {
			UI.Form.setFieldValues($form, $step);

			UI.unlock();

			if (callback ) {
				callback();
			}
		});
	},
	
	_getEntitySelect: function($form) {
		var $select = $form.find("[name=entityId]");
		return $select;
	},

	getSelectedEntityId: function($form) {
		var $entitySelect = CalculationStepManager._getEntitySelect($form);
		var entityId = $entitySelect.val();
		return entityId;
	},
	
	_getVariableSelect: function($form) {
		var $select = $form.find("[name=variableId]");
		return $select;
	},
	
	/**
	 * Internal function: populate the "entity" select
	 */
	_populateEntitySelect: function($form, callback) {
		var $entitySelect = CalculationStepManager._getEntitySelect($form);
		var $variableSelect = CalculationStepManager._getVariableSelect($form);
		$variableSelect.empty();
		
		WorkspaceManager.loadEntities(function(response) {
			var entities = response;
			UI.Form.populateSelect($entitySelect, entities, "id", "name");
			if ( callback ) {
				callback(response);
			}
		});
	},
	
	/**
	 * Populate the "output variable" select according to the selected parent entityId.
	 * 
	 * The option corresponding to the value specified will be selected.
	 */
	populateVariableSelect: function($form, value, callback) {
		var $variableSelect = $form.find("[name=variableId]");
		var $addVariableButton = $form.find("[name=add-variable]");
		
		$variableSelect.attr("disabled", "disabled");
		$addVariableButton.attr("disabled", "disabled");
		
		var entityId = CalculationStepManager.getSelectedEntityId($form);
		if ( entityId ) {
			WorkspaceManager.loadQuantitativeVariables(entityId, function(response) {
				var variables = response;
				
				UI.Form.populateSelect($variableSelect, variables, "id", "name");
				
				$variableSelect.removeAttr("disabled");
				$addVariableButton.removeAttr("disabled");
				
				if ( value ) {
					$variableSelect.val(value);
				}
				if ( callback ) {
					callback();
				}
			});
		} else {
			$variableSelect.empty();
		}
	},
	/**
	 * Load all the calculation steps and populate the container in the home page
	 * 
	 * @param callback
	 */
	updateHomePage: function(callback) {
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
	},
	/**
	 * Update only the element in the home page corresponding to the specified step
	 * 
	 * @param $step
	 */
	updateHomePageStepElement: function($step) {
		var $calculationContainer = $("#calculation");
		var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
		if ( $el.length == 0 ) {
			CalculationStepManager._addHomePageStepElement($step);
		} else {
			$el.html($step.caption);
		}
	},
	/**
	 * Private function: create a home page calculation step element and add it to the dom
	 *  
	 * @param $step
	 */
	_addHomePageStepElement: function($step) {
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
	}
	
};