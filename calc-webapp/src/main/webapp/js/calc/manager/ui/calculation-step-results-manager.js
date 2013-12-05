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
	// results container for data visualisation manager
	var resultsContainer = this.container.find('.results-container');
	this.dataManager = new DataVisualisationManager(resultsContainer);
};

CalculationStepResultsManager.prototype = (function() {
	
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
		constructor : CalculationStepResultsManager
		,	
		//show / hide 
		show : show
		,
		hide : hide
		
	};
})();
