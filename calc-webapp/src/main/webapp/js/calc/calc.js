/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */

/**
 * Global variables
 */
home = "home.html";
$footer = $("#footer");
$footerHomeLinks = $("#footer .links");
$backHomeButton = $("#footer button.back");
$dataVisualization = $("#data-visualization");

/**
 * main html container
 */
$container = $("#container");

//current working section
$section  = null;
/*
 * Home page sections 
*/
$page = $("#page");
$nav = $(".container ul.breadcrumb");

/**
 * Global variables
 * 
 * Managers
 */
homeCalculationManager = null;

/**
 * Global functions
 * 
 * 
 */

$(document).ready(function() {
	
	positionFooter = function() {
		$(".footer-placeholder").css("height", $footer.height());
		$footer.animate({top:$(window).height()- $footer.height() }, 400);
	};
	
	resizeContainer = function() {
		var containerHeight = $(window).height() - $footer.height();
		$container.css({"height":containerHeight+"px"});
	};
	
	var scrollToCurrentSection = function (animate) {
		//calculate the scroll offset
		var scrollTop = $section[0].offsetTop;
		
		if ( animate ) {
			//show all siblings temporarily during scrolling
			$section.siblings().andSelf().visible();
			
			//enable container scrolling during animation
			$container.css('overflow','auto');
			
			var onAnimationComplete = function(){
				//remove scrollbar when animation ends
				$container.css('overflow','hidden');
				//make siblings invisible (block focus of hidden sections)
				$section.siblings().invisible();
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

	$footer.find(".links button").click(function(event){
		event.preventDefault();

		var target = $( $(this).attr("href") );
		
		//set current home section
		$section = target;
		
		scrollToCurrentSection(true);
	});

	// event handler for home button click
	homeButtonClick = function(event){
		event.preventDefault();
		var $button = $(event.currentTarget);
		
		sectionUrl = $button.attr("href");
		//set the current working section (calculation,results,data or settings)
		$section = $button.parents(".section-home");
		//home page section (contains the button links to the external pages)
		$homeSection = $section.find(".page-section");
		if(!sectionUrl) {
			var msg = " Calc error. Section url is undefinded";		
			throw msg;
		}
		$.ajax({
			url: sectionUrl,
			dataType: "html"
//				,
//			data:{"r":Math.random()}
		}).done(function(response){
			var $page = $(response);
			
			//hide loaded page
			$page.hide();
			/**
			 * hide home and shows loaded page
			 */
			//fade out footer links
			$footerHomeLinks.fadeOut(500);
			//move the home section buttons out of the screen towards left
			$homeSection.animate({left:"-="+$(document).width()}, 1000,'easeInOutExpo');
			setTimeout(function(){
				//hide the home section buttons
				$homeSection.hide();
				//append and show the loaded page to the current home section
				$section.append($page);
				$page.show();
				//show the back home button
				$backHomeButton.fadeIn(500);
			},500);
		});
		
		
	};
	
	$(".section-home button.btn-home, .section-home button.btn-home-plus").click(homeButtonClick);
	
	$backHomeButton.click(function(event){
		event.preventDefault();
		
		var $btnSection = $section.find(".page-section:nth-child(1)");
		var $extSection = $section.find(".page-section:nth-child(2)");
		
		//fade out loaded content and back button
		var duration = 500;
		$extSection.fadeOut(duration);
		$backHomeButton.fadeOut(duration);
		//remove loaded page from the document
		setTimeout(function(){
			$extSection.remove();
		}, duration);
		
		//show home section and footer buttons
		$btnSection.show();
		$btnSection.animate({left:"0px"}, 1000, 'easeOutExpo');
		$footerHomeLinks.fadeIn(500);
	});
	
	// on resize window
	$( window ).resize(function() {
		positionFooter();
		resizeContainer();
		scrollToCurrentSection(false);
	});
	
	// when page is loaded init function is called
	init = function() {
		var calculation = $("#calculation");
		
		//set current home section to calculation
		$section = calculation;
		
		//hide other sections to avoid focus on their elements
		calculation.siblings().invisible();
		
		homeCalculationManager = new HomeCalculationManager( calculation );
		var homeDataManager = new HomeDataManager( $("#data") );
		//load all calculation steps
		homeCalculationManager.updateSteps();
		
		JobManager.getInstance().checkJobStatus();
		
		//on load, the footer buttons is positioned to the bottom of the page
		resizeContainer();
		positionFooter();
	};
	
	init();
	
});