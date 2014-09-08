/**
 * Manger for error settings section
 * 
 *  @author Mino Togna
 */

ErrorSettingsManager = function( container ) {
	this.container = $( container );
	// placeholder variable for the workspace
	this.workspace = null;
	
	// r editor
	this.rEditor					= new REditor( "error-r-script" );
	
	this.formContainer				= this.container.find( ".form" );
	
	// variable selection container
	this.quantity					= this.container.find( ".quantity" );
	// variable error settings container 
	this.errorSettingsContainer 	= this.container.find( ".error-settings" );
	this.errorSettingsContainer.hide();
	
	// list of option buttons
	this.aoiButtons = [];
	this.categoryButtons = [];
	// error settings model object
	this.errorSettings 	= {};
	
	this.saveBtn 		= this.container.find( ".save-btn button" );
	
	this.init();
};

ErrorSettingsManager.prototype.init = function() {
	this.initEventHandlers();
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.workspace = ws;
		
		$this.showQuantity();
		
		var errSettings = $this.workspace.errorSettings.parameters;
		if( errSettings ){
			for( var i in errSettings ){
				var variableId = parseInt( i );
				$this.addVariableSettings( variableId );
				
				var varErrorSettings = errSettings[ i ];
				if( varErrorSettings.aois ){
					for( var j in varErrorSettings.aois ){
						var aoiId 		= varErrorSettings.aois[ j ];
						var optionBtn	= $this.aoiButtons[ variableId ][ aoiId ];
						optionBtn.select();
					}
				}
				if( varErrorSettings.categories ){
					for( var j in varErrorSettings.categories ){
						var categoryId 	= varErrorSettings.categories[ j ];
						var optionBtn 	= $this.categoryButtons[ variableId ][ categoryId ];
						optionBtn.select();
					}
				}

			}
		}
	} , true);
}; 

ErrorSettingsManager.prototype.initEventHandlers = function() {
	var $this = this;
	
	this.saveBtn.click( function(e){
		e.preventDefault();
		UI.lock();
		var data = JSON.stringify( $this.errorSettings );
		WorkspaceManager.getInstance().setErrorSettings( data, function(ws){
			$this.workspace = ws;
			UI.unlock();
		});
		
	});
};

ErrorSettingsManager.prototype.showQuantity = function() {
	var $this = this;
	var container = $( '<div class="height95 option col-md-12" style="overflow: auto;"></div>' );
	$this.quantity.append( container );
	UI.lock();
	
	var vars = [];
	CalculationStepManager.getInstance().loadAll( function(steps) {
			for( var i in steps ) {
				var step = steps[i];
				var variableId = step.outputVariableId;
				var variable = $this.workspace.getVariableById( variableId );
				if( !ArrayUtils.contains(vars,variableId) && variable.type == "QUANTITATIVE" ) {
					
					var initVariableButton = function(){
						var variable = $this.workspace.getVariableById( variableId ); 
						
						var div = $( '<div class="option row no-margin no-padding" style="padding-bottom: 2px"></div>' );
						
						var btn = $( '<button class="btn blue-btn col-md-12"></button>' );
						btn.html( variable.name );
						btn.click( function(e){
							$this.showVariableSettings( variable.id );
							
							container.find( ".blue-btn-selected" ).removeClass( "blue-btn-selected" ).addClass( "blue-btn" );
							btn.removeClass( "blue-btn" ).addClass( "blue-btn-selected" );
						});
						div.append( btn );
						container.append( div );
						
						
//						var errorBtn = $( '<button class="btn no-background col-md-4" style="margin-top:3px;"><img alt="" src="img/cv.png" width="20em" height="20em"></button>' );
//						div.append( errorBtn );
//						var optBtn = new OptionButton( errorBtn );
						
						vars.push( variableId );
					};
					initVariableButton();
					
				}
			}
			UI.unlock();
			$this.quantity.fadeIn();
	});
}; 

ErrorSettingsManager.prototype.showVariableSettings = function( variableId ){
	var $this = this;
	
	this.formContainer.find( ".variable-error-settings" ).hide();
	var variableSettingsContainer = this.formContainer.find( ".settings" + variableId );
	if( variableSettingsContainer.length == 0 ) {
		variableSettingsContainer = this.addVariableSettings( variableId );
	}
	// show the ui settings
	variableSettingsContainer.fadeIn();
};
ErrorSettingsManager.prototype.addVariableSettings = function( variableId ){
	var $this 							= this;
	var aoiButtons						= [];
	this.aoiButtons[ variableId ] 		= aoiButtons;
	var categoryButtons 				= [];
	this.categoryButtons[ variableId ] 	= categoryButtons;
	
	// init variable container
	var variableSettingsContainer = this.errorSettingsContainer.clone();
	variableSettingsContainer.addClass( "settings"+variableId ).addClass( "variable-error-settings" );
	
	// add aois
	var aoisContainer 	= variableSettingsContainer.find( ".aoi .button-container" );
	var addAoi 			= function( aoi , depth ){
		var btn = $( '<button class="btn option-btn width100"></button>' );
		btn.html( aoi.caption );
		aoisContainer.append( btn );
		
		// option button
		var optionBtn = new OptionButton( btn );
		$this.aoiButtons[ variableId ][aoi.id] = optionBtn;
		optionBtn.select( function( vId , aId ){
			$this.selectAoi( vId , aId );
		} , variableId , aoi.id );
		optionBtn.deselect( function( vId , aId ){
			$this.deselectAoi( vId , aId );
		} , variableId , aoi.id );
		
		for( var i in aoi.children ){
			var child = aoi.children[ i ];
			addAoi( child , depth + 1);
		}
	};
	
	var rootAoi 		= this.workspace.getRootAoi();
	addAoi( rootAoi , 0 );
	
	// add categories
	var variable 	= this.workspace.getVariableById( variableId );
	var entity 		= this.workspace.getEntityById( variable.entityId );
	var categories	= entity.samplingUnitHierarchyCategoricalVariables();
	
	var categoryContainer 	= variableSettingsContainer.find( ".category .button-container" );
	var addCategory = function( category ){
		var btn = $( '<button class="btn option-btn width100"></button>' );
		btn.html( category.name );
		categoryContainer.append( btn );
		
		// option button
		var optionBtn = new OptionButton( btn );
		$this.categoryButtons[ variableId ][category.id] = optionBtn;
		optionBtn.select( function( vId , cId ){
			$this.selectCategory( vId , cId );
		} , variableId , category.id );
		optionBtn.deselect( function( vId , cId ){
			$this.deselectCategory( vId , cId );
		} , variableId , category.id );
	};
	
	for( var i in categories ){
		var category = categories[ i ];
		addCategory( category );
	};
	
	
	this.formContainer.append( variableSettingsContainer );
	return variableSettingsContainer;
}
/**
 * Select / deselect Aoi
 */
ErrorSettingsManager.prototype.selectAoi = function( variableId, aoiId ){
	var settings = this.getVariableErrorSettings( variableId );
	
	var aoiSettings = settings.aois;
	if( aoiSettings ){
	} else {
		aoiSettings = [];
		settings.aois = aoiSettings;
	}
	
	aoiSettings.push( aoiId );
};
ErrorSettingsManager.prototype.deselectAoi = function( variableId, aoiId ){
	ArrayUtils.removeItem( this.errorSettings[ variableId ].aois , aoiId );
};

/**
 * Select / deselect categorical variable
 */
ErrorSettingsManager.prototype.selectCategory = function( variableId, categoryId ){
	var settings = this.getVariableErrorSettings( variableId );
	
	var categorySettings = settings.categories;
	if( categorySettings ){
	} else {
		categorySettings = [];
		settings.categories = categorySettings;
	}
	
	categorySettings.push( categoryId );
};
ErrorSettingsManager.prototype.deselectCategory = function( variableId, categoryId ){
	ArrayUtils.removeItem( this.errorSettings[ variableId ].categories , categoryId );
};

ErrorSettingsManager.prototype.getVariableErrorSettings = function( variableId ){
	var settings = this.errorSettings[ variableId ];
	if( settings ){
	} else {
		settings = {};
		this.errorSettings[ variableId ] = settings;
	}
	
	return settings;
};
