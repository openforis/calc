/**
 * Manager for job operations
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
		$.ajax({
			url : $this.contextPath + "/step/"+stepId+"/execute.json",
			dataType:"json"
		}).done(function(response) {
			checkJobStatus(success, false, hideModalStatusOnComplete);
		});
	};
	
	return {
		constructor : JobManager
		,
		executeCalculationStep : executeCalculationStep
	};
	
})();

