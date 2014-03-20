/**
 * Manager for variable objects
 * @author Mino Togna
 */

VariableManager = function() {
	
    this.contextPath = "rest/workspace/active/variable/";

};

/**
 * It returns the categories associated with the given variable
 * max 25 items will be returned, otherwise null
 */
VariableManager.prototype.getCategories = function( variableId , success ) {
    WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws) {
	
		$.ajax({
		    url		: this.contextPath + variableId + "/categories.json" ,
		    dataType: "json" ,
		    data	: { max : 35 }
		}).done( function(response) {
		    var categories = response.fields.categories;
		    success( categories );
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		});
		
    } , this ));
    
};
	

// singleton instance of variable manager
var _variableManager = null;
VariableManager.getInstance = function() { 
	if( ! _variableManager ) {
		_variableManager = new VariableManager();
	}
	return _variableManager;
};

