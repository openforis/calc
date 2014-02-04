/**

 * Manager for job operations
 * @author Mino Togna
 */

JobManager = function() {
	this.contextPath = "rest/job";
	// job status modal div
	this.jobStatus = new JobStatus();
	
	// current job in execution
	this.job = null;
};

	/**
	 * execute a job for the calculation step with id stepId
	 */
JobManager.prototype.executeCalculationStep = function(stepId, complete, hideOnComplete){
	var $this = this;
	UI.disableAll();
	$.ajax({
		url : $this.contextPath + "/step/"+stepId+"/execute.json",
		dataType:"json"
	}).done(function(response) {
		$this.start(response, complete, hideOnComplete);
//		$.proxy(start, $this)(response, complete, hideOnComplete);
	})
	.error(function(e){
		console.log("error!!! on exec");
		console.log(e);
	});
};
	
/**
 * execute a job for testing the calculation step with id stepId
 */
JobManager.prototype.executeCalculationStepTest = function(stepId, complete, variableParameters, hideOnComplete) {
	var $this = this;
	var data = {stepId: stepId, variables: JSON.stringify(variableParameters)};
	$.ajax({
		url : $this.contextPath + "/test/execute.json",
		type: "POST", 
		data: data,
		dataType:"json"
	}).done(function(response) {
//		$.proxy(start, $this)(response, complete, hideOnComplete);
		$this.start(response, complete, hideOnComplete);
	})
	.error(function(e){
		console.log("error!!! on exec");
		console.log(e);
	});
};
	
// execute all steps for active workspace
JobManager.prototype.execute = function(complete) {
	var $this = this;
	UI.disableAll();
	$.ajax({
		url : $this.contextPath + "/execute.json",
		dataType:"json"
	}).done(function(response) {
//		$.proxy(start, $this)(response, complete);
		$this.start(response, complete);
	})
	.error(function(e){
		console.log("error!!! on exec");
		console.log(e);
	});
};
	
/**
 * if workspace is locked, it shows the jobstatus modal window
 */
JobManager.prototype.checkJobStatus = function(complete, hideOnComplete) {
	var $this = this;
	// if workspace is locked then shows job status
	WorkspaceManager.getInstance().activeWorkspaceIsLocked(function(locked){
		if(locked === true) {
//			$.proxy(start, $this)(null, complete, hideOnComplete);
			$this.start(null, complete, hideOnComplete);
		} else if ( complete ) {
			complete();
		}
	});
};

JobManager.prototype.start = function(job, complete, hideOnComplete) {
	this.job = job;
	this.jobStatus.show();
	this.updateJobStatus(complete, hideOnComplete);
//	$.proxy(updateJobStatus, this)(complete, hideOnComplete);
};
	
// check the job status for the active workspace 
JobManager.prototype.updateJobStatus = function(complete, hideOnComplete) {
	var $this = this;
	$.ajax({
		url: "rest/workspace/job.json",
		dataType: "json"
	})
	.done(function(response) {
		var job = response;
		
		// if current job id == passed or current job is null (never executed) job updates it
		if( !$this.job || ($this.job && job.id === $this.job.id) ) {
			// update job status ui
			$this.jobStatus.update(job, complete, hideOnComplete);
			switch(job.status) {
				case "PENDING":
				case "RUNNING":
					// update job status
					setTimeout(function(){
						$this.updateJobStatus(complete, hideOnComplete);
//						$.proxy(updateJobStatus , $this)(complete, hideOnComplete);
					}, 100);
					break;
				case "COMPLETED":
				case "FAILED":
					UI.enableAll();
					// done call callback
//						if ( complete ) {
//							complete(job);
//						}
//						if( hideOnComplete === true ) {
//							$this.jobStatus.hide();
//						}
					break;
				default:
			}
		} else {
			// job not yet started and never executed one so far
			setTimeout(function(){
				$this.updateJobStatus(complete, hideOnComplete);
//				$.proxy(updateJobStatus , $this)(complete, hideOnComplete);
			}, 200);
		}
		
	})
	.error(function(e) {
		//TODO
		console.log(e);
	});
		
};
	
//singleton instance of job manager manager
var _jobManager = null;
JobManager.getInstance = function() { 
	if(!_jobManager){
		_jobManager = new JobManager();
	}
	return _jobManager;
};
