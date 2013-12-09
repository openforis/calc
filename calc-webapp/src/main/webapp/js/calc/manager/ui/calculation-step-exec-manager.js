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
	this.testInputVariablesRows = this.container.find(".input-var-rows");
	//input variable test parameters row template
	this.testInputVariableRowTemplate = this.container.find(".test-input-var.template");
	
	//calc step associated. it's set by the show method 
	this.calculationStep = null;
	
	//job manager
	this.jobManager = new JobManager();
	
	//associative array with variable parameters indexed by name
	this.testVariableParametersByName = {};
	
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
			UI.disableAll();
			$this.jobManager.executeCalculationStep(
					$this.calculationStep.id, 
					//on complete show results
					function(job) {
						UI.enableAll();
						
						// instanciate data provider
						var entityId = $this.calculationStep.outputEntityId;
						var variables  = $this.calculationStep.variables;
						var dataProvider = new DataViewProvider(entityId , variables);
						
						// once completed hide this and shows results section
						$this.hide();
						$this.calculationStepResultsManager.show(dataProvider);
					}
					, true
			);
		});
		
		//validate parameters and starts the execution of the test
		this.testButton.click(function(e) {
			if ( $.proxy(validateTestForm, $this)() ) {
				//TODO
				var parameters = $.proxy(extractVariablesParameters, $this)();
				var dataObj = {variables_parameters: parameters};
				var dataStr = JSON.stringify(dataObj);
				$.ajax({
					url : "rest/job/step/"+$this.calculationStep.id+"/test.json",
					data: dataStr,
					dataType:"json"
				}).done(function(response) {
					console.log(response);
				})
				.error(function(e){
					console.log("error!!! on test");
					console.log(e);
				});
				
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
		$.proxy(updateInputVariablesRows, this)();
		
		//show main container
		this.container.fadeIn(400);
	};
	
	var hide = function () {
		this.container.hide();
	};
	
	var updateInputVariablesRows = function() {
		var $this = this;
		//delete rows corresponding to unused variables
		$.each($this.testVariableParametersByName, function(variableName, variableParametersRow) {
			if ( $this.calculationStep.inputVariables.indexOf(variableName) < 0 ) {
				variableParametersRow.remove();
				delete $this.testVariableParametersByName[variableName];
			}
		});
		//add new variable rows
		$.each($this.calculationStep.inputVariables, function(index, variableName) {
			var oldRow = $this.testVariableParametersByName[variableName];
			if ( oldRow == null ) {
				var variableParametersRow = CalculationStepExcecutionVariableRow.newInstance($this.testInputVariableRowTemplate, variableName);
				$this.testVariableParametersByName[variableName] = variableParametersRow;
				$this.testInputVariablesRows.append(variableParametersRow.row);
			}
		});
	};
	
	var extractVariablesParameters = function() {
		var result = new Array();
		$.each(this.testVariableParametersByName, function(variableName, variableParametersRow) {
			var params = variableParametersRow.extractParameters();
			result.push(params);
		});
		return result;
	};
	
	var validateTestForm = function() {
		var result = true;
		$.each(this.testVariableParametersByName, function(variableName, variableParametersRow) {
			var valid = variableParametersRow.validate();
			result = result && valid;
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
function CalculationStepExcecutionVariableRow(variableName, row, minField, maxField, incrementField) {
	this.variableName = variableName;
	this.row = row;
	this.minField = minField;
	this.maxField = maxField;
	this.incrementField = incrementField;
	
	this._init();
};

/**
 * Creates an instance of CalculationStepExcecutionVariableRow associated to a variable, starting from a row template.
 */
CalculationStepExcecutionVariableRow.newInstance = function(rowTemplate, variableName) {
	//constants
	var defaultMin = 0;
	var defaultMax = 100;
	var defaultIncrement = 10;
	
	var row = rowTemplate.clone();
	row.removeClass("template");
	row.find(".variable-name").text(variableName);
	row.show();
	var minField = row.find("input[name=min_]");
	minField.attr("name", "min_" + variableName);
	minField.val(defaultMin);
	var maxField = row.find("input[name=max_]");
	maxField.attr("name", "max_" + variableName);
	maxField.val(defaultMax);
	var incrementField = row.find("input[name=increment_]");
	incrementField.attr("name", "increment_" + variableName);
	incrementField.val(defaultIncrement);
	var result = new CalculationStepExcecutionVariableRow(variableName, row, minField, maxField, incrementField);
	return result;
};

CalculationStepExcecutionVariableRow.prototype = (function() {
	
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
		UI.Form.removeErrors(this.row);
		var minValid = UI.Form.validation.required(this.minField, "Min") && UI.Form.validation.numeric(this.minField);
		var maxValid = UI.Form.validation.required(this.maxField, "Max") && UI.Form.validation.numeric(this.maxField);		
		var incrementValid = UI.Form.validation.required(this.incrementField, "Increment") && UI.Form.validation.numeric(this.incrementField);
		return minValid && maxValid && incrementValid;
	};
	
	/**
	 * Removes the row element from the UI
	 */
	var remove = function() {
		this.row.remove();
	};
	
	/**
	 * Extracts a @link{CalculationStepExecutionVariableParameters} from the input field values  
	 */
	var extractParameters = function () {
		var min = this.minField.val();
		var max = this.maxField.val();
		var increment = this.incrementField.val();
		var result = new CalculationStepExecutionVariableParameters(this.variableName, max, min, increment);
		return result;
	};
	
	//prototype
	return {
		constructor : CalculationStepExcecutionVariableRow,
		
		//public methods
		_init : init,
		
		validate : validate
		,
		remove : remove
		,
		extractParameters : extractParameters
		
	};
})();

function CalculationStepExecutionVariableParameters(varName, max, min, increment) {
	this.varName = varName;
	this.max = max;
	this.min = min;
	this.increment = increment;
}