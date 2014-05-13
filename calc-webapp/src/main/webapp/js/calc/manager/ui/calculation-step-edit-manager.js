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
	this.$entityCombo 			= this.$form.find("[name='entityId']").combobox();
	this.$variableCombo 		= this.$form.find("[name='variableId']").combobox();
	this.$addVariableButton		= this.$form.find("[name=add-variable]");
	
	this.equationListCombo		= this.$form.find( '[name=equationList]' ).combobox();
	this.codeVariableCombo		= this.$form.find( '[name=codeVariable]' ).combobox();
	
	// sections to show / hide based on the type selection
	this.rScriptForm			= this.container.find( ".r-script-form" );
	this.rScriptForm.hide();
	this.equationForm			= this.container.find( ".equation-form" );
	this.equationForm.hide();
	
	// calculation step type buttons
	var typeRScriptButton = this.$form.find( 'button[name="type-r-script"]' );
	this.rScriptButton = new OptionButton( typeRScriptButton );
	
	var typeEquationButton = this.$form.find( 'button[name="type-equation"]' );
	this.equationButton = new OptionButton( typeEquationButton );
	
	//initialized in the init method
	//R script component manager
	var rScriptField = this.$form.find("[name=script]");
	this.$RScript = new RScript(rScriptField);
	
	this.$addVariableButton = this.$form.find( "button[name='add-variable']" );	
	this.addVariableModal = new AddVariableModal( this.$addVariableButton , this );
	
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
		
		// load step if necessary
		var url = window.sectionUrl;
		var stepId = $.url(url).param("id");
		if ( stepId ) {
			UI.lock();
			CalculationStepManager.getInstance().load (stepId, function(response) {
				$this.currentCalculationStep = response;
				$this.updateForm();
			});
		} else {
			// default settings
			$this.rScriptButton.select();
		}
		
		// disable / enable type buttons
		if( $this.workspace.equationLists.length == 0 ) {
			// disable type equation
			UI.disable( $this.equationButton.button );
		} else {
			// enable type equations
			$this.equationListCombo.data( $this.workspace.equationLists , "id" , "name" );
		}
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
		
		$this.$form.find( '[name=type]' ).val( "SCRIPT" );
		$this.rScriptForm.fadeIn( 300 );
	});
	this.rScriptButton.deselect( function() {
		UI.enable( this.button );
		
		$this.$form.find( '[name=type]' ).val( "" );
		$this.rScriptForm.hide();
	});
	
	this.equationButton.select( function() {
		UI.disable( this.button );
		$this.rScriptButton.deselect();
		
		$this.$form.find( '[name=type]' ).val( "EQUATION" );
		$this.equationForm.fadeIn( 300 );
	});
	this.equationButton.deselect( function() {
		UI.enable( this.button );
		
		$this.$form.find( '[name=type]' ).val( "" );
		$this.equationForm.hide();
	});
	
	
	this.equationListCombo.change( $.proxy( this.equationListChange , $this ) ) ;
	
};

/**
 * Create or update the calculation step according to the field values in the form 
 */
CalculationStepEditManager.prototype.save = function(successCallback, errorCallback) {
	UI.lock();
	var $this = this;
	var $step = $this.$form.serialize();
	CalculationStepManager.getInstance().save($step,
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
		    	
	    		Calc.homeCalculationManager.updateStep($this.currentCalculationStep);
		    	
	    		if(successCallback) {
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
	
	switch ( this.currentCalculationStep.type ) {
		case "SCRIPT":
			this.rScriptButton.select();
			break;
		case "EQUATION":
			this.equationButton.select();
			// populate equation form
			var params = this.currentCalculationStep.parameters;
			
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
			
			break;
	}
	
	UI.Form.setFieldValues( this.$form, this.currentCalculationStep );
	UI.unlock();
	
	//reset changed state 
	this.$form.data('changed', false);
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
	
	if ( entity ) {
		// populate fields that need the entity
		// r script
		this.$RScript.entity = entity;
		
		// variable select
		this.$variableCombo.enable();
		this.updateVariableSelect();
		
		UI.enable( this.$addVariableButton );
		
		this.equationListCombo.enable();
		
	} else {
		// Entity not selected reset fields that need the entity
		this.$RScript.entity = null;
		UI.disable( this.$addVariableButton );
		
		this.$variableCombo.reset();
		this.$variableCombo.disable();
	
		// not necessary. it shoud be easier to deselect the combobox...
		this.equationListCombo.reset();
		this.equationListCombo.data( this.workspace.equationLists , "id" , "name" );
		
		this.equationListCombo.disable();
		
		this.codeVariableCombo.reset();
		this.codeVariableCombo.disable();
	}
	
};

/**
 * Populate the "output variable" select according to the selected parent entityId.
 */
CalculationStepEditManager.prototype.updateVariableSelect = function() {
	var entity = this.getSelectedEntity();
	if( entity ) {
		var variables = entity.outputVariables();
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
		this.equationListVariableCombos = {};
		for( var i in vars ){
			var variable = vars[i];
			
			var div = $( '<div class="form-group eq-variable">' );
			var label = $( '<label class="col-md-2 control-label"></label>' );
			label.html( "Variable '" +variable+ "'");
			div.append( label );

			var divSelect = $( '<div class="col-md-10">' );
			var select = $( '<select class="form-control"></select>' );
			select.attr( "name" , variable );
			divSelect.append( select );	
			var combo = select.combobox();
			combo.data( this.getSelectedEntity().getAncestorsVariables() , "id" , "name" );
			this.equationListVariableCombos[ variable ] = combo;
			div.append( divSelect );
			
			div.hide();
			this.equationForm.find(".form-container").append( div );
			div.fadeIn();
		}
	} else {
		this.codeVariableCombo.disable();
		this.codeVariableCombo.reset();
	}
};

AddVariableModal = function( triggerButton , editManager ) {
	// calculation step edit manager instance
	this.calcStepEditManager = editManager;
	// button that triggers the opening of the add variable modal form
	this.triggerButton = triggerButton;
	// modal container
	this.container = $( '#add-variable-modal' );
	// form to submit
	this.form = this.container.find('form');
	// save button form
	this.saveButton = this.container.find('.save');

	this.init();
};

AddVariableModal.prototype.init = function() {
	var $this = this;
	
	//submits the add variable form
	this.form.submit( function(event) {
		event.preventDefault();
		UI.lock();
		var variable = UI.Form.toJSON( $this.form );
		
		var successCallback = function(response) {
			UI.Form.updateErrors( $this.form, response.errors );
			
	    	if(response.status == "ERROR" ) {
	    		var errors = response.errors;
	    		var errorMessage = UI.Form.getFormErrorMessage( $this.form, errors );
	    		UI.showError( errorMessage, true );
	    	} else {
	    		var variable = response.fields.variable;
	    		
	    		$this.calcStepEditManager.updateVariableSelect();
	    		$this.calcStepEditManager.$variableCombo.val( variable.id );
	    		
	    		$this.container.modal('hide');
	    		$this.container.modal('removeBackdrop');
	    	}
		};
		var errorCallback = function (e) {
	    	UI.showError("An error occured. Please check the log file.", false);
		};
		var completeCallback = function() {
			UI.unlock();
		};
		
		WorkspaceManager
			.getInstance()
			.activeWorkspaceAddQuantitativeVariable( variable, successCallback, errorCallback, completeCallback );
	});
	
	//add variable button click
	$this.triggerButton.click(function(event){
		event.preventDefault();
		
		UI.Form.reset( $this.form );
		
		//set entityId hidden field value
		var selectedEntityId = $this.calcStepEditManager.getSelectedEntityId();
		$this.form.find( "[name=entityId]" ).val( selectedEntityId );
		// open the modal 
		$this.container.modal( {keyboard: true, backdrop: "static"} );
		
		//set focus on first field in form
		setTimeout(function() {
			UI.Form.setFocus( $this.form );	
		}, 500);
	});
	
	// add variable form: on save button click, it submits the form
	$this.saveButton.click(function(event) {
		event.preventDefault();
		$this.form.submit();
	});

};