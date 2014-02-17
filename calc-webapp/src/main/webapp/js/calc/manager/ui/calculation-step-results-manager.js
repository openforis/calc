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

/**
 * show results section and set the data provider to the data visualization manager
 */
CalculationStepResultsManager.prototype.show = function(dataProvider) {
	//show main container
	this.dataManager.show(dataProvider);
	this.container.fadeIn(400);
};
	
CalculationStepResultsManager.prototype.hide = function () {
	this.container.hide();
};	

CalculationStepResultsManager.prototype.showDataVisOptions = function() {
    this.dataManager.showOptions();
};

CalculationStepResultsManager.prototype.hideDataVisOptions = function() {
    this.dataManager.hideOptions();
};
