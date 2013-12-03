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
function HomeCalculationManager(container) {
	this.container = container;
	
	//ui elements
	this.deleteBtn = null;
	this.stepsBtnsContainer = null;
	this.calculationStepBtnTemplate = null;
	
	//managers
	this.calculationStepManager = null;
	this.workspaceManager = null;

	this.init();
}

HomeCalculationManager.prototype = (function() {
	
	/**
	 * Initializes instance variables and event listeners
	 */
	var init = function() {
		var $this = this;
		
		//init ui elements
		$this.stepBtnsContainer = $this.container.find('.step-buttons-container');
		$this.deleteBtn = $this.container.find(".delete");
		$this.calculationStepBtnTemplate = $this.container.find(".calculation-button.template");
		
		//init managers
		$this.calculationStepManager = CalculationStepManager.getInstance();
		$this.workspaceManager = WorkspaceManager.getInstance();

		$.proxy(initDroppableOnDeleteButton, $this)();
	};
	
	var initDroppableOnDeleteButton = function() {
		var $this = this;
		$this.deleteBtn.droppable({
			accept : ".calculation-button",
			over : function(event, ui) {
				$(this).addClass("highlight");
			},
			out : function(event, ui) {
				$(this).removeClass("highlight");
			},
			drop : function(event, ui) {
				var $stepBtn = $(ui.draggable);
				var step = $stepBtn.data("calculationStep");

				$stepBtn.addClass("ui-draggable-drop");

				$.proxy(showDeleteConfirm, $this)(step);

				$(this).removeClass("highlight");
			}
		});
	};
	
	/**
	 * Updates a calculation step button associated to the specified CalculationStep
	 */
	var updateCalculationStepButton = function($step, callback) {
		var $this = this;

		var $button = $.proxy(getStepButton, $this)($step);
		if ($button.length == 0) {
			$.proxy(addCalculationStepButton, $this)($step);
		} else {
			$button.data("calculationStep", $step);
			$button.text($step.caption);
		}
		if (callback) {
			callback();
		}
	}
	
	/**
	 * Updates all calculation step buttons
	 */
	var updateCalculationStepButtons = function(callback) {
		var $this = this;
		var $stepBtns = $this.stepBtnsContainer.find(".calculation-button");
		$stepBtns.remove();

		$this.calculationStepManager.loadAll(function(response) {
			var $steps = response;
			$.each($steps, function(i, $step) {
				$.proxy(addCalculationStepButton, $this)($step);
			});
			if (callback) {
				callback();
			}
		});
	};
	
	/**
	 * Returns the button associated to the specified CalculationStep
	 */
	var getStepButton = function(step) {
		var button = this.container.find("#calculation-step-button-" + step.id);
		return button;
	};

	/**
	 * Show the button associated to the specified CalculationStep
	 */
	var showStepButton = function(step) {
		var button = $.proxy(getStepButton, this)(step);
		button.css("visibility", "visible");
	};

	/**
	 * Hide the button associated to the specified CalculationStep
	 */
	var hideStepButton = function(step) {
		var button = $.proxy(getStepButton, this)(step);
		button.css("visibility", "hidden");
	};

	/**
	 * Show the dialog to confirm the calculation step delete
	 */
	var showDeleteConfirm = function(step) {
		var $this = this;
		var position = $this.deleteBtn.offset();

		UI.showConfirm("Delete '" + step.caption + "'?", $.proxy(performDelete, $this, step), function() {
			// restore the step button in the old position
			var stepButton = $.proxy(getStepButton, $this)(step);
			stepButton.removeClass("ui-draggable-drop");
			$.proxy(showStepButton, $this)(step);
			UI.enableAll();
		}, position);
	};

	/**
	 * Executes the delete of the specified CalculationStep
	 */
	var performDelete = function(step) {
		var $this = this;
		$this.calculationStepManager.remove(step.id,
			function(response) {
				$.proxy(removeCalculationStepButton, $this)(step);
				$this.workspaceManager.refreshActiveWorkspace(function() {
					UI.enableAll();
				});
		});
	};
	
	/**
	 * Creates a home page calculation step button and add it
	 * to the calculation home page section
	 * 
	 * @param $step
	 */
	var addCalculationStepButton = function(step) {
		var $this = this;

		var $stepBtn = $this.calculationStepBtnTemplate.clone();
		
		$stepBtn.removeClass("template");
		$stepBtn.data("calculationStep", step);
		$stepBtn.attr("id", "calculation-step-button-" + step.id);

		$stepBtn.text(step.caption);
		$stepBtn.attr("href", "step-edit.html?id=" + step.id);

		$stepBtn.click($.proxy(calculationStepButtonClickHandler, $this));

		$stepBtn.css("display", "block");

		$this.stepBtnsContainer.append($stepBtn);

		$stepBtn.draggable({
			revert : "invalid",
			cancel : false,
			helper : "clone",
			start : function(event, ui) {
				$.proxy(hideStepButton, $this)(step);
				UI.disableAll();
				UI.enable(ui.helper);
				UI.enable($this.deleteBtn);
				$this.deleteBtn.addClass("blue-btn-hover");
			},
			stop : function(event, ui) {
				if (!$(this).hasClass("ui-draggable-drop")) {
					$.proxy(showStepButton, $this)(step);
					UI.enableAll();
				} else {
					// dropping: don't forget to re-enable all fields
				}
				$this.deleteBtn.removeClass("blue-btn-hover");
				UI.disable($this.deleteBtn);
			}
		});
		return $stepBtn;
	};
	
	/**
	 * Calculation step button click handler
	 */
	var calculationStepButtonClickHandler = function(event) {
		var button = $(event.currentTarget);
		if (!(button.hasClass("ui-draggable-dragging") || 
				button.hasClass("ui-draggable-drop"))) {
			homeButtonClick(event);
		}
	};
	
	/**
	 * Removes the button associated to the specified CalculationStep
	 */
	var removeCalculationStepButton = function(step) {
		var $this = this;
		var button = $.proxy(getStepButton, $this)(step);
		button.remove();
	};

	// prototype
	return {
		constructor : HomeCalculationManager,

		// public methods
		init : init
		,
		updateCalculationStepButton : updateCalculationStepButton
		,
		updateCalculationStepButtons : updateCalculationStepButtons
	};
})();
