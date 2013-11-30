/**
 * Manager operations with job
 * @author Mino Togna
 */

JobManager = function() {
	this.contextPath = "rest/job";
};

JobManager.prototype = (function(){
	
	/**
	 * execute a job for the calculation step with id stepId
	 */
	var executeCalculationStep = function(stepId, success, hideModalStatusOnComplete){
		var $this = this;
		console.log("executeCalculationStep");
		
		$.ajax({
			url : $this.contextPath + "/step/"+stepId+"/execute.json",
			dataType:"json"
		}).done(function(response) {
			console.log("job executed");
			console.log(response);
			checkJobStatus(success, false, hideModalStatusOnComplete);
//			$.proxy(setActiveWorkspace, $this)( response, success );
		});
	};
	
	return {
		constructor : JobManager
		,
		executeCalculationStep : executeCalculationStep
	};
	
})();

