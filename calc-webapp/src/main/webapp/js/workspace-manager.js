function WorkspaceManager() {};

/**
 * Load all the entities from the active workspace and call the callback function
 * 
 * @param callback
 */
WorkspaceManager.loadEntities = function(callback) {
	$.ajax({
		url: "rest/workspace/entities.json",
		dataType: "json"
	})
	.done(function(response) {
		callback(response);
	});
};

/**
 * Load all the quantitative variables of the specified entity and call the callback function
 * 
 * @param entityId
 * @param callback
 */
WorkspaceManager.loadQuantitativeVariables = function(entityId, callback) {
	$.ajax({
		url: "rest/workspace/entities/" + entityId + "/qtyvariables.json",
		dataType: "json"
	})
	.done(function(response) {
		callback(response);
	});
};

WorkspaceManager.saveVariable = function(variable, success, error, complete) {
	$.ajax({
		url: "rest/workspace/variable/save.json",
		dataType: "json",
		type: "POST",
		data: variable
	})
	.done(function(response) {
		if ( success ) {
			success(response);
		}
	})
	.error(function(e) {
		if ( error ) {
			error(e);
		}
	})
	.complete(function() {
		if ( complete ) {
			complete();
		}
	});
};