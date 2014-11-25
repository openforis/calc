/**
 * Data manager for categories
 * @author Mino Togna 
 */
CategoryManager = function(){
	this.contextPath 		= "rest/workspace/active/category";
	this.workspaceManager 	= WorkspaceManager.getInstance();
};

/**
 * Returns the category with given id (Async call)
 * @param categoryId
 * @param callback
 */
CategoryManager.prototype.getCategory = function( categoryId , callback ){
	var $this = this;
	this.workspaceManager.activeWorkspace( function(ws){
		$.each( ws.categories , function( i , category ){
			if( category.id == categoryId ){
				Utils.applyFunction( callback , category );
				return false;
			}
		});
	})
};

/**
 * Create a new category
 */
CategoryManager.prototype.create = function( params , successCallback ){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		
		$.ajax({
			url		: $this.contextPath + "/create.json" ,
			method 	: "PUT" ,
			type	: "json" ,
			data	: params
		}).done(function(response){
			if( response.status == "OK" ){
				ws.categories = response.fields.categories;
			}
			if( successCallback ){
				successCallback( response );
			}
		}).error(function(){
			Calc.error.apply( this, arguments );
		});
		
	});
};

/**
 * Returns all the classes of the first level associated to the category with the given id 
 * @param categoryId
 * @param doneFunction
 */
CategoryManager.prototype.getCategoryLevelClasses = function( categoryId , doneFunction ) {
	var url =  this.contextPath + "/"+ categoryId +"/level/classes.json";
	$.ajax({
		url		: url ,
		method 	: "GET" ,
		type	: "json" 
	}).done(function(response){
		var classes = response.fields.classes;
		Utils.applyFunction( doneFunction, classes );
	}).error(function(){
		Calc.error.apply( this, arguments );
	});
};

/**
 * Load all categories created from Calc (where original id is null)
 */
CategoryManager.prototype.loadUserDefinedCategories = function( success ){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		var url =  $this.contextPath + "/all/userdefined.json";
		$.ajax({
			url		: url ,
			method 	: "GET" ,
			type	: "json" 
		}).done(function(response){
			var categories = response;
			Utils.applyFunction( success, categories );
		}).error(function(){
			Calc.error.apply( this, arguments );
		});
	});
};

/**
 * Delete the category with the given id
 */
CategoryManager.prototype.remove = function( categoryId , successCallback ){
	var $this = this;
	UI.lock();
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		
		$.ajax({
			url		: $this.contextPath + "/"+ categoryId +"/delete.json" ,
			method 	: "POST" ,
			type	: "json" 
		}).done(function(response){
			
			if( response.status == "OK" ){
				UI.showSuccess( "Category deleted!" , true );
				ws.categories = response.fields.categories;
			} else {
				UI.showError( response.fields.error , true );
			}
			
			UI.unlock();
			Utils.applyFunction( successCallback , response.fields.categoryId );
		}).error(function(){
			Calc.error.apply( this, arguments );
		});
		
	});
};

var _categoryManager = null;
CategoryManager.getInstance = function(){
	if( !_categoryManager ){
		_categoryManager = new CategoryManager();
	}
	return _categoryManager;
};