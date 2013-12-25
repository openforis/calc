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
		$footer.animate({top:$(window).height()- $footer.height() +1 }, 400);
	};
	
	resizeContainer = function() {
		var containerHeight = $(window).height() - $footer.height();
		$("#container").css({"height":containerHeight+"px"});
	};
	
	$footer.find(".links button").click(function(event){
		event.preventDefault();
		var target = $(this).attr("href");

		$('html, body').stop().animate({
	        scrollTop: $(target).offset().top
	    }, 800);
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
			//remove scrollbar
			$("body").css('overflow','hidden');
			//fade out footer links
			$footerHomeLinks.fadeOut(500);
			//move the home section buttons out of the screen towards left
			$homeSection.animate({left:"-="+$(document).width()}, 1000,'easeInOutExpo');
			setTimeout(function(){
				//hide the home section buttons
				$homeSection.hide();
				// hide all other home sections of the page
				$section.siblings('.section-home').fadeOut();
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
		$extSection.fadeOut(500);
		$backHomeButton.fadeOut(500);
		//remove loaded page from the document
		setTimeout(function(){
			$extSection.remove();
		},500);
		
		//show home section and footer buttons
		//remove scroll when adding the item to the page
		$btnSection.parent().css('overflow','hidden');
		$btnSection.show();
		$btnSection.animate({left:"0px"}, 1000,'easeOutExpo');
		$footerHomeLinks.fadeIn(500);
		
		//show home sections
		$section.siblings('.section-home').fadeIn();
		$('html, body').stop().animate({
	        scrollTop: $section.offset().top
	    }, 0);
		setTimeout(function(){
			//reset scroll bars
			$btnSection.parent().css('overflow','auto');
			$("body").css('overflow','auto');
		}, 800);
	});
	
	// on resize window
	$( window ).resize(function() {
		positionFooter();
		resizeContainer();
	});
	
	// when page is loaded init function is called
	init = function() {
		homeCalculationManager = new HomeCalculationManager($("#calculation"));
		
		//load all calculation steps
		homeCalculationManager.updateSteps();
		
		JobManager.getInstance().checkJobStatus();
		//on load, the footer buttons is positioned to the bottom of the page
		positionFooter();
		
		resizeContainer();
	};
	
	init();
	
});