/**
 * Home Page Calculation section manager
 * 
 * @author S. Ricci
 */
/**
 * Refresh the home page updating the element related to the specified step or
 * reloading all the step elements
 * 
 * @param callback
 * @param $step
 */
function HomeCalculationManager() {
	this.calculationStepManager = CalculationStepManager.getInstance();
}

HomeCalculationManager.prototype = (function() {
	
	var refreshHome = function($step, callback) {
		var $this = this;
		var $calculationContainer = $("#calculation");
		if ( $step ) {
			//update specified step
			var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
			if ( $el.length == 0 ) {
				$.proxy(_addStepToHome, $this)($step);
			} else {
				$el.text($step.caption);
			}
			if ( callback ) {
				callback();
			}
		} else {
			//update all steps
			var $stepElContainer = $calculationContainer.find('.steps-container');
			var $stepElems = $stepElContainer.find(".calculation-button");
			$stepElems.remove();
			
			$this.calculationStepManager.loadAll(function(response){
				var $steps = response;
				$.each($steps, function(i, $step){
					$.proxy(_addStepToHome, $this)($step);
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
	var _addStepToHome = function($step) {
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
	
	var _removeStepFromHome = function($step) {
		var $calculationContainer = $("#calculation");
		var $el = $calculationContainer.find("#calculation-step-el-" + $step.id);
		$el.remove();
	};
	
	//prototype
	return {
		constructor : HomeCalculationManager,
		
		//public methods
		refreshHome : refreshHome
	};
})();
