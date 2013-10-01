/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */

$page = $("#page");

loadPage = function(page) {
	$page.html("");
	
	$.ajax({
		  url: page,
		  dataType: "html"
	})
	.done(function(response) {
		$page.html(response);
	});
}; 

$(document).ready(function() {
	
	$(".row-section .menu a").on("click", function(event) {
		event.preventDefault();
		
		$href = $(this).attr("href");
		console.log($href);
	});
	
	$(".home-label-link").on("click", function(event) {
		event.preventDefault();
		
		$href = $(this).attr("href");
		console.log($href);
	});
	
	loadPage("home.html");
});