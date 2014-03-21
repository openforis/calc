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
	var update = function( completed, total ) {
		this.progressBar.addClass("progress-bar-info");
		
		if( completed > 0 ) {
			var percent = (total > 0 ) ?  parseInt(completed / total * 100) : -1 ;
			
			// update progress bar
			if( percent >= 0 ){ 
				this.progressBar.width( percent + "%" );
			} else {
//				this.progressBar.parent().addClass("active progress-striped");
//				this.progressBar.width("100%");
				this.progressStriped();
			}
			
			// update percent text if set
			if( this.percentSection ){
				var htmlPercent = (percent >= 0) ? percent + " %" : " -% ";
				this.percentSection.text( htmlPercent );
			}
		} 
//		else {
//			if( this.percentSection ){
//				this.percentSection.text( " -% " );
//			}
//		}
		
		
		// if completed add success class
		if( completed == total ) {
			this.progressSuccess();
		}
	};
	
	// reset method
	var reset = function() {
		this.progressBar.removeClass();
		this.progressBar.addClass("progress-bar");
		this.progressBar.parent().removeClass();
		this.progressBar.parent().addClass("progress");
		this.progressBar.width("0%");
		if( this.percentSection ){
			this.percentSection.text("");
		}
	};
	
	// utility methos
	var progressStriped = function(){
		this.reset();
		this.progressBar.parent().addClass("active progress-striped");
		this.progressBar.addClass("progress-bar progress-bar-info");
		this.progressBar.width("100%");
	};
	
	var progressSuccess = function() {
		this.reset();
		this.progressBar.addClass("progress-bar progress-bar-success");
		this.progressBar.width("100%");
		
		if( this.percentSection ) {
			this.percentSection.text( "100%" );
		}
	};
	
	var progressDanger = function() {
		this.reset();
		this.progressBar.addClass("progress-bar progress-bar-danger");
		this.progressBar.width("100%");
	};
	
	return {
		constructor : ProgressBar
		,
		update : update
		,
		reset : reset
		, 
		progressStriped : progressStriped
		, 
		progressSuccess : progressSuccess
		,
		progressDanger : progressDanger
	};
	
})();