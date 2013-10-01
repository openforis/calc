/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */

home = "home.html";
$page = $("#page");
$nav = $(".container ul.breadcrumb");

loadPage = function(page) {
	$page.hide();
	$page.html("");
	
	$.ajax({
		  url: page,
		  dataType: "html"
	})
	.done(function(response) {
		if(page == home) {
			$nav.hide();
		} else {
			$nav.fadeIn(500);
		}
		$page.html(response);
		$page.fadeIn(500);
	});
	
}; 

$(document).ready(function() {
	
//	$(".row-section .menu a").on("click", function(event) {
//		event.preventDefault();
//		
//		$href = $(this).attr("href");
//		console.log($href);
//	});
//	
//	$(".home-label-link").on("click", function(event) {
//		event.preventDefault();
//		
//		$href = $(this).attr("href");
//		console.log($href);
//	});
	
	$nav.find("a").on("click", function(event) {
		event.preventDefault();
		
		$href = $(this).attr("href");
		loadPage($href);
	});
	
	loadPage("home.html");
});