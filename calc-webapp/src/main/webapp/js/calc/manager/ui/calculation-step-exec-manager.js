/**
 * 
 * Manager for calculation step execute and test sections
 * 
 * @author Mino Togna
 * @author Stefano Ricci
 *  
 */
function CalculationStepExecManager(container, calculationStepResultsManager) {
	// main ui container
	this.container = container;
	// results container to show after job execution
	this.calculationStepResultsManager = calculationStepResultsManager;
	// header
	this.header = this.container.find(".header");
	//exec btn
	this.execButton = this.container.find('button[name=exec-btn]');
	//test section
	this.testContainer = this.container.find(".test");
	//test btn
	this.testButton = this.container.find("button[name=test-btn]");
	//test input variables rows container
	this.settingsRowsContainer = this.container.find(".input-var-rows");
	//input variable test parameters row template
	this.testInputVariableRowTemplate = this.container.find(".test-input-var.template");
	
	//calc step associated. it's set by the show method 
	this.calculationStep = null;
	
	//job manager
	this.jobManager = JobManager.getInstance();
	this.calculationStepManager = CalculationStepManager.getInstance();
	this.workspaceManager = WorkspaceManager.getInstance();
	
	//array with the variable settings
	this.settingsRows = new Array();
	
	this._init();
};

CalculationStepExecManager.prototype = (function() {
	
	/**
	 *  init function
	 */
	var init = function() {
		var $this = this;
		
		/*
		 * event handlers
		 */
		//start step execution on click
		this.execButton.click(function(e) {
			$this.jobManager.executeCalculationStep(
					$this.calculationStep.id, 
					// on complete show results
					function(job) {
						// create instance of data provider
						var entityId = $this.calculationStep.outputEntityId;
						var variables  = $this.calculationStep.variables;
						var dataProvider = new DataViewProvider(entityId, variables, true);
						
						// once completed hide this and shows results section
						$this.hide();
						$this.calculationStepResultsManager.showDataVisOptions();
						$this.calculationStepResultsManager.show(dataProvider);
					}
					, true
			);
		});
		
		//validate parameters and starts the execution of the test
		this.testButton.click(function(e) {
			if ( $.proxy(validateTestSettings, $this)() ) {
				UI.disableAll();
				var variableSettings = $.proxy(extractVariablesSettings, $this)();
				$this.jobManager.executeCalculationStepTest(
					$this.calculationStep.id,
					//on complete show results
					function(job) {
						if ( job.status == "COMPLETED" ) {
							$.proxy(showTestResults, $this)(variableSettings, job);
						}
					},
					variableSettings
				);
			} else {
				UI.showMessage("Please fix the errors in the form before proceeding.");
			}
		});
	};
	
	/**
	 * show exec section
	 */
	var show = function(calculationStep) {
		this.calculationStep = calculationStep;
		
		// append legend with step name to header
		this.header.empty();
		var legend = $("<legend></legend>");
		legend.html(this.calculationStep.caption);
		this.header.append( legend );
		
		// update input variables rows
		$.proxy(updateTestSettings, this)();
		
		//show main container
		this.container.fadeIn(400);
		
		
		// test
//		var entityId = this.calculationStep.outputEntityId;
//		var variables  = this.calculationStep.variables;
//		var dataProvider = new DataViewProvider(entityId , variables);
//		
//		var start = $.now();
//		console.log("Start : " + start);
//		dataProvider.data( 0 , 100000 , null , null, function(response) {
//			var end = ( $.now() - start ) /1000;
//			console.log("End : " + end + " seconds");
//		} );
	};
	
	var hide = function () {
		this.container.hide();
	};
	
	var showTestResults = function(variableSettings, job) {
		var $this = this;
		$this.workspaceManager.activeWorkspace(function(workspace) {
			var variableNames = $.map(variableSettings, function(settings, variableName) {
				return variableName;
			});
			var entityId = $this.calculationStep.outputEntityId;
			var entity = workspace.getEntityById(entityId);
			var outputVariable = entity.getVariableById($this.calculationStep.outputVariableId);
			
			variableNames.push(outputVariable.name);
			
			// instanciate data provider
			var dataProvider = new CalculationStepTestDataProvider(job.id, entityId, variableNames);
			
			// once completed hide this and shows results section
			$this.hide();
			$this.calculationStepResultsManager.hideDataVisOptions();
			$this.calculationStepResultsManager.show(dataProvider);
		});
	};
	
	/**
	 * Updates the settings row.
	 * First it removes the unused variables settings rows, then adds the new rows.
	 */
	var updateTestSettings = function() {
		var $this = this;
		
		//delete rows corresponding to unused variables
		var unusedRows = $.proxy(getUnusedVariablesRows, $this)();
		$.each(unusedRows, function(index, row) {
			row.remove();
			ArrayUtils.removeItem($this.settingsRows, row);
		});
		
		//add new variable rows
		$.proxy(getTestVariableNames, $this)(function(variables){
			$.each(variables, function(index, variable) {
				var oldRow = $.proxy(getSettingsRow, $this)(variable);
				if ( oldRow == null ) {
					var row = new VariableSettingsRow(variable, $this.testInputVariableRowTemplate);
					$this.settingsRows.push(row);
					$this.settingsRowsContainer.append(row.rowElement);
				}
			});
		}, true);
	};
	
	/**
	 * Returns the entity variables used as test parameters.
	 */
	var getTestVariableNames = function(success, excludeOutputVariable) {
		var $this = this;
		$this.workspaceManager.activeWorkspace(function(workspace) {
			var rScript = $this.calculationStep.rscript;
			var variableNames = ArrayUtils.clone(rScript.variables);
			var entity = workspace.getEntityById($this.calculationStep.outputEntityId);
			var outputVariable = entity.getVariableById($this.calculationStep.outputVariableId);
			if ( excludeOutputVariable ) {
				ArrayUtils.removeItem(variableNames, outputVariable.name);
			}
			success(variableNames);
		});
	};
	
	/**
	 * Returns the row corresponding to the specified variable
	 */
	var getSettingsRow = function(variableName) {
		for(var i=0; i < this.settingsRows.length; i++) {
			var row = this.settingsRows[i];
			if ( row.variableName == variableName ) {
				return row;
			}
		}
		return null;
	};
	
	/**
	 * Extracts all the settings from every row
	 */
	var extractVariablesSettings = function() {
		var result = {};
		$.each(this.settingsRows, function(index, row) {
			var params = row.extractSettings();
			if ( params.increment > 0 ) {
				result[row.variableName] = params;
			}
		});
		return result;
	};
	
	/**
	 * Validates the input fields of a variable settings row
	 */
	var validateTestSettings = function() {
		var result = true;
		$.each(this.settingsRows, function(index, row) {
			var valid = row.validate();
			result = result && valid;
		});
		return result;
	};
	
	/**
	 * Returns a list of variable settings rows corresponding to variables not in use by the calculation step
	 */
	var getUnusedVariablesRows = function() {
		var $this = this;
		var result = new Array();
		$.proxy(getTestVariableNames, $this)(function(variables) {
			$.each($this.settingsRows, function(index, row) {
				if ( ! ArrayUtils.contains(variables, row.variableName) ) {
					result.push(row);
				}
			});
		});
		return result;
	};
	
	//prototype
	return {
		constructor : CalculationStepExecManager,
		
		//public methods
		_init : init,
		
		//show / hide 
		show : show
		,
		hide : hide
		
	};
})();

/**
 * Wrapper for a calculation step variable parameters row
 */
function VariableSettingsRow(variableName, rowTemplate) {
	this.variableName = variableName;
	
	this.rowElement = rowTemplate.clone();
	this.rowElement.removeClass("template");
	this.rowElement.show();
	this.variableNameLabel = this.rowElement.find(".variable-name");
	this.minField = this.rowElement.find("input[name=min]");
	this.maxField = this.rowElement.find("input[name=max]");
	this.incrementField = this.rowElement.find("input[name=increment]");

	//set default values in elements
	var defaultMin = 0;
	var defaultMax = 100;
	var defaultIncrement = 10;

	this.variableNameLabel.text(variableName);
	this.minField.val(defaultMin);
	this.maxField.val(defaultMax);
	this.incrementField.val(defaultIncrement);
	
	this._init();
};

VariableSettingsRow.prototype = (function() {
	
	/**
	 * Initializes event listeners on the fields
	 */
	var init = function() {
		//on fields change, validate the fields
		this.minField.change($.proxy(validate, this));
		this.maxField.change($.proxy(validate, this));
		this.incrementField.change($.proxy(validate, this));
	};
	
	/**
	 * Validates the fields and updates the UI error feedback
	 */
	var validate = function() {
		UI.Form.removeErrors(this.rowElement);
		var minValid = UI.Form.validation.required(this.minField, "Min") && UI.Form.validation.numeric(this.minField);
		var maxValid = UI.Form.validation.required(this.maxField, "Max") && UI.Form.validation.numeric(this.maxField) && 
			UI.Form.validation.greaterThan(this.maxField, "Max", Number(this.minField.val()));
		var incrementValid = UI.Form.validation.required(this.incrementField, "Increment") && UI.Form.validation.numeric(this.incrementField) &&
			UI.Form.validation.greaterThan(this.incrementField, "Increment", 0);
		return minValid && maxValid && incrementValid;
	};
	
	/**
	 * Removes the row element from the UI
	 */
	var remove = function() {
		this.rowElement.remove();
	};
	
	/**
	 * Extracts the variable settings from the input field values  
	 */
	var extractSettings = function () {
		var result = { 
			max: this.maxField.val(),
			min: this.minField.val(),
			increment: this.incrementField.val()
		};
		return result;
	};
	
	//prototype
	return {
		constructor : VariableSettingsRow,
		
		//public methods
		_init : init,
		
		validate : validate
		,
		remove : remove
		,
		extractSettings : extractSettings
		
	};
})();
