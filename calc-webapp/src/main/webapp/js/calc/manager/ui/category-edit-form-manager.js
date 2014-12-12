/**
* Category edit form manager
* 
* @author Mino Togna
* 
*/
CategoryEditFormManager = function( container ){
	//  container
	this.container 					= $( container );
	// form to submit
	this.form 						= this.container.find( "form" );
	this.captionField				= this.form.find( '[name=caption]' );
	this.categoryIdField			= this.form.find( '[name=categoryId]' );
	
	this.categoryClassesContainer 	= this.form.find( ".category-classes" );
	
	// save button form
	this.saveButton 				= this.form.find( 'button.save' );

	// contains the classes created by the user
	this.categoryClasses			= [];
	
	// on save function. to override externally
	this.onSave 		= function( categoryId ){};
	
	// category model manager
	this.categoryManager	= CategoryManager.getInstance();
	
	this.init();
};

CategoryEditFormManager.prototype.init = function( categoryId ){
	var $this = this;
	
	this.saveButton.click( function(e){
		e.preventDefault();
		
		// update input field names
		var codes = $this.container.find(".category-class-row input.code");
		$.each( codes, function(i, input){
			$( input ).attr( "name" , "codes["+i+"]" );
		});
		var captions = $this.container.find(".category-class-row input.caption");
		$.each( captions, function(i, input){
			$( input ).attr( "name" , "captions["+i+"]" );
		});
		
		var params = $this.form.serialize();
		
		var saveCategoryCallback = function( response ){
			UI.Form.updateErrors( $this.form , response.errors );
			
			if( response.status == "OK" ){
				UI.showSuccess( "Saved!" , true );
				WorkspaceManager.getInstance().activeWorkspace( function(ws){
					var categoryId = response.fields.categoryId;
					Utils.applyFunction( $this.onSave , categoryId );
				});
			}
		};
		
		CategoryManager
			.getInstance()
			.create( params , saveCategoryCallback );
	});
	
};
/**
 * Hide form
 */
CategoryEditFormManager.prototype.hide = function(){
	this.container.hide();
};

/**
 * Show form
 * @param optional param to preload form with given categoryId
 */
CategoryEditFormManager.prototype.show = function( categoryId ) {
	UI.Form.reset( this.form );
	this.categoryClassesContainer.empty();
	this.categoryClasses			= [];
	
	var $this = this;
	var showForm = function(){
		$this.container.fadeIn();
		setTimeout(function() {
			UI.Form.setFocus( $this.form );	
		}, 500);
	};
	
	if( categoryId ){
		// populate form with category fields
		$this.categoryManager.getCategory( categoryId , function(category){
			$this.captionField.val( category.caption );
			$this.categoryIdField.val( category.id );

			// populate form with category classes
			$this.categoryManager.getCategoryLevelClasses( categoryId , function(classes){
				$.each( classes , function( i , cls ){
					$this.addRow( cls.code, cls.caption );
				});
			});
			
			showForm();
		} );
		
	} else {
		this.addRow();
		
		showForm();
	}
};

// add new category class row
CategoryEditFormManager.prototype.addRow = function( code , caption ) {
	var row = new CategoryClassRow( this , code , caption );
	this.categoryClasses.push( row );
	this.updateRowButtons();
};

CategoryEditFormManager.prototype.deleteRow = function( categoryClassRow , rowDiv ){
	for(var i in this.categoryClasses){
		var r = this.categoryClasses[i];
		if( r == categoryClassRow ){
			rowDiv.fadeOut(200);
			setTimeout( $.proxy( function(){
				rowDiv.remove();
				this.updateRowButtons();
			} , this ) , 140);
			this.categoryClasses.splice(i, 1);
			break;
		}
	}
};

CategoryEditFormManager.prototype.updateRowButtons = function() {
	this.container.find(".category-class-row").find( "[name=delete-btn]" ).visible();
	if( this.categoryClasses.length == 1 ){
		this.container.find( ".category-class-row:first" ).find( "[name=delete-btn]" ).invisible();
	}

	this.container.find( ".category-class-row:not(:last)" ).find( "[name=add-btn]" ).invisible();
	this.container.find( ".category-class-row:last" ).find( "[name=add-btn]" ).visible();
};

CategoryClassRow = function( formManager , code , caption ) {
	var $this = this;
	var container = formManager.categoryClassesContainer;
	
	this.code = null;
	this.caption = null;
	
	// add row to the modal window
	var row = $( '<div class="row no-margin category-class-row" style="margin-bottom: 2px"></div>' );
	row.hide();
	container.append( row );
	
	var divCode 	= $( '<div class="col-md-3"></div>' );
	row.append( divCode );
	var inputCode	= $( '<input type="text" class="form-control width100 code">' );
	inputCode.change( function(){
		$this.code = $( this ).val();
	});
	divCode.append( inputCode );
	if( code ){
		inputCode.val( code );
	}
	
	var divCaption 	= $( '<div class="col-md-7"></div>' );
	row.append( divCaption );
	var inputCaption	= $( '<input type="text" class="form-control width100 caption">' );
	inputCaption.change( function(){
		$this.caption = $( this ).val();
	});
	divCaption.append( inputCaption );
	if( caption ){
		inputCaption.val( caption );
	}

	var deleteBtn 	= $( '<button type="button" class="btn no-background col-md-1" name="delete-btn"><i class="fa fa-minus-square-o"></i></button>' );
	deleteBtn.click( function(e){
		e.preventDefault();
		formManager.deleteRow( $this , row );
	});
	row.append( deleteBtn );
	
	var addBtn 		= $( '<button type="button" class="btn no-background col-md-1" name="add-btn"><i class="fa fa-plus-square-o"></i></button>' );
	addBtn.click( function(e){
		e.preventDefault();
		formManager.addRow();
	});
	row.append( addBtn );
	
	row.fadeIn();
};
