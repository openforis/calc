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
//	console.log( " select aoi " + aoiId + " for variable: " + variableId );
	var settings = this.getVariableErrorSettings( variableId );
	
	var aoiSettings = settings.aois;
	if( aoiSettings ){
	} else {
		aoiSettings = [];
		settings.aois = aoiSettings;
	}
	
	aoiSettings.push( aoiId );
	
	console.log( this.errorSettings );
};
ErrorSettingsManager.prototype.deselectAoi = function( variableId, aoiId ){
//	console.log( " deselect aoi " + aoiId + " for variable: " + variableId );
	ArrayUtils.removeItem( this.errorSettings[ variableId ].aois , aoi );
};

ErrorSettingsManager.prototype.selectCategory = function( variableId, categoryId ){
	console.log( " select category " + categoryId + " for variable: " + variableId );
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
		console.log( "not found");
		settings = {};
		this.errorSettings[ variableId ] = settings;
	}
	
	return settings;
};












































ErrorSettingsManager.prototype.showCategory = function( vId ) {
	var $this = this;
	$this.category.find("div.option").remove();
	var container = $( '<div class="height95 option" style="overflow: auto;"></div>' );
	$this.category.append( container );
	
	WorkspaceManager.getInstance().activeWorkspace( function(ws) {
		var v = ws.getVariableById( vId );
		var entity 	= ws.getEntityById( v.entityId );
		
		var divs = [];
		var btns = [];
		while( entity ) {
			
			var vars = entity.categoricalVariables();
			for( i in vars ) {
				var variable = vars[i];
				var variableId = variable.id;
				
				var initBtn = function(){
					
					var div = $( '<div class="option width100" style="padding-bottom: 2px"></div>' );
					div.data( "variable" , variable );
					var btn = $( '<button class="btn option-btn width100"></button>' );
					btn.html( variable.name );
					div.append( btn );
					divs[variableId] = div;
					container.append( div );
					
					var optBtn = new OptionButton( btn );
					btns[variableId] = optBtn;
					var selectFunction = function( vId ) {
						// first deselect others
						for( var j in btns ){
							if( parseInt(j) != vId ) {
								var b = btns[j];
								b.deselect();
							}
						}
						
						$this.params.category 	= vId;
						$this.params.classes 	= null;
						
						$this.showClasses( vId );
					};
					
					var deselectFunction = function( vId ) {
						$this.params.category 	= null;
						$this.params.classes 	= null;
						
						$this.classes.hide();
						$this.viewBtn.hide();
						
						$this.result.hide();
					};
					
					optBtn.select( selectFunction , variableId );
					optBtn.deselect( deselectFunction , variableId );
				};
				initBtn();
			}
			
			
			
			entity = entity.parent();
		}
		$this.category.fadeIn();
	});
	
};

ErrorSettingsManager.prototype.showClasses = function( vId ) {
	UI.lock();
	var $this = this;
	this.params.classes 	= [];
	$this.classes.find("div.option").remove();
	var container = $( '<div class="height95 option" style="overflow: auto;"></div>' );
	$this.classes.append( container );
	
	VariableManager.getInstance().getCategories( vId , function(categories) {
		
		if( categories ) {
			
			for( var i in categories ){
				var category	= categories[ i ];
				
				var addButton = function() {
					var code 		= category.code;
					var caption 	= category.caption;
					
					var div = $( '<div class="float-left width25 option" style="padding: 0.1em 0.1em;"></div>' );
					container.append( div );
					var btn = $( '<button class="btn option-btn width100">' );
					btn.html( code );
					div.append( btn );
					
					// enable caption tooltip
					btn.tooltip({ title: caption, delay: { show: 200, hide: 100 }});
					
					var optionBtn = new OptionButton( btn );
					
					optionBtn.select( function(c){
//						$this.values.push( code );
						$this.params.classes.push( c );
						if( $this.params.classes.length > 0 ){
							$this.viewBtn.fadeIn();
						} else {
							$this.viewBtn.fadeOut();
							$this.result.hide();
						}
					} , code );
					
					optionBtn.deselect( function(c) {
						var index = $this.params.classes.indexOf( c );

						if (index > -1) {
							$this.params.classes.splice(index, 1);
						}
						if( $this.params.classes.length > 0 ){
							$this.viewBtn.fadeIn();
						} else {
							$this.viewBtn.fadeOut();
							$this.result.hide();
						}
					} , code  );
					
				};
				
				addButton();
			}
			
			
			$this.classes.fadeIn();
		} else {
			UI.showError( "Too many classes to show for this category. Not yet implemented for such categories." , true );
		}
		
		UI.unlock();	
	});
	
};


ErrorSettingsManager.prototype.showResults = function( results ) {
	var thead = this.resultTable.find( "thead" ); 
	var tbody = this.resultTable.find( "tbody" );
	var formatNumber = d3.format(".4n");
	
	thead.empty();
	tbody.empty();
	
	var fieldNames = results[0].fields;
	
	var tr = $( "<tr></tr>" );
	thead.append(tr);
	for( var n in fieldNames ){
		var th = $( "<th></th>" );
		th.html( n );
		tr.append( th );
	}
//	$.each( fieldNames , function(i,field) {
//		var th = $( "<th></th>" );
//		th.html( field );
//		tr.append( th );
//	});
	
	
	$.each( results, function( i , record ) {
		var tr = $( "<tr></tr>" );		
		tbody.append( tr );
		
		$.each( fieldNames, function( j , f ) {
			var value = record.fields[ j ];
			// format only numbers with decimal points
			var field = ( typeof value === "number" && value % 1 !== 0 ) ? formatNumber( value ) : value;
			var td = $( "<td></td>" );
			td.html( field );
			tr.append( td );
		});

		
	});
	
	this.result.fadeIn();
	
};