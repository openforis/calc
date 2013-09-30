/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */
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
});