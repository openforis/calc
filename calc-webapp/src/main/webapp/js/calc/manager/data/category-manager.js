/**
 * Data manager for categories
 * @author Mino Togna 
 */
CategoryManager = function(){
	this.contextPath = "rest/workspace/active/category";
};

/**
 * create a new category
 */
CategoryManager.prototype.create = function( name , caption , categoryClasses , successCallback ){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		
		var data	 			= {};
		data.name 				= name;
		data.caption			= caption;
		data.categoryClasses 	= categoryClasses;
		
		$.ajax({
			url		: $this.contextPath + "/create.json" ,
			method 	: "PUT" ,
			type	: "json" ,
			data	: data
		}).done(function(response){
			
		}).error(function(){
			Calc.error.apply( this, arguments );
		})
		
	});
};

var _categoryManager = null;
CategoryManager.getInstance = function(){
	if( !_categoryManager ){
		_categoryManager = new CategoryManager();
	}
	return _categoryManager;
};