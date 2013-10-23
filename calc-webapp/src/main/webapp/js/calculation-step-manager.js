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
	save: function($step, successCallback, errorCallback) {
		UI.lock();
		$.ajax({
			url: "rest/calculationstep/save.json",
			dataType: "json",
			data: $step,
			type: "POST"
		})
		.done(function(response) {
			successCallback(response);
		})
		.error(function(e) {
			errorCallback(e);
		})
		.complete(function() {
			UI.unlock();
		});
		
	},
	/**
	 * TODO
	 * 
	 * @param step
	 * @param form
	 */
	updateForm: function(step, form) {
		
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