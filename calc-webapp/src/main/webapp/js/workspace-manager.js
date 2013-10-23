var WorkspaceManager = {

	/**
	 * Load all the entities from the active workspace and call the callback function
	 * 
	 * @param callback
	 */
	loadEntities: function(callback) {
		$.ajax({
			url: "rest/workspace/entities.json",
			dataType: "json"
		})
		.done(function(response) {
			callback(response);
		});
	},
	/**
	 * Load all the quantitative variables of the specified entity and call the callback function
	 * 
	 * @param entityId
	 * @param callback
	 */
	loadQuantitativeVariables: function(entityId, callback) {
		$.ajax({
			url: "rest/workspace/entities/" + entityId + "/qtyvariables.json",
			dataType: "json"
		})
		.done(function(response) {
			callback(response);
		});
	}
		
};