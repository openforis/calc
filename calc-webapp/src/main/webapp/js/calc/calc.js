/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */

$dataVisualization 	= $( "#data-visualization" );

/**
 * main html container
 */
$container 			= $("#container");

$(document).ready(function() {
	
	/**
	 * Main calc instance
	 */
	Calc = {};
	
	/**
	 * current working section
	 */
	Calc.section = null;
	
	/**
	 * Ui managers
	 */
	Calc.homeCalculationManager 	= null;
	Calc.homeDataManager 			= null;
	Calc.workspaceSettingsManager 	= null;
	
	/**
	 * Ui elements
	 */
	Calc.footer 			= $( "#footer" );
	Calc.footerHomeLinks 	= Calc.footer.find( ".links" );
	Calc.backHomeBtn 		= Calc.footer.find( "button.back" );
	
	/**
	 * Function to be called when active workspace change
	 */
	Calc.workspaceChange = function( success ) {
		
		WorkspaceManager.getInstance().refreshActiveWorkspace( function(ws){
			// load calc steps to home page
			Calc.homeCalculationManager.updateSteps();
			// load entities and variables for home data query section
			Calc.homeDataManager.refresh();
			// update active workspace home settings ui
			Calc.workspaceSettingsManager.updateActive( ws );
			
			// update ui buttons
			Calc.updateButtonStatus();
			
			if ( success ) {
				success(ws);
			}
		});
			
	};
	
	/**
	 * Enable / disable buttons based on workspace status
	 */
	Calc.updateButtonStatus = function(){
		
		var wsButtons 		= $( document ).find( ".workspace-required" );
		var aoiButtons 		= $( document ).find( ".aoi-required" );
		var sdButtons 		= $( document ).find( ".sampling-design-required" );
		var chainButtons 	= $( document ).find( ".default-chain-completed-required" );
		var stepsButtons 	= $( document ).find( ".calculation-steps-required" );
		
		UI.disable( wsButtons );
		UI.disable( aoiButtons );
		UI.disable( sdButtons );
		UI.disable( chainButtons );
		UI.disable( stepsButtons );
		
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if ( ws ) {
				UI.enable( wsButtons );
				if( ws.aoiHierarchies.length > 0 ) {
					UI.enable( aoiButtons );
					
					if( ws.samplingDesign ){
						UI.enable( sdButtons );
						var chain = ws.getDefaultProcessingChain();
						if( chain.status === "COMPLETED" ){
							UI.enable( chainButtons );
						}
					}
				}
				var chain = ws.getDefaultProcessingChain();
				if( chain && chain.calculationSteps.length > 0 ){
					UI.enable( stepsButtons );			
				}
			}
			
		});
	};
	
	/**
	 * Functions used to show rest call errors
	 */
	Calc.error = function( jqXHR , textStatus , errorThrown ) {
		var message = $( "<div class='width100'></div>" );
		
		if( jqXHR.status ) {
			textStatus += " " + jqXHR.status; 
		}
		message.append( "<div class='width100'>" + textStatus + "</div>" );
		
		if( errorThrown ) {
			message.append( "<div class='width100'>" + errorThrown + "</div>" );
		}
		
		if( jqXHR.responseText ) {
			var heightPercent = 80 / 100;
			//div height will be 80% of available space
			var height = heightPercent * ($(window).height() - Calc.footer.height()) + "px";
			message.append( "<div class='width100' style='overflow-y: auto; height: " + height + "'>" + jqXHR.responseText + "</div>" );
		}
		
		UI.showError( message, false );
	};
	
	
	var positionFooter = function() {
		$(".footer-placeholder").css("height", Calc.footer.height());
		Calc.footer.animate({top:$(window).height()- Calc.footer.height() }, 400);
	};
	
	var resizeContainer = function() {
		var containerHeight = $(window).height() - Calc.footer.height();
		$container.css({"height":containerHeight+"px"});
	};
	
	var scrollToSection = function (animate) {
		//calculate the scroll offset
		var scrollTop = Calc.section[0].offsetTop;
		
		if ( animate ) {
			//show all siblings temporarily during scrolling
			Calc.section.siblings().andSelf().visible();
			
			//enable container scrolling during animation
			$container.css('overflow','auto');
			
			var onAnimationComplete = function(){
				//remove scrollbar when animation ends
				$container.css( 'overflow' , 'hidden' );
				//make siblings invisible (block focus of hidden sections)
				Calc.section.siblings().invisible();
			};
			//scroll to the current section offset
			$container.stop().animate( {scrollTop: scrollTop}, 700, "easeOutQuart", onAnimationComplete );
		} else {
			//scroll to the current section offset
			$container.css( 'overflow' , 'auto' );
			$container[0].scrollTop = scrollTop;
			$container.css( 'overflow' , 'hidden' );
		}
	};

	Calc.footer.find(".links button").click( function(event){
		event.preventDefault();

		var target = $( $(this).attr("href") );
		
		//set current home section
		if( Calc.section.attr("id") != target.attr("id") ){
		    Calc.section = target ;
		    scrollToSection(true);
		}
		
	});

	// event handler for home button click
	homeButtonClick = function(event){
		event.preventDefault();

		var $button = $(event.currentTarget);
		sectionUrl = $button.attr("href");
		
		//set the current working section (calculation,results,data or settings)
		Calc.section = $button.parents( ".section-home" );
		//home page section (contains the button links to the external pages)
		$homeSection = Calc.section.find( ".page-section" );
		if( !sectionUrl ) {
			var msg = " Calc error. Section url is undefinded";		
			throw msg;
		}
		
		Calc.navigateToSection( sectionUrl );
	};
	
	Calc.navigateToSection = function( sectionUrl ){
		// not accessing the page directly with section in the url
		if( typeof $homeSection !== 'undefined' ){
		
			Calc.pushToHistory( sectionUrl );
			
			$.ajax({
				url		: sectionUrl,
				dataType: "html",
				data	: { "t" : new Date().getTime() }
			}).done(function( response ) {
				var page = $( response );
				
				//hide loaded page
				page.hide();
				/**
				 * hide home and shows loaded page
				 */
				//fade out footer links
				Calc.footerHomeLinks.fadeOut(500);
				//move the home section buttons out of the screen towards left
				$homeSection.animate( {left:"-="+$(document).width()} , 1000 , 'easeInOutExpo' );
				setTimeout( function(){
					//hide the home section buttons
					$homeSection.hide();
					//append and show the loaded page to the current home section
					Calc.section.append( page );
					page.show();
					//show the back home button
					Calc.backHomeBtn.fadeIn(500);
				},500);
			}).error( function() {
				Calc.error.apply( this , arguments );
			});
		}
	};
	
	$(".section-home button.btn-home, .section-home button.btn-home-plus").click( homeButtonClick );
	
	Calc.backHomeBtn.click( function(event){
		event.preventDefault();
		history.go( -1 );
//		Calc.navigateToHome();
	});
	
	Calc.navigateToHome = function(){
		var $btnSection = Calc.section.find(".page-section:nth-child(1)");
		var $extSection = Calc.section.find(".page-section:nth-child(2)");
		
		//fade out loaded content and back button
		var duration = 500;
		$extSection.fadeOut(duration);
		Calc.backHomeBtn.fadeOut(duration);
		//remove loaded page from the document
		setTimeout(function(){
			$extSection.remove();
		}, duration);
		
		//show home section and footer buttons
		$btnSection.show();
		$btnSection.animate({left:"0px"}, 1000, 'easeOutExpo');
		Calc.footerHomeLinks.fadeIn(500);
		
		Calc.pushToHistory( "home" );
	};
	
	// history methods
	Calc.pushToHistory = function( section ) {
		window.history.replaceState( window.history.state , "load" , location.pathname + "#" + section );
		window.history.pushState( { section: section} );
	};
	
	//private static utility method to load Calc info model object
	Calc._loadInfo = function() {
		$.ajax({
			url: "rest/calc/info.json",
			dataType:"json"
		}).done(function(calc) {
			$.extend( Calc , calc );
			$("#footer .application-version").text( calc.version );
		}).error( function() {
			Calc.error.apply( this , arguments );
		});

	};
	
	// window events
	// on resize window
	$( window ).resize(function() {
		positionFooter();
		resizeContainer();
		scrollToSection(false);
	});
	
	// on browser back button click 
	window.onpopstate = function(event) {
		var section = ( event.state ) ? event.state.section : "home";
//		console.log( section );
		window.history.replaceState( window.history.state , "load" , location.pathname + "#" + section );
		
		switch ( section ) {
		case "home":
			Calc.navigateToHome();
			break;
		default:
			Calc.navigateToSection( section );
		break;
		}
	};

	// when page is loaded init function is called
	init = function() {
		
		//disable caching
		$.ajaxSetup( { cache: false } );
		
		// load calc info
		Calc._loadInfo();
		
		// init ui managers
		Calc.homeCalculationManager 	= new HomeCalculationManager( $("#calculation") );
		Calc.homeDataManager 			= new HomeDataManager( $("#data") );
		Calc.workspaceSettingsManager	= new WorkspaceSettingsManager( "#workspace-settings" );
		
		//set current home section to calculation
		var calculation = $("#calculation");
		Calc.section = calculation;
		
		//hide other sections to avoid focus on their elements
		calculation.siblings().invisible();
		
		//on load, the footer buttons is positioned to the bottom of the page
		resizeContainer();
		positionFooter();
		
		Calc.workspaceChange(function(workspace) {
			// check if there's a job currently running
			JobManager.getInstance().checkJobStatus();
		});
	
//		console.log( location );
//		var section = location.hash.replace( "#" , "" );
//		var indexOfQuestionMark = section.indexOf("?");
//		if( indexOfQuestionMark > 0 ){
//			section = section.substring( 0 , indexOfQuestionMark );
//		}
//		console.log( section );
//		var btn = $( "[href='"+section+"']" );
//		console.log( btn );
		
		Calc.pushToHistory( "home" );
		
//		console.log( $.url(location.) )
	};
	
	init();
	
});
