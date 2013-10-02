/**
 * Custom javascript for calc 
 * 
 *  @author Mino Togna
 */

/**
 * Global variables
 */
home = "home.html";
$page = $("#page");
$nav = $(".container ul.breadcrumb");

/**
 * Global functions
 */
checkJobStatus = function() {
//	console.log("aaaa2");
	$.ajax({
		url: "rest/workspace/job.json",
		dataType: "json"
	})
	.done(function(response){
//		console.log("aaaa");
		console.log(response);
	}); 
};

loadPage = function(page) {
	$page.hide();
	$page.empty();
	
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
		
		checkJobStatus();
	});
	
}; 


$(document).ready(function() {
		
	$("a").click(function(event) {
		event.preventDefault();
		
		$href = $(this).attr("href");
		loadPage($href);
	});
	
	loadPage( home );
});