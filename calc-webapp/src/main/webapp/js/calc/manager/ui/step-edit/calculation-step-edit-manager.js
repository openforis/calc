/**
 * 
 * Manages the editing of the CalculationStep
 *  @author S. Ricci
 *  @author Mino Togna
 */
CalculationStepEditManager = function (container) {
	this.container 				= container;
	this.$form 					= this.container.find('#step-form');
	
	this.currentCalculationStep = null;
	
	// ui form elements
	this.typeInput				= this.$form.find( '[name=type]' );
	this.$entityCombo 			= this.$form.find("[name='entityId']").combobox();
	this.$variableCombo 		= this.$form.find("[name='variableId']").combobox();
	this.$addVariableButton		= this.$form.find("[name=add-variable]");
	
	this.equationListCombo		= this.$form.find( '[name=equationList]' ).combobox();
	this.codeVariableCombo		= this.$form.find( '[name=codeVariable]' ).combobox();
	
	this.categoryCombo			= this.$form.find( '[name=categoryId]' ).combobox();
	
	// sections to show / hide based on the type selection
	this.outputVariableForm 	= this.container.find( ".output-variable-form" );
	this.outputCategoryForm 	= this.container.find( ".output-category-form" );
	
	this.rScriptForm			= this.container.find( ".r-script-form" );
	this.rScriptForm.hide();
	this.equationForm			= this.container.find( ".equation-form" );
	this.equationForm.hide();
	this.categoryForm			= this.container.find( ".category-form" );
	this.categoryForm.hide();
	this.categorySettingsForm = this.container.find( ".category-settings-form" );
	
	// calculation step type buttons
	var typeRScriptButton 	= this.$form.find( 'button[name="type-r-script"]' );
	this.rScriptButton 		= new OptionButton( typeRScriptButton );
	
	var typeCategoryButton 	= this.$form.find( 'button[name="type-category"]' );
	this.categoryButton 	= new OptionButton( typeCategoryButton );
	
	var typeEquationButton 	= this.$form.find( 'button[name="type-equation"]' );
	this.equationButton 	= new OptionButton( typeEquationButton );
	
	//initialized in the init method
	//R script component manager
	var rScriptField = this.$form.find("[name=script]");
	this.$RScript = new RScript(rScriptField);
	
	this.$addVariableButton = this.$form.find( "button[name='add-variable']" );	
	this.addVariableModal 	= new AddVariableModal( this.$addVariableButton , this );
	
	this.addCategoryModal 	= new AddCategoryModal( this.$form.find("button[name=add-category]") , this );
	
	// workspace instance
	this.workspace 	= null;
	
	this.init();
};

/**
 * Initialize the event handlers and populate the form with initial data
 *  
 * @param callback
 */
CalculationStepEditManager.prototype.init = function( callback ) {
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( function(ws) {
		// i can use workspace as instance because there aren't any changes here to it
		$this.workspace = ws;
		
		// init event handlers
		$this.initEventHandlers();
		
		// populate entity select 
		$this.$entityCombo.data( ws.entities, "id", "name" );
		
		// disable / enable type buttons
		if( $this.workspace.equationLists.length == 0 || stepId ) {
			// disable type equation
			UI.disable( $this.equationButton.button );
		} else {
			// enable type equations
			$this.equationListCombo.data( $this.workspace.equationLists , "id" , "name" );
		}

		// load step if necessary
		var url = window.sectionUrl;
		var stepId = $.url(url).param("id");
		if ( stepId ) {
			UI.lock();
			CalculationStepManager.getInstance().load (stepId, function(response) {
				
				$this.currentCalculationStep = response;
				$this.updateForm();
				
				// disable all type buttons
				UI.disable( $this.rScriptButton.button );
				UI.disable( $this.equationButton.button );
				UI.disable( $this.categoryButton.button );
				
				UI.disable( $this.addVariableModal.triggerButton );
				UI.disable( $this.addCategoryModal.triggerButton );
				
				$this.$entityCombo.disable();
				$this.$variableCombo.disable();
				$this.categoryCombo.disable();
			});
		} else {
			// default settings
			$this.rScriptButton.select();
		}
		
		// populate category combobox
		$this.categoryCombo.data( $this.workspace.userDefinedcategories() , "id" , "caption" );
	});
};
	
/**
 * Init input fields event listeners
 */
CalculationStepEditManager.prototype.initEventHandlers = function() {
	var $this = this;
	
	this.$form.submit(function(event) {
		event.preventDefault();
	});
	
	//entity select change handler
	this.$entityCombo.change( $.proxy( $this.entityChange , $this ) ) ;
	
	// on submit button click 
	this.$form.find("button[type='submit']").click(function(event){
		event.preventDefault();
		$this.save( function(){
			UI.showSuccess("Saved!",true);
		});
	});
	
	// at input change, it keeps track that the form has changed
	this.$form.find(":input").change(function() {
		$this.$form.data('changed', true);
	});
	
	// selection / deselection of type buttons
	this.rScriptButton.select( function() {
		UI.disable( this.button );
		$this.equationButton.deselect();
		$this.categoryButton.deselect();
		
		$this.typeInput.val( "SCRIPT" );
		$this.rScriptForm.fadeIn( 300 );
	});
	this.rScriptButton.deselect( function() {
		UI.enable( this.button );
		
		$this.typeInput.val( "" );
		$this.rScriptForm.hide();
	});

	this.categoryButton.select( function() {
		UI.disable( this.button );
		$this.rScriptButton.deselect();
		$this.equationButton.deselect();
		
		// hide variable from selection
		$this.outputVariableForm.hide();
		$this.outputCategoryForm.fadeIn();
		
		$this.typeInput.val( "CATEGORY" );
		$this.categoryForm.fadeIn( 300 );
	});
	this.categoryButton.deselect( function() {
		UI.enable( this.button );
		
		// show variable form section
		$this.outputCategoryForm.hide();
		$this.outputVariableForm.fadeIn();
		
		$this.typeInput.val( "" );
		$this.categoryForm.hide();
	});
	
	this.equationButton.select( function() {
		UI.disable( this.button );
		$this.rScriptButton.deselect();
		$this.categoryButton.deselect();
		
		$this.typeInput.val( "EQUATION" );
		$this.equationForm.fadeIn( 300 );
	});
	this.equationButton.deselect( function() {
		UI.enable( this.button );
		
		$this.typeInput.val( "" );
		$this.equationForm.hide();
	});
	
	
	this.equationListCombo.change( $.proxy( this.equationListChange , $this ) ) ;
	
	this.categoryCombo.change( $.proxy( this.categoryChange , $this ) ) ;
};

/**
 * Create or update the calculation step according to the field values in the form 
 */
CalculationStepEditManager.prototype.save = function( successCallback, errorCallback ){
	UI.lock();
	var $this = this;
	var $step = $this.$form.serialize();
	CalculationStepManager.getInstance().save( $step,
		//success
		function(response) {
	    	UI.Form.updateErrors($this.$form, response.errors);
	    	if(response.status == "ERROR" ) {
	    		UI.showError("There are errors in the form. Please fix them before proceeding.", true);
	    		
	    		if ( errorCallback ) {
	    			errorCallback();
	    		}
	    	} else {
	    		$this.currentCalculationStep = response.fields.calculationStep;
	    		$this.updateForm();
		    	
	    		if( successCallback ){
		    		successCallback( $this.currentCalculationStep );
	    		};
	    	}
	    	
	    	UI.unlock();
		},
		//error
		function(e) {
	    	UI.showError("An error occured. Please check the log file.", false);
	
			if ( errorCallback ) {
				errorCallback(e);
			}
		},
		//complete
		function() {
			//reset changed state
//			$this.$form.data('changed', false);
//			UI.unlock();
		}
	);
};
	
/**
 * Save the calculation step form only if it has changed, else calls the callback synchronously
 */
CalculationStepEditManager.prototype.saveIfChanged = function( success ) {
	if( this.$form.data('changed') ) {
		this.save( success );
	} else {
		success( this.currentCalculationStep );
	}
};
	
/**
 * Update form with currentCalculationStep instance
 */
CalculationStepEditManager.prototype.updateForm = function() {
	this.$entityCombo.val( this.currentCalculationStep.outputEntityId );
	this.entityChange();

	this.$variableCombo.val( this.currentCalculationStep.outputVariableId );
	
	UI.Form.setFieldValues( this.$form, this.currentCalculationStep );
	
	var params = this.currentCalculationStep.parameters;
	switch ( this.currentCalculationStep.type ) {
		
		case "SCRIPT" :
			this.rScriptButton.select();
			break;
		
		case "EQUATION" :
			this.equationButton.select();
			// populate equation form
			
			var equationListId = this.currentCalculationStep.equationListId;
			this.equationListCombo.val( equationListId );
			this.equationListChange();

			var codeVariable = this.getSelectedEntity().getVariableById( params.codeVariable );
			this.codeVariableCombo.val( codeVariable.id );
			
			for( var i in params.variables ){
				var variableOption = params.variables[i];
				var eqVar = variableOption.equationVariable;
				var equiationVariableId = variableOption.variableId;
				this.equationListVariableCombos[ eqVar ].val( equiationVariableId );
			}
			
			this.$RScript.$inputField.val( "" );
			break;
		
		case "CATEGORY" :
			this.categoryButton.select();
			this.$RScript.$inputField.val( "" );
			
			this.categoryCombo.val( params.categoryId );
			
			var classes 	= params.categoryClassParameters;
			this.categoryChange( function(){

				for( var i in classes ){
					var classSettings = classes[i]; 
					var classOption = this.categoryClassSettings[ classSettings.classId ];
					
					classOption.variableCombo.val( classSettings.variableId );
					classOption.conditionSelect.val( classSettings.condition );
					classOption.input1.val( classSettings.left );
					classOption.input2.val( classSettings.right );
				}
				
			});
			
			break;
	}
	
	UI.unlock();
	
	//reset changed state 
	this.$form.data( 'changed', false );
};
	
/**
 * Returns the selected entity in the form
 */
CalculationStepEditManager.prototype.getSelectedEntityId = function() {
	var entityId = this.$entityCombo.val();
	return entityId;
};
CalculationStepEditManager.prototype.getSelectedEntity = function() {
	var entityId = this.getSelectedEntityId();
	var entity = this.workspace.getEntityById( entityId );
	return entity;
};
/**
 * Function called when the entity combo change 
 */
CalculationStepEditManager.prototype.entityChange = function() {
	var entityId = this.getSelectedEntityId();
	var entity = this.workspace.getEntityById(entityId);
	
	if ( entity ){
		// populate fields that need the entity
		// r script
		this.$RScript.entity = entity;
		
		// variable select
		this.$variableCombo.enable();
		this.updateVariableSelect();
		
		UI.enable( this.$addVariableButton );
		
		this.equationListCombo.enable();
		
		this.categoryCombo.enable();
		UI.enable( this.addCategoryModal.triggerButton );
		
	} else {
		// Entity not selected reset fields that need the entity
		this.$RScript.entity = null;
		UI.disable( this.$addVariableButton );
		
		this.$variableCombo.reset();
		this.$variableCombo.disable();
	
		// not necessary. it should be easier to deselect the combobox...
		this.equationListCombo.val( null );
//		this.equationListCombo.reset();
//		this.equationListCombo.data( this.workspace.equationLists , "id" , "name" );
		
		this.equationListCombo.disable();
		this.equationListChange();
		
		this.codeVariableCombo.reset();
		this.codeVariableCombo.disable();
		
		this.categoryCombo.val( null );
		this.categoryCombo.disable();
		UI.disable( this.addCategoryModal.triggerButton );
		this.categorySettingsForm.empty();
	}
};

/**
 * Populate the "output variable" select according to the selected parent entityId.
 */
CalculationStepEditManager.prototype.updateVariableSelect = function() {
	var entity = this.getSelectedEntity();
	if( entity ) {
		var variables = entity.quantitativeOutputVariables();
		this.$variableCombo.data( variables, "id", "name" );
	}
};

CalculationStepEditManager.prototype.show = function() {
	this.container.fadeIn(400);
};

CalculationStepEditManager.prototype.hide = function() {
	this.container.hide();
};

/**
 * Handler for equation list combo box change event
 */
CalculationStepEditManager.prototype.equationListChange = function () {
	var listId = this.equationListCombo.val();
	var equationList = this.workspace.getEquationList( listId );
	
	this.equationForm.find( '.eq-variable' ).remove();
	if( equationList ) {
		this.codeVariableCombo.enable();
		this.codeVariableCombo.data( this.getSelectedEntity().getAncestorsVariables() , "id" , "name" );
		
		var vars = equationList.parameters.variables;
		this.equationListVariableCombos = [];
		for( var i in vars ){
			var variable = vars[i];
			
			var div = $( '<div class="form-group eq-variable">' );
			var label = $( '<label class="col-md-2 control-label"></label>' );
			label.html( "Variable '" +variable+ "'");
			div.append( label );

			var divSelect 		= $( '<div class="col-md-10">' );
			var select 			= $( '<select class="form-control"></select>' );
			var selectNameAttr 	= "equationVariables['" + variable + "']"; 
			select.attr( "name" , selectNameAttr );
			divSelect.append( select );	
			var combo = select.combobox();
			combo.data( this.getSelectedEntity().getAncestorsVariables() , "id" , "name" );
			this.equationListVariableCombos[ variable ] = combo;
			div.append( divSelect );
			
			div.hide();
			this.equationForm.find( ".form-container" ).append( div );
			div.fadeIn();
		}
	} else {
		this.codeVariableCombo.disable();
		this.codeVariableCombo.reset();
	}
};

CalculationStepEditManager.prototype.categoryChange = function( callback ){
	this.categorySettingsForm.empty();
	
	var $this = this;
	var categoryId = this.categoryCombo.val();
	$this.categoryClassSettings = [];
	if( categoryId ){
		UI.lock();
		CategoryManager.getInstance().getCategoryLevelClasses( categoryId, function(classes){
			// add headers
			var container = $( '<div class="row"></div>' );
			var divCode = $( '<div class="col-md-2">Code</div>' );
			container.append( divCode );
			var divVar = $( '<div class="col-md-3">Variable</div>' );
			container.append( divVar );
			var divVarFiler = $( '<div class="col-md-6">Condition</div>' );
			container.append( divVarFiler );
			$this.categorySettingsForm.append( container );
			
			$.each( classes , function( i , cls ){
				if( cls.code != '-1' ){
					var option = new CategoryClassOption( cls , $this , $this.categorySettingsForm );
//					$this.categorySettingsForm.append( option.container );
					$this.categoryClassSettings[ cls.id ] = option;
				}
			});
			UI.unlock();
			
			if( Utils.isFunction( callback) ){
				callback.apply( $this );
			}
		});
	}
};
