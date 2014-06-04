/**
* Add category modal window manager
* @author Mino Togna
* 
*/

AddCategoryModal = function( triggerButton , calcStepEditManager ){
	// calculation step edit manager instance
	this.calcStepEditManager 		= calcStepEditManager;
	// button that triggers the opening of the add variable modal form
	this.triggerButton 				= triggerButton;
	// modal container
	this.container 					= $( '#add-category-form' );
	// form to submit
	this.form 						= this.container.find('form');
	
	this.categoryClassesContainer 	= this.container.find( ".category-classes" );
	
	// save button form
	this.saveButton 				= this.container.find('button.save');

	// contains the selected classes by the user
	this.categoryClasses			= [];
	
	this.init();
};

AddCategoryModal.prototype.init = function(){
	var $this = this;
	
	//add variable button click
	this.triggerButton.click(function(event){
		event.preventDefault();
		
		// reset form
		UI.Form.reset( $this.form );
		$this.categoryClassesContainer.empty();
		$this.categoryClasses			= [];
		// add empty row
		$this.addRow();
		
		// open the modal 
		$this.container.modal( {keyboard: true, backdrop: "static"} );
		
		//set focus on first field in form
		setTimeout(function() {
			UI.Form.setFocus( $this.form );	
		}, 500);
	});
	
	var saveCategoryCallback = function( response ){
		
		UI.Form.updateErrors( $this.form , response.errors );
		
		if( response.status == "OK" ){
			WorkspaceManager.getInstance().activeWorkspace( function(ws){
				var categoryId = response.fields.categoryId;
				
				$this.calcStepEditManager.workspace = ws;
				$this.calcStepEditManager.categoryCombo.data( ws.userDefinedcategories() , "id" , "caption" );
				$this.calcStepEditManager.categoryCombo.val( categoryId );
				$this.calcStepEditManager.categoryChange();
				
				$this.container.modal('hide');
				$this.container.modal('removeBackdrop');
			});
		}
		UI.unlock();
		
	};
	
	this.saveButton.click( function(e){
		e.preventDefault();
		UI.lock();
		
//		var name 	= $this.form.find( 'input[name=name]' ).val();
		var caption = $this.form.find( 'input[name=caption]' ).val();
		var classes = JSON.stringify( $this.categoryClasses );
		
		// update input names
		var codes = $this.container.find(".category-class-row input.code");
		$.each( codes, function(i, input){
			$( input ).attr( "name" , "codes["+i+"]" );
		});
		var captions = $this.container.find(".category-class-row input.caption");
		$.each( captions, function(i, input){
			$( input ).attr( "name" , "captions["+i+"]" );
		});
		
		var params = $this.form.serialize();
		
		CategoryManager
			.getInstance()
			.create( params , saveCategoryCallback );
	});
	
	
};
// add new category class row
AddCategoryModal.prototype.addRow = function() {
	var row = new CategoryClassRow( this );
	this.categoryClasses.push( row );
	this.updateRowButtons();
};

AddCategoryModal.prototype.deleteRow = function( categoryClassRow , rowDiv ){
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

AddCategoryModal.prototype.updateRowButtons = function() {
	this.container.find(".category-class-row").find( "[name=delete-btn]" ).visible();
	if( this.categoryClasses.length == 1 ){
		this.container.find( ".category-class-row:first" ).find( "[name=delete-btn]" ).invisible();
	}

	this.container.find( ".category-class-row:not(:last)" ).find( "[name=add-btn]" ).invisible();
	this.container.find( ".category-class-row:last" ).find( "[name=add-btn]" ).visible();
};

CategoryClassRow = function( addCategoryModal ) {
	var $this = this;
	var container = addCategoryModal.categoryClassesContainer;
	
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
	
	var divCaption 	= $( '<div class="col-md-7"></div>' );
	row.append( divCaption );
	var inputCaption	= $( '<input type="text" class="form-control width100 caption">' );
	inputCaption.change( function(){
		$this.caption = $( this ).val();
	});
	divCaption.append( inputCaption );

	var deleteBtn 	= $( '<button type="button" class="btn no-background col-md-1" name="delete-btn"><i class="fa fa-minus-square-o"></i></button>' );
	deleteBtn.click( function(e){
		e.preventDefault();
		addCategoryModal.deleteRow( $this , row );
	});
	row.append( deleteBtn );
	
	var addBtn 		= $( '<button type="button" class="btn no-background col-md-1" name="add-btn"><i class="fa fa-plus-square-o"></i></button>' );
	addBtn.click( function(e){
		e.preventDefault();
		addCategoryModal.addRow();
	});
	row.append( addBtn );
	
	row.fadeIn();
};
