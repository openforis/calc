/**
 * Home Page Calculation section manager
 * 
 * @author S. Ricci
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
		
		$this.stepBtnsContainer.sortable({
			cancel: false,
			placeholder: "ui-state-highlight",
			start: function(event, ui) {
				$.proxy(setDraggingViewState, $this)(ui.item);
			},
			stop: function(event, ui) {
				var stepBtn = ui.item;
				if ( stepBtn.hasClass("ui-draggable-drop") ) {
					//dropping, don't forget to re-enable all fields at the end...
				} else {
					$.proxy(setDefaultViewState, $this)();
				}
			}
		});
	};
	
	/**
	 * Inits the droppable plugin on the delete button
	 */
	var initDroppableOnDeleteButton = function() {
		var $this = this;
		$this.deleteBtn.droppable({
			accept : ".calculation-button",
			over : function(event, ui) {
				$this.deleteBtn.addClass("highlight");
			},
			out : function(event, ui) {
				$this.deleteBtn.removeClass("highlight");
			},
			drop : function(event, ui) {
				var stepBtn = ui.draggable;
				var step = stepBtn.data("calculationStep");
				stepBtn.addClass("ui-draggable-drop");
				$.proxy(hideStepButton, $this)(step);

				$.proxy(showDeleteConfirm, $this)(step);

				$this.deleteBtn.removeClass("highlight");
			}
		});
	};
	
	/**
	 * Updates a calculation step button associated to the specified CalculationStep
	 */
	var updateCalculationStepButton = function(step, callback) {
		var $this = this;

		var $button = $.proxy(getStepButton, $this)(step);
		if ($button.length == 0) {
			$.proxy(addCalculationStepButton, $this)(step);
		} else {
			$button.data("calculationStep", step);
			$button.text(step.caption);
		}
		if (callback) {
			callback();
		}
	};
	
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
		
		var onOk = $.proxy(performDelete, $this, step);
		
		var onCancel = function() {
			// restore the step button in the old position
			var stepButton = $.proxy(getStepButton, $this)(step);
			stepButton.removeClass("ui-draggable-drop");
			$.proxy(showStepButton, $this)(step);
			$.proxy(setDefaultViewState, $this)();
		};
		UI.showConfirm("Delete '" + step.caption + "'?",  onOk, onCancel, position);
	};

	/**
	 * Set the view to the default state
	 * (enables all fields but the delete button)
	 */
	var setDefaultViewState = function() {
		UI.enableAll();
		UI.disable(this.deleteBtn);
		this.deleteBtn.removeClass("blue-btn-hover");
	};
	
	/**
	 * Set the view to the "dragging" state
	 * (disable all fields but the delete button and the dragged step item)
	 */
	var setDraggingViewState = function(draggedItem) {
		UI.disableAll();
		UI.enable(draggedItem);
		UI.enable(this.deleteBtn);
		this.deleteBtn.addClass("blue-btn-hover");
	};
	
	/**
	 * Executes the delete of the specified CalculationStep
	 */
	var performDelete = function(step) {
		var $this = this;
		$this.calculationStepManager.remove(step.id,
			function(response) {
				$.proxy(removeCalculationStepButton, $this)(step);
				var deletedVariableId = response.fields.deletedVariable;
				if ( deletedVariableId ) {
					//update active workspace object, remove deleted variable
					$this.workspaceManager.activeWorkspace(function(workspace) {
						var entity = workspace.getEntityById(step.outputEntityId);
						entity.deleteVariable(deletedVariableId);
					});
				}
				UI.enableAll();
		});
	};
	
	/**
	 * Creates a home page calculation step button and add it
	 * to the calculation home page section
	 */
	var addCalculationStepButton = function(step) {
		var $this = this;

		var $stepBtn = $this.calculationStepBtnTemplate.clone();
		
		$stepBtn.removeClass("template");
		$stepBtn.data("calculationStep", step);
		$stepBtn.attr("id", "calculation-step-button-" + step.id);

		$stepBtn.find("button").text(step.caption);
		$stepBtn.attr("href", "step-edit.html?id=" + step.id);

		$stepBtn.click($.proxy(calculationStepButtonClickHandler, $this));

		$this.stepBtnsContainer.append($stepBtn);
		/*
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
		*/
		return $stepBtn;
	};
	
	/**
	 * Calculation step button click handler
	 */
	var calculationStepButtonClickHandler = function(event) {
		var button = $(event.currentTarget);
		if (!(button.hasClass("ui-sortable-helper") || 
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
