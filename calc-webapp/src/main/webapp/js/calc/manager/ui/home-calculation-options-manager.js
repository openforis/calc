/**
 * Home Page Calculation options section manager
 * 
 * @author M. Togna
 */
HomeCalculationOptionsManager = function( container , triggerButton ){
	this.container 	= $( container );
	this.triggerBtn = $( triggerButton );
	
	// view step option buttons
	var viewStepList 		= this.container.find( 'button.view-steps-list' );
	this.viewStepListBtn 	= new OptionButton( viewStepList );
	var viewStepFolder		= this.container.find( 'button.view-steps-folder' );
	this.viewStepFolderBtn 	= new OptionButton( viewStepFolder );
	
	this.init();
};

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
	
	this.viewStepListBtn.select( function(){
		$this.viewStepFolderBtn.deselect();
	});
	this.viewStepListBtn.deselect( function(){ });
	
	this.viewStepFolderBtn.select( function(){
		$this.viewStepListBtn.deselect();
	});
	this.viewStepFolderBtn.deselect( function(){ });
};

