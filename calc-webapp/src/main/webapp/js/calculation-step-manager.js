var CalculationStepManager = {
	loadAll: function(callback) {
		
	},
	load: function(id, callback) {
		$.ajax({
			url:"rest/calculationstep/"+id+"/load.json",
			dataType:"json"
		})
		.done(function(response){
			callback(response);
		});
	},
	execute: function(id, callback) {
		$.ajax({
			url:"rest/calculationstep/"+id+"/run.json",
			dataType:"json",
			async: false 
		})
		.done(function(response){
			callback(response);
		});
	},
	updateForm: function(step, form) {
		
	},
	updateHomePage: function() {
		
	},
	updateHomePageStepElement: function(id) {
		
	}
};