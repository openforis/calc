/**
 * 
 * Manager for calculation step execution results
 * 
 * @author Mino Togna
 *  
 */
function CalculationStepResultsManager(container) {
	// main ui container
	this.container = container;
	// results containter
	this.resultsContainer = this.container.find('.results-container');
	
	// data visualisation manager
	this.dataManager = new DataVisualisationManager(this.resultsContainer);
};

CalculationStepResultsManager.prototype = (function() {
	
	/**
	 *  init function
	 * @param callback
	 */
	var init = function() {
//		var $this = this;
		/*
		 * event handlers
		 */
	};
	
	/**
	 * show results section and set the data provider to the data visualization manager
	 */
	var show = function(dataProvider) {
		//show main container
		this.dataManager.show(dataProvider);
		this.container.fadeIn(400);
	};
	
	var hide = function () {
		this.container.hide();
	};
	
	//prototype
	return {
		constructor : CalculationStepResultsManager,
		
		//public methods
		_init : init,
		//show / hide 
		show : show
		,
		hide : hide
		
	};
})();
