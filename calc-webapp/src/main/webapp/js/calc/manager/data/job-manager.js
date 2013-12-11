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
	var executeCalculationStep = function(stepId, success, hideJobStatusOnComplete){
		var $this = this;
		$.ajax({
			url : $this.contextPath + "/step/"+stepId+"/execute.json",
			dataType:"json"
		}).done(function(response) {
//			console.log("job manager job executed");
//			console.log(response);
			checkJobStatus(success, false, hideJobStatusOnComplete);
		})
		.error(function(e){
			console.log("error!!! on exec");
			console.log(e);
		});
	};
	
	// execute all steps for active workspace
	var execute = function(complete) {
		var $this = this;
		$.ajax({
			url : $this.contextPath + "/execute.json",
			dataType:"json"
		}).done(function(response) {
			checkJobStatus(complete, false);
		})
		.error(function(e){
			console.log("error!!! on exec");
			console.log(e);
		});
	};
	
	return {
		constructor : JobManager
		,
		executeCalculationStep : executeCalculationStep
		,
		execute : execute
	};
	
})();

//singleton instance of job manager manager
var _jobManager = null;
JobManager.getInstance = function() { 
	if(!_jobManager){
		_jobManager = new JobManager();
	}
	return _jobManager;
};