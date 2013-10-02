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
	console.log("check job statys");
	$.ajax({
		url: "rest/workspace/job.json",
		dataType: "json"
	})
	.done(function(response) {
		//TODO: open when status is 'RUNNING'
		$job = response;
		if( $job.status == 'RUNNING'){
			$jobStatus.modal({keyboard:false,backdrop:"static"});
			if( updateOnly ){
				updateJobStatus($job);
			} else {
				createJobStatus($job);				
			}
		}
		
		
	}); 
};

createJobStatus = function($job) {
	console.log("createJobStatus");
	
	title = $job.name+" remove this"; 
	$jobStatus.find('.modal-title').html(title);
	$modalBody = $jobStatus.find('.modal-body');
	$modalBody.empty();
	
	$tasks = $job.tasks;
	
	$.each($tasks, function(i, $task) {
		
		$status = $taskStatus.clone();
		$status.removeClass("hide");
		
		$status.attr("id",$task.id);
		
		$status.find(".number").html( (i+1) +"." );
		$status.find(".name").html( $task.name );
		
		$modalBody.append($status);	
	});

	updateJobStatus($job);
};


updateJobStatus = function($job) {
	console.log("updateJobStatus");
	
	$tasks = $job.tasks;
	$.each($tasks, function(i, $task) {
		
		$status = $jobStatus.find("#"+$task.id);
		
//		$status.attr("id",$task.id);
		
		$status.find(".number").html( (i+1) +"." );
		$status.find(".name").html( $task.name );
		
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
			break;
		case "COMPLETED": 
			$progressBar.addClass("progress-bar-success");
			$progressBar.width("100%");
			break;
		default: // nothing for now;
		}
		
		htmlPercent = (percent >= 0) ? percent+" %" : " -% ";
		$status.find(".percent").html();
		
		
		$modalBody.append($status);	
		
		
	});

	if( $job.status == "COMPLETED" ){
		$jobStatus.find(".modal-footer").removeClass("hide");
	} else {
		setTimeout(function(){checkJobStatus(true);}, 1000);
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
	
	$jobStatus.on('shown.bs.modal', function () {
		console.log('showing');
	    $(this).find('.modal-body').css({width:'auto',
	                               height:'auto', 
	                              'max-height':'100%'});
	});
	
	loadPage( home );
	
});