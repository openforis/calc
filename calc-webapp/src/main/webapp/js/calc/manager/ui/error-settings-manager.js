/**
 * Manger for error settings section
 * 
 *  @author Mino Togna
 */

ErrorSettingsManager = function( container ) {
	this.container = $( container );
	// placeholder variable for the workspace
	this.workspace = null;
	
	this.formContainer				= this.container.find( ".form" );
	
	// variable selection container
	this.quantity					= this.container.find( ".quantity" );
	// variable error settings container 
	this.errorSettingsContainer 	= this.container.find( ".error-settings" );
	this.errorSettingsContainer.hide();
	
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
	});
}; 

ErrorSettingsManager.prototype.initEventHandlers = function() {
	var $this = this;
	
	this.saveBtn.click( function(e){
		e.preventDefault();
		
		var data = JSON.stringify( $this.errorSettings );
		WorkspaceManager.getInstance().setErrorSettings( data, function(ws){
			$this.workspace = ws;
			console.log( $this.workspace );
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
						
						var btn = $( '<button class="btn blue-btn col-md-8"></button>' );
						btn.html( variable.name );
						btn.click( function(e){
							$this.showVariableSettings( variable.id );
						});
						div.append( btn );
						container.append( div );
						
						
						var errorBtn = $( '<button class="btn no-background col-md-4" style="margin-top:3px;"><img alt="" src="img/cv.png" width="20em" height="20em"></button>' );
						div.append( errorBtn );
						
						var optBtn = new OptionButton( errorBtn );
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
	if( variableSettingsContainer.length > 0 ){
//		console.log("found");
	} else {
		// init variable container
		variableSettingsContainer = this.errorSettingsContainer.clone();
		variableSettingsContainer.addClass( "settings"+variableId ).addClass( "variable-error-settings" );
		
		// add aois
		var aoisContainer 	= variableSettingsContainer.find( ".aoi" );
		var addAoi 			= function( aoi , depth ){
			var btn = $( '<button class="btn option-btn width100"></button>' );
			btn.html( aoi.caption );
			aoisContainer.append( btn );
			
			// option button
			var optionBtn = new OptionButton( btn );
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
		
		var categoryContainer 	= variableSettingsContainer.find( ".category" );
		var addCategory = function( category ){
			var btn = $( '<button class="btn option-btn width100"></button>' );
			btn.html( category.name );
			categoryContainer.append( btn );
			
			// option button
			var optionBtn = new OptionButton( btn );
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
		
	}
	// show the ui settings
	variableSettingsContainer.fadeIn();
};

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
	ArrayUtils.removeItem( this.errorSettings[ variableId ].aois , aoi );
};

ErrorSettingsManager.prototype.selectCategory = function( variableId, categoryId ){
	var settings = this.getVariableErrorSettings( variableId );
	
	var categorySettings = settings.categories;
	if( categorySettings ){
	} else {
		categorySettings = [];
		settings.categories = categorySettings;
	}
	
	categorySettings.push( categoryId );
	
	console.log( this.errorSettings );
};
ErrorSettingsManager.prototype.deselectCategory = function( variableId, categoryId ){
//	console.log( " deselect category " + categoryId + " for variable: " + variableId );
	ArrayUtils.removeItem( this.errorSettings[ variableId ].categories , categoryId );
};

ErrorSettingsManager.prototype.getVariableErrorSettings = function( variableId ){
	var settings = this.errorSettings[ variableId ];
	if( settings ){
//		console.log( "found");
	} else {
		settings = {};
		this.errorSettings[ variableId ] = settings;
	}
	
	return settings;
};
