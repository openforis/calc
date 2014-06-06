/**
 * data visualisation options
 * it contains the entity and variable selectors
 * 
 * @author Mino Togna
 */
DataVisualisationOptions = function ( container ) {
	
	this.container 		= container;
	
	this.dataProvider 	= null;
	
	this.entityCombo 	= this.container.find( ".entity-select select" ).combobox();
	this.quantities		= this.container.find( ".quantities" );
	this.categories 	= this.container.find( ".categories" );
	this.qBtn 			= this.container.find( "[name=q-btn]" ); 
	this.cBtn 			= this.container.find( "[name=c-btn]" ); 
	this.viewResultsBtn	= this.container.find( ".view-results-btn button" ); 

	// function to call after view results btn is clicked
	// to set externally
	this.viewResults = function(){};
	// max number of variables user can select (e.g. 2 for scatter plot)
	this.maxVariables = null;
	// disable category  (e.g. scatter plot only qty vars can be selected)
	this.disableCategories = false;
	
	this.optionButtonsHeight = "90%";
	
	this.init();
};

DataVisualisationOptions.prototype.init = function() {
	var $this = this;
	
	this.categories.hide();
	this.container.css( {"height" : this.optionButtonsHeight } );
	
	this.container.on( "show.bs.collapse" , function(e) {
		e.stopPropagation();
		$this.container.hide();
	});
	this.container.on( "shown.bs.collapse" , function(e) {
		e.stopPropagation();

		$this.container.css( {"height": $this.optionButtonsHeight } );
		setTimeout( function(){
			$this.container.fadeIn( 200 );
		} , 50 );
	});
		
	
	// event handlers
	this.entityCombo.change( function(e){
		var entityId = $this.entityCombo.val();
		// empty sections
		$this.quantities.empty();
		$this.categories.empty();
		if ( entityId ) {
			// create data provider
			$this.dataProvider = new DataViewProvider( entityId, null, true );
			
			// update variable sections
			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				var entity = ws.getEntityById( entityId );
				$this.addVariableOptions( entity );
			});
			
			if( $this.disableCategories == true ) {
				var btns = $this.categories.find( "button:not(.filter-btn)" );
				UI.disable( btns );
			}
		}
	});
	
	this.qBtn.click( function(e) {
		$this.quantities.show();
		$this.categories.hide();
	});
	this.cBtn.click( function(e) {
		$this.categories.show();
		$this.quantities.hide();
	});
	
	this.viewResultsBtn.click( function(e){
		e.preventDefault();
		
		if( $this.viewResults ) {
			$this.viewResults.apply(this);
		}
	});
	
};

DataVisualisationOptions.prototype.addVariableOptions = function( entity ) {
	while( entity ) {
		var header = $( '<div class="width100 float-left entity-header"></div>' );
		header.html( entity.name );

		this.quantities.append( header );
		this.categories.append( header.clone() );
		
		this.addVariableOptionButtons( entity.quantitativeVariables() , this.quantities );
		this.addVariableOptionButtons( entity.categoricalVariables() , this.categories );
		
		entity = entity.parent();
	}
};

DataVisualisationOptions.prototype.addVariableOptionButtons = function( variables , uiContainer ) {
//    var filters  = ;
	var $this = this;
    for( var i in variables ) {
		var v = variables[i];
		
		var div = $( '<div class="width100 clearfix"></div>' );
		var divVarBtn = $( '<div class="width90 float-left"></div>' );
		var divFilterBtn = $( '<div class="width10 float-left filter-div"></div>' );
		div.append( divVarBtn );
		div.append( divFilterBtn );
		uiContainer.append( div );
		
		var btn = $( '<button class="btn option-btn"></button>' );
		btn.html( v.name );
		divVarBtn.append( btn );
		
		var optBtn = new OptionButton( btn );
		var selectFunction =  function( v , optBtn ) {
			var valid = true;
			if( $this.maxVariables &&  ( $this.dataProvider.variables.length == $this.maxVariables ) ) {
				valid = false;
				UI.showError( "Max "+ $this.maxVariables +" variables are allowed", true );
				optBtn.deselect();
			}
			if( valid ){
				$this.dataProvider.addVariable( v.name );
			}
		};
		var deselectFunction = function( v ) {
			$this.dataProvider.deleteVariable( v.name );
		};
		optBtn.select( selectFunction , v  , optBtn );
		optBtn.deselect( deselectFunction , v );
		
		var addFilterBtn = function() {
		    var filterBtn = $( '<button class="btn no-background filter-btn"><i class="fa fa-filter"></i></button>' );  
		    filterBtn.data( "variable" , v );
		    
		    filterBtn.click( function(e) {
		    	e.stopPropagation();
		    	filterBtn.data( "container" , uiContainer );
		    	$this.dataProvider.filters.show( filterBtn );
		    });
			    
		    divFilterBtn.append( filterBtn );
		};
		addFilterBtn();
    }
};

/**
 * Refresh ui states
 * it should be called when workspace changes
 */
DataVisualisationOptions.prototype.refresh = function() {
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws ) {
			$this.categories.empty();
			$this.quantities.empty();
			
			$this.entityCombo.data( ws.entities, 'id' , 'name' );
		}
	});
};

DataVisualisationOptions.prototype.show = function() {
	this.container.collapse("show");
};


DataVisualisationOptions.prototype.hide = function() {
	this.container.collapse("hide");
};
