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
	Calc.homeCalculationManager = null;
	Calc.homeDataManager 		= null;

	/**
	 * Ui elements
	 */
	Calc.footer 			= $( "#footer" );
	Calc.footerHomeLinks 	= Calc.footer.find( ".links" );
	Calc.backHomeBtn 		= Calc.footer.find( "button.back" );
	
	
	/**
	 * Function to be called when active workspace change
	 */
	Calc.workspaceChange = function(success) {
		WorkspaceManager.getInstance().refreshActiveWorkspace( function(ws){
			Calc.homeCalculationManager.updateSteps();
			Calc.homeDataManager.refresh();
			if ( success ) {
				success(ws);
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
		
		if( errorThrown ){
			message.append( "<div class='width100'>" + errorThrown + "</div>" );
		}
		
		if( jqXHR.responseText ) {
			message.append( "<div class='width100'>" + jqXHR.responseText + "</div>" );
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
				$container.css('overflow','hidden');
				//make siblings invisible (block focus of hidden sections)
				Calc.section.siblings().invisible();
			};
			//scroll to the current section offset
			$container.stop().animate({scrollTop: scrollTop}, 800, "easeOutQuart", onAnimationComplete);
		} else {
			//scroll to the current section offset
			$container.css('overflow','auto');
			$container[0].scrollTop = scrollTop;
			$container.css('overflow','hidden');
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
		
		$.ajax({
			url: sectionUrl,
			dataType: "html",
			data:{ "t" : new Date().getTime() }
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
		
		
	};
	
	$(".section-home button.btn-home, .section-home button.btn-home-plus").click(homeButtonClick);
	
	Calc.backHomeBtn.click(function(event){
		event.preventDefault();
		
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
	});
	
	// on resize window
	$( window ).resize(function() {
		positionFooter();
		resizeContainer();
		scrollToSection(false);
	});
	
	// when page is loaded init function is called
	init = function() {
		
		// init ui managers
		Calc.homeCalculationManager = new HomeCalculationManager( $("#calculation") );
		Calc.homeDataManager 		= new HomeDataManager( $("#data") );

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
	};
	
	init();
	
});