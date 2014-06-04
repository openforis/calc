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

CategoryManager.prototype.getCategoryLevelClasses = function( categoryId , doneFunction ) {
	var url =  this.contextPath + "/"+ categoryId +"/level/classes.json";
	$.ajax({
		url		: url ,
		method 	: "GET" ,
		type	: "json" 
	}).done(function(response){
		var classes = response.fields.classes;
		if( doneFunction ){
			doneFunction( classes );
		}
	}).error(function(){
		Calc.error.apply( this, arguments );
	});
};

var _categoryManager = null;
CategoryManager.getInstance = function(){
	if( !_categoryManager ){
		_categoryManager = new CategoryManager();
	}
	return _categoryManager;
};