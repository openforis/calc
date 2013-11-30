/**
 * 
 * Manager for calculation step execute and test sections
 * 
 * @author Mino Togna
 *  
 */
function CalculationStepExecManager(container) {
	// main ui container
	this.container = container;
	// header
	this.header = this.container.find(".header");
	//exec btn
	this.execButton = this.container.find('button[name=exec-btn]');
	
	//calc step associated. it's set by the show method 
	this.calculationStep = null;
	
	//job manager
	this.jobManager = new JobManager();
	
	this._init();
};

CalculationStepExecManager.prototype = (function() {
	
	/**
	 *  
	 * @param callback
	 */
	var init = function(callback) {
		var $this = this;
		
		/*
		 * event handlers
		 */
		//execute step
		this.execButton.click(function(e) {
			$this.jobManager.executeCalculationStep(
					$this.calculationStep.id, 
					//on complete show results
					function(job) {
						console.log("job finished");
						console.log(job);
					}
					, true
			);
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
		
		//show main container
		this.container.fadeIn(400);
	};
	
	var hide = function () {
		this.container.hide();
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
