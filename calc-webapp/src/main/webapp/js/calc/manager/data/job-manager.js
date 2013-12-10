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
//			console.log("job manager job executed");
//			console.log(response);
			checkJobStatus(success, false, hideModalStatusOnComplete);
		})
		.error(function(e){
			console.log("error!!! on exec");
			console.log(e);
		});
	};
	
	/**
	 * Executes a job for the calculation step test with id stepId
	 */
	var testCalculationStep = function(stepId, parameters, success, hideModalStatusOnComplete){
		var $this = this;
		$.ajax({
			url : $this.contextPath + "/step/"+stepId+"/test.json",
			type: "POST", 
			data: JSON.stringify(parameters),
			dataType: "json",
			contentType: "application/json"
		}).done(function(response) {
//			console.log("job manager job executed");
//			console.log(response);
			checkJobStatus(success, false, hideModalStatusOnComplete);
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
		testCalculationStep : testCalculationStep
	};
	
})();

