/**
 * manager for category edit page
 * @author Mino Togna
 */

CategoryEditManager = function( container ){	
	this.container 			= $( container );
	
	this.categoryManager	= CategoryManager.getInstance();
	
	// list section
	this.listSection 	= this.container.find( ".list-section" );
	this.categories		= this.listSection.find( ".categories" );

	// add button
	this.addBtn			= this.listSection.find( 'button[name=add]' );
	
	// form section
	this.formSection 				= this.container.find( ".form-section" );
	this.categoryEditFormManager 	= new CategoryEditFormManager( this.formSection );
	this.categoryEditFormManager.hide();
	
	this.categoryButtons	= [];
	
	this.init();
};

CategoryEditManager.prototype.init = function(){
	var $this = this;
	// add button click handler
	this.addBtn.click( function( e ){ 
		$this.categoryEditFormManager.show();
	});
	
	// load categories
	this.loadCategories();
	
	var saveCallback = function( categoryId ){
		$this.loadCategories( function(){
			// after loading all categories select the one just saved
			$.each( $this.categoryButtons , function( i , optionBtn){ 
				if( i == categoryId ){
					optionBtn.select();
					return false;
				}
			});
		});
		
	};
	this.categoryEditFormManager.onSave = saveCallback;
};

/**
 * load all output categories
 */
CategoryEditManager.prototype.loadCategories = function( callback ) {
	this.categories.empty();
	
	var $this = this;
	this.categoryManager.loadUserDefinedCategories( function(categories){
		$.each( categories , function( i , category ){

			var addButton = function( ){
				
				var div = $( '<div class="row no-margin no-padding width100"></div>')
				div.hide();
				$this.categories.append( div );
				
				var btn = $( '<button class="btn option-btn col-md-10"></button>' );
				btn.html( category.caption );
				div.append( btn );
				
				var deleteBtn = $( '<button class="btn no-background col-md-2"><i class="fa fa-minus-square"></i></button>' );
				div.append( deleteBtn );
				deleteBtn.click( function(){

					var position = deleteBtn.offset();
					position.top -= 20; 
					position.left -= 20;
					UI.showConfirm( 
							"Do you want to delete " + category.caption + " category? This operation cannot be undone." , 
							function(){
								$this.categoryManager.remove( category.id, function(categoryId){
									if( categoryId ){
										$this.categoryEditFormManager.hide();
										$this.loadCategories();
									}
								});
							} , 
							null,
							position
							)
				});
				
				// show the row 
				setTimeout( function(){
					div.fadeIn();
				} , i * 75);
				
				
				var optionBtn = new OptionButton( btn );
				
				var select = function( cat ) {
					for( var j in $this.categoryButtons ){
						if( j != cat.id ) {
							var optionBtn = $this.categoryButtons[ j ] ; 
							optionBtn.deselect();
						}
					}
					
					$this.categoryEditFormManager.show( cat.id );
				};
				optionBtn.select( select , category );
				
				var deselect = function( cat ) {
					$this.categoryEditFormManager.hide( );
				};
				optionBtn.deselect( deselect , category );
				
				$this.categoryButtons[ category.id ] = optionBtn;
			};
			
			addButton();
		});
		
		Utils.applyFunction( callback );
	});
};
