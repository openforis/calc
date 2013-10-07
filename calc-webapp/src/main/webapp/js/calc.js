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
		} else if ($job.status == 'RUNNING') {
			createJobStatus($job);				
		}
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