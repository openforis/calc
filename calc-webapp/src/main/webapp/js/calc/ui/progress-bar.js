/**
 * Progress bar 
 * 
 * @author Mino Togna
 */
ProgressBar = function(container, percent) {
	
	this.container = $(container);
	this.progressBar = this.container.find('.progress-bar');
	// optional percent component
	this.percentSection = $(percent);
};

ProgressBar.prototype = (function() {
	
	// update progress bar 
	var update = function( completed, total ){
		this.progressBar.addClass("progress-bar-info");
		
		var percent = (total > 0 ) ?  parseInt(completed / total * 100) : -1 ;
		
		if( percent >= 0 ){ 
			this.progressBar.width( percent + "%" );
		} else {
			this.progressBar.parent().addClass("active progress-striped");
			this.progressBar.width("100%");
		}
		
		// update percent text if set
		if( this.percentSection ){
			var htmlPercent = (percent >= 0) ? percent + " %" : " -% ";
			this.percentSection.text( htmlPercent );
		}
		// if completed added success class
		if( completed == total ){
			this.progressBar.removeClass();
			this.progressBar.addClass("progress-bar progress-bar-success");
			this.progressBar.width("100%");
			this.percentSection.text("100%");
		}
	};
	
	// reset method
	var reset = function() {
		this.progressBar.removeClass();
		this.progressBar.addClass("progress-bar");
		this.progressBar.parent().removeClass();
		this.progressBar.parent().addClass("progress");
	};
	
	return {
		constructor : ProgressBar
		,
		update : update
		,
		reset : reset
	};
	
})();