/**
 * Job Status modal div ui component (defined in index.html)
 * 
 * @author Mino Togna
 */
JobStatus = function() {
	
	this.container = $( "#job-status" );
	// status header text 
	this.status = this.container.find( ".status" );
	// ui container for the tasks
	this.tasks = this.container.find( ".modal-body" );
	// template for taskStatus
	this.taskStatus = $( "#task-status" );

	// log section
	this.logSection = this.container.find(".log-section");
	// log
	this.log = this.logSection.find(".log");
	// toggle-log-btn
	this.logBtn = this.logSection.find(".toggle-log-btn");
	
	// close button
	this.closeBtn = this.container.find(".close-btn");
	
	// current job
	this.job = null;
};

JobStatus.prototype = (function() {
	
	var show = function() {
		// first reset and then shows it
		$.proxy(reset, this)();
		
		this.container.modal( {keyboard:false, backdrop:"static"} );
	};
	
	// update task status
	var update = function(job, complete, hideOnComplete) {
		var $this = this;
		
		if(!this.job) {
			$.proxy(init, this)(job);
		}
		
		// update each task 
		var tasks = job.tasks;
		$.each(tasks, function(i, task) {
			var taskStatus =  $this.tasks.find("#" + task.id);

			var progressBar = taskStatus.data("progress-bar");
			progressBar.update( task.itemsProcessed, task.totalItems );
			
			if( task.totalItems <= 0 ) {
				switch(task.status) {
					case "PENDING":
						break;
					case "RUNNING":
						progressBar.progressStriped();
						break;
//					case "FAILED":
//						progressBar.progressDanger();
//						break;
					case "COMPLETED":
						progressBar.progressSuccess();
						break;
					default:
				}
			}
//			else {
			if(task.status == "FAILED") {
				progressBar.progressDanger();
			}
//			}

		});
		
		// update log if there is
		if( job.rlogger ) {
			var lines = job.rlogger.lines;
			var processedLines = this.log.children().length;
			var delay = 50;
			
			var newLines = lines.slice(processedLines, lines.length);
			$.each(newLines, function(i, line){
				// just faking spaces
				var textLine = "<span>" + line.text.replace(/ /g, '<span class="space">&nbsp;</span>')  + "</span>";//.replace(/[\s]/g, "\u0020");;
				
				var div = $('<div class="row"></div>');
				div.hide();
				
				var match = line.text.match(/.{1}/);
//				$.each(line.text, function(i, line){
				for(var k = 0; k< line.text.length; k++ ){
					var char = line.text[k];
//					console.log("char " + k);
					var span =$('<span class="space"></span>');
//					span.html( char.replace(/ /g,'&nbsp') );
					span.html( char );
					div.append(span);
//					console.log(char);
				}
//				});
				
//				var s = $("<span>> </span>");
//				s.disableSelection();
//				div.append( s );
//				div.append( $(textLine) );
				$this.log.append(div);
//				setTimeout(function(){
					div.fadeIn(300);
					// scroll only at the end
					if(i == newLines.length-1){
						setTimeout(function(){
							$this.log.stop().animate({
								scrollTop: div.offset().top
							}, 0);
						}, 1000);
					}
//				}, (delay+=50) );

			});
		}
		
		var $this = this;
		// show/hide close btn
		switch(job.status) {
			case "PENDING":
			case "RUNNING":
				break;
			case "COMPLETED":
				this.closeBtn.on("click", function(){
					$this.hide(complete, job);
				});
				this.closeBtn.show();
				// auto hide disabled for now
//				if( hideOnComplete === true ) {
//					$this.hide(complete, job);
//				}
				break;
			case "FAILED":
				this.closeBtn.on("click", function(){
					$this.hide();
				});
				this.closeBtn.show();
				break;
			default:
//				this.closeBtn.show();
		}
	};
	
	var hide = function(callback, job) {
		this.container.modal( "hide" );
		$.proxy(reset,this)();
		if(callback){
			callback(job);
		}
	};
	
	// initialize ui with given job
	var init = function(job) {
		var $this = this;
		$this.job = job;
		
		this.status.html( this.job.status.toLowerCase() );
		
		var delay = 50;
		var tasks = this.job.tasks;
		$.each(tasks, function(i, task) {
			// add a row for each task
			var taskStatus = $this.taskStatus.clone();
			taskStatus.hide();
			
			taskStatus.attr("id", task.id);
			taskStatus.find(".number").text( (i+1) +"." );
			taskStatus.find(".name").text( task.name );
			// add a progress bar to each task
			var progressBar = new ProgressBar( taskStatus.find(".progress"), taskStatus.find(".percent") );
			taskStatus.data("progress-bar", progressBar);

			$this.tasks.append(taskStatus);	

			setTimeout(function(){
				taskStatus.fadeIn(100);
			}, (delay+=50) );
		});
		
		// init r logger if calcjob
		if( $this.job.rlogger ) {
			// show log section
			$this.logSection.show();
			if( $this.logBtn.hasClass("option-btn-selected") ){
				// set logSection height
				var height = $(document).height() / 5;
				$this.logSection.css({"height":height});
			}
			
			var clickFunction = function(e){
				if( $this.logBtn.hasClass("option-btn-selected") ){
					// click to hide log
					$this.logBtn.removeClass("option-btn-selected");
					$this.logBtn.addClass("option-btn");
					
					$this.logSection.animate({ height: "80px" }, 800);
					setTimeout(function(){
						$this.log.animate({ opacity: ".2" }, 800);
					},800); 
					
				} else {
					// click to show log
					$this.logBtn.removeClass("option-btn");
					$this.logBtn.addClass("option-btn-selected");
					
					var height = $(document).height() / 5;
					$this.logSection.animate({ height: height }, 800);
					$this.log.animate({ opacity: "1" }, 800);
				}	
			};
			$this.logBtn.on("click", clickFunction);
			
			// resize log on window resize
			$this.updateLogSectionHeight = function() {
				console.log("resize");
				if( $this.logBtn.hasClass("option-btn-selected") ){
					var height = $(document).height() / 5;
					$this.logSection.animate({ height: height }, 600);
				}
			};
			$(window).on("resize", $this.updateLogSectionHeight);
			
		}
	};
	
	
	
	// reset its internal state
	var reset = function() {
		this.status.html( "Waiting job status" );
		this.tasks.empty();

		// empty log
		this.logSection.hide();
		this.log.empty();
//		this.log.css({ opacity: "1" });
		this.logBtn.off("click");
		UI.enable( this.logBtn );
		//(3 below lines) let's leave the last ui status. maybe users don't want to see the log? and always have to click it? 
//		this.log.show();
//		this.logBtn.removeClass("option-btn");
//		this.logBtn.addClass("option-btn-selected");
		
		this.closeBtn.off("click");
		this.closeBtn.hide();
		
		this.job = null;
		
		$(window).off("resize", $this.updateLogSectionHeight);
	};
	
	return {
		constructor : JobStatus
		,
		show : show
		,
		hide : hide
		,
		update : update
	};
	
})();