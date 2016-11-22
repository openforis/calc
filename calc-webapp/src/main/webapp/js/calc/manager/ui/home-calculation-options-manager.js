/**
 * Home Page Calculation options section manager
 * 
 * @author M. Togna
 */
HomeCalculationOptionsManager = function( homeCalculationManager, container , triggerButton ){
	
	this.homeCalculationManager = homeCalculationManager;
	this.container 				= $( container );
	this.triggerBtn 			= $( triggerButton );
	
	// view step option buttons
	var viewStepList 		= this.container.find( 'button.view-steps-list' );
	this.viewStepListBtn 	= new OptionButton( viewStepList );
	//this.viewStepListBtn.disableUnselect = true;
	
	var viewStepFolder		= this.container.find( 'button.view-steps-folder' );
	this.viewStepFolderBtn 	= new OptionButton( viewStepFolder );
	//this.viewStepFolderBtn.disableUnselect = true;
	
	this.workspaceManager 	= WorkspaceManager.getInstance();
	
	this.init();
};

HomeCalculationOptionsManager.viewStepsAsList = "AS_LIST";
HomeCalculationOptionsManager.viewStepsByEntity = "BY_ENTITY";

HomeCalculationOptionsManager.prototype.init	= function(){
	var $this = this;
	// hide options section
	this.container.css( {'width':'0%'} );
	this.container.hide();
	
	// toggle options section
	this.triggerBtn.click( function(e){
		if( $this.container.is(":visible") ){
			setTimeout( function(){
				$this.container.stop().hide( 0 );
			}, 350);
			$this.container.animate( {width :'0px'} , {'duration': 500} );
		} else {
			$this.container.show( 0 );
			$this.container.animate( {width :'320px'} , {'duration': 500} ); 
		}
	});
	
	// view steps as list buttons click
	this.viewStepListBtn.select( function(){
		$this.viewStepFolderBtn.deselect();
		$this.setViewSteps( HomeCalculationOptionsManager.viewStepsAsList );
	});
	this.viewStepListBtn.deselect( function(){ });

	// view steps by entity buttons click
	this.viewStepFolderBtn.select( function(){
		$this.viewStepListBtn.deselect();
		$this.setViewSteps( HomeCalculationOptionsManager.viewStepsByEntity );
	});
	this.viewStepFolderBtn.deselect( function(){ });
};

/**
 * Set view steps option for current workspace
 * @param viewStepsOption
 */
HomeCalculationOptionsManager.prototype.setViewSteps = function( viewStepsOption ){
	var $this = this;
	this.workspaceManager.setViewSteps( viewStepsOption , function(ws){
		$this.updateUI();
		$this.triggerBtn.click();
	});
};

/**
 * Show calculation steps based on the view steps option defined 
 */
HomeCalculationOptionsManager.prototype.updateUI = function(){
	var $this = this;
	this.workspaceManager.activeWorkspace( function(ws){
		var steps = $this.homeCalculationManager.stepsContainer.find( '.calculation-step' );
		
		if( ws.settings.viewSteps == HomeCalculationOptionsManager.viewStepsAsList ){
			$this.viewStepListBtn.displayAsSelected();
			$this.viewStepFolderBtn.displayAsUnelected();
			
			$this.homeCalculationManager.stepsEntityContainer.hide();
			
			$.each( steps , function(i,step){
				setTimeout( function(){
					$(step).fadeIn( 50 );	
				}, 15*i );
				
			});
		} else if( ws.settings.viewSteps == HomeCalculationOptionsManager.viewStepsByEntity ){
			steps.hide();
			
			$this.viewStepListBtn.displayAsUnelected();
			$this.viewStepFolderBtn.displayAsSelected();
			
			$this.homeCalculationManager.stepsEntityContainer.fadeIn( 500 );
			
			for( var i in $this.homeCalculationManager.stepsEntityMap ){
				var stepBtn = $this.homeCalculationManager.stepsEntityMap[ i ];
				stepBtn.displayAsUnelected();
			}
		}
	});
};
