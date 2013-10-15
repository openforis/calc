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

//current working section
$section  = null;

$page = $("#page");
$nav = $(".container ul.breadcrumb");
$jobStatus = $("#job-status");
$taskStatus = $(".task-status");
/**
 * Global functions
 */
checkJobStatus = function(updateOnly) {
	$.ajax({
		url: "rest/workspace/job.json",
		dataType: "json"
	})
	.done(function(response) {
		$job = response;
		
		if( updateOnly ){
			updateJobStatus($job);
		} else if ( $job.status == 'RUNNING' ) {
			createJobStatus($job);				
		}
	})
	.error(function(e) {
//		console.log(e);
	}); 
};

createJobStatus = function($job) {
	$jobStatus.modal({keyboard:false,backdrop:"static"});
	$jobStatus.find('.modal-title').text($job.name);
	$modalBody = $jobStatus.find('.modal-body');
	$modalBody.empty();
	
	$tasks = $job.tasks;
	
	$.each($tasks, function(i, $task) {
		
		$status = $taskStatus.clone();
		$status.removeClass("hide");
		
		$status.attr("id",$task.id);
		
		$status.find(".number").text( (i+1) +"." );
		$status.find(".name").text( $task.name );
		
		$modalBody.append($status);	
	});
	updateJobStatus($job);
};


updateJobStatus = function($job) {
	$tasks = $job.tasks;
	$.each($tasks, function(i, $task) {
		
		$status = $jobStatus.find("#"+$task.id);
		
		$progressBar = $status.find(".progress-bar");		
		$progressBar.removeClass();
		$progressBar.addClass("progress-bar");
		$progressBar.parent().removeClass();
		$progressBar.parent().addClass("progress");
		
		totalItems = $task.totalItems;
		itemsProcessed = $task.itemsProcessed;
		percent = (totalItems > 0 ) ?  parseInt(itemsProcessed / totalItems * 100) : -1 ;
		
		switch( $task.status ) {
		case "RUNNING": 
			$progressBar.addClass("progress-bar-info");
			if( percent >= 0 ) { 
				$progressBar.width(percent+"%");
			} else {
				$progressBar.parent().addClass("active progress-striped");
				$progressBar.width("100%");
			}
			htmlPercent = (percent >= 0) ? percent + " %" : " -% ";
			$status.find(".percent").text(htmlPercent);
			break;
		case "COMPLETED": 
			$progressBar.addClass("progress-bar-success");
			$progressBar.width("100%");
			$status.find(".percent").text("100%");
			break;
		default: // nothing for now;
		}
		
		$modalBody.append($status);
		
	});

	if( $job.status == "RUNNING" ){
		setTimeout(function(){checkJobStatus(true);}, 1000);
	} else {
		$jobStatus.find(".modal-footer").removeClass("hide");
	}
	
};

//loadPage = function(page) {
//	$page.hide();
//	$page.empty();
//	
//	$.ajax({
//		  url: page,
//		  dataType: "html"
//	})
//	.done(function(response) {
//		if(page == home) {
//			$nav.hide();
//		} else {
//			$nav.fadeIn(500);
//		}
//		$page.html(response);
//		$page.fadeIn(500);
//		
//	});
//	
//}; 


$(document).ready(function() {
		
//	$("a").click(function(event) {
//		event.preventDefault();
//		
//		$href = $(this).attr("href");
//		loadPage($href);
//	});
	
//	loadPage( home );
	//$("html, body").animate({scrollTop: $("#footer").position().top},1000);

	
	positionFooter = function() {
		$footer.animate({top:$(window).height()-200 }, 300);
	};
	
	$footer.find(".links button").click(function(event){
		event.preventDefault();
		target = $(this).attr("href");
		
		$('html, body').stop().animate({
	        scrollTop: $(target).offset().top
	    }, 1200,'easeInOutExpo');
	});
	
	$(".section-home button").click(function(event){
		event.preventDefault();
		
		target = $(this).attr("href");
		//set the current working section (calculation,results,data or settings)
		$section = $(this).parents(".section-home");
		//home page section (contains the button links to the external pages)
		$homeSection = $section.find(".page-section");
		
		$.ajax({
			url:target,
			dataType:"html"
		}).done(function(response){
			$page = $(response);
			
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
		
		
	});
	
	$backHomeButton.click(function(event){
		event.preventDefault();
		
		$btnSection = $section.find(".page-section:nth-child(1)");
		$extSection = $section.find(".page-section:nth-child(2)");
		
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
	
	// on resize windows reposition footer
	$( window ).resize(function() {
		positionFooter();
	});
	
	init = function() {
		checkJobStatus();
		//on load, the footer buttons is positioned to the bottom of the page
		positionFooter();
	};
	
	init();
	
});