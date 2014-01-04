/**
 * Home Page Calculation section manager
 * 
 * @author S. Ricci
 */
function HomeCalculationManager(container) {
	this.container = container;
	
	//init ui elements
	this.stepsContainer = this.container.find('.calculation-steps-container');
	this.deleteBtn = this.container.find(".delete");
	this.executeBtn = this.container.find(".execute");
	
	this.calculationStepBtnTemplate = this.container.find(".calculation-step.template");
	
	//init managers
	this.calculationStepManager = CalculationStepManager.getInstance();
	this.workspaceManager = WorkspaceManager.getInstance();

	this._init();
}

HomeCalculationManager.prototype = (function() {
	
	/**
	 * Initializes instance variables and event listeners
	 */
	var init = function() {
		var $this = this;
		
		// set delete button as droppable
		$this.deleteBtn.droppable({
			accept : ".calculation-step",
			over : function(event, ui) {
				$this.deleteBtn.addClass("highlight");
			},
			out : function(event, ui) {
				$this.deleteBtn.removeClass("highlight");
			},
			drop : $.proxy(deleteDropHandler, $this)
		});
		
		// set calc steps sortable
		$this.stepsContainer.sortable({
			cancel: false,
			placeholder: "calculation-step-placeholder",
			revert: true,
			start: $.proxy(sortStart, $this),
			stop: $.proxy(sortStop, $this),
			update: $.proxy(sortUpdate, $this)
		});
		
		// exec button click
		this.executeBtn.click(function(e){
			JobManager.getInstance().execute(function(response){
//				console.log("response. executed?!");
			});
		});
	};
	/**
	 * Handlers on drop event of delete button
	 */
	var deleteDropHandler = function(event, ui) {
		var element = ui.draggable;
		var step = element.data("calculationStep");
		element.addClass("ui-draggable-drop");
		// temporarly hide step element from ui (waiting for confirmation)
		element.invisible();
		// hide sortable placeholder
		this.container.find(".calculation-step-placeholder").invisible();
		// and show confirm dialog
		$.proxy(showDeleteConfirm, this)(step);

		this.deleteBtn.removeClass("highlight");
	};

	/**
	 * Handlers for sortable calculation step events
	 */	
	var sortStart = function(event, ui) {
		var element = ui.item;
		element.data("originalIndex", element.index());
		
		UI.disableAll();
		UI.enable(element);
		UI.enable(element.find("button"));
		UI.enable(this.deleteBtn);
		this.deleteBtn.addClass("blue-btn-hover");
	};
	
	var sortStop = function(event, ui) {
		var element = ui.item;
		// if element has not been dropped (ui-draggable-drop) and has not been sorted (ui-sortable-updating) reset ui to its original state
		if ( ! (element.hasClass("ui-draggable-drop") || element.hasClass("ui-sortable-updating") ) ) {				
			$.proxy(reset, this)();
		}
	};
	
	var sortUpdate = function(event, ui) {
		var element = ui.item;
		// if step has not been dropped to the trash updates its step number  
		if ( ! element.hasClass("ui-draggable-drop") ) {
			var newStepNo = element.index() + 1; 
			var step = element.data("calculationStep");
			element.addClass("ui-sortable-updating");
			$.proxy(updateStepNumber, this)(step, newStepNo);
		}
	};
	
	/**
	 * Updates a calculation step element associated to the specified CalculationStep
	 */
	var updateStep = function(step, callback) {
		var $this = this;

		var element = $.proxy(getStepElement, $this)(step);
		if (element.length == 0) {
			$.proxy(addStepElement, $this)(step);
		} else {
			element.data("calculationStep", step);
			element.find("button").text(step.caption);
		}
		if (callback) {
			callback();
		}
	};
	
	/**
	 * Updates all calculation step elements
	 */
	var updateSteps = function(callback) {
		var $this = this;
		var stepElements = $this.stepsContainer.find(".calculation-step");
		stepElements.remove();

		$this.calculationStepManager.loadAll(function(response) {
			var $steps = response;
			$.each($steps, function(i, $step) {
				$.proxy(addStepElement, $this)($step);
			});
			if (callback) {
				callback();
			}
		});
	};
	
	/**
	 * Returns the element associated to the specified CalculationStep
	 */
	var getStepElement = function(step) {
		var element = this.container.find("#calculation-step-" + step.id);
		return element;
	};

	/**
	 * Show the dialog to confirm the calculation step delete
	 */
	var showDeleteConfirm = function(step) {
		var $this = this;
		
		var position = $this.deleteBtn.offset();
		position.top -= 20; 
		position.left -= 200; 
		var onOk = $.proxy(deleteStep, $this, step);
		
		var onCancel = function() {
			//restore step element original position
			var stepElement = $.proxy(getStepElement, $this)(step);
			var originalIndex = stepElement.data("originalIndex");
			var replacedBy = stepElement.parent().children()[originalIndex];
			stepElement.insertAfter(replacedBy);

			stepElement.removeClass("ui-draggable-drop");
			stepElement.visible();
			
			$.proxy(reset, $this)();
		};
		UI.showConfirm("Delete '" + step.caption + "'?",  onOk, onCancel, position);
	};

	/**
	 * Set the view to the default state
	 * (enables all fields but the delete button)
	 */
	var reset = function() {
		UI.enableAll();
		UI.disable(this.deleteBtn);
		this.deleteBtn.removeClass("blue-btn-hover");
	};
	
	/**
	 * Executes the delete of the specified CalculationStep
	 */
	var deleteStep = function(step) {
		var $this = this;
		UI.lock();
		$this.calculationStepManager.remove(step.id,
			function(response) {
				//remove element from ui
				var element = $.proxy(getStepElement, $this)(step);
				element.remove();
				
				var deletedVariableId = response.fields.deletedVariable;
				if ( deletedVariableId ) {
					//update active workspace object, remove deleted variable
					$this.workspaceManager.activeWorkspace(function(workspace) {
						var entity = workspace.getEntityById(step.outputEntityId);
						entity.deleteVariable(deletedVariableId);
					});
				}
				$.proxy(reset, $this)();
				UI.unlock();
		});
	};

	var updateStepNumber = function(step, stepNo) {
		var $this = this;
		UI.lock();
		var element = $.proxy(getStepElement, $this)(step);
		UI.disable(element);
		UI.disable(element.find("button"));
		$this.calculationStepManager.updateStepNumber(step.id, stepNo, 
			function(response) {
				var element = $.proxy(getStepElement, $this)(step);
				element.removeClass("ui-sortable-updating");
				$.proxy(reset, $this)();
				UI.unlock();
			}
		);
	};
	
	/**
	 * Creates a home page calculation step element and add it
	 * to the calculation home page section
	 */
	var addStepElement = function(step) {
		var $this = this;

		var element = $this.calculationStepBtnTemplate.clone();
		
		element.removeClass("template");
		element.data("calculationStep", step);
		element.attr("id", "calculation-step-" + step.id);

		element.find("button").text(step.caption);
		element.attr("href", "step-edit.html?id=" + step.id);

		element.click($.proxy(stepClickHandler, $this));

		$this.stepsContainer.append(element);

		return element;
	};
	
	/**
	 * Calculation step element click handler
	 */
	var stepClickHandler = function(event) {
		var element = $(event.currentTarget);
		if ( !( element.hasClass("ui-sortable-helper") || element.hasClass("ui-draggable-drop") ) ) {
			homeButtonClick(event);
		}
	};
	
	// prototype
	return {
		constructor : HomeCalculationManager,

		// public methods
		_init : init
		,
		updateStep : updateStep
		,
		updateSteps : updateSteps
	};
})();
