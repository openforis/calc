/**
 * Manages file upload.
 * 
 * It solves an issue using IE8 onwards ('value' property for input with type 'file' is readonly).
 * Instead of setting the file input field value to empty, it replaces the entire input field with a new one.
 * 
 * 
 * @author S. Ricci
 */
FileUpload = function(browseButton, fileInput, change) {
	/**
	 * Button that will trigger the file browsing
	 */
	this.browseButton = browseButton;
	/**
	 * Input field with type "file" used to upload the file to the server
	 */
	this.fileInput = fileInput;
	/**
	 * Called when file is selected
	 */
	this.change = change;
	
	this.init();
};

/**
 * Initializes all the event handlers.
 */
FileUpload.prototype.init = function () {
	var $this = this;
	
	this.browseButton.click(function(event) {
		$this.fileInput.click();
	});
	
	this.fileInput.change( function(event) {
		if ( $this.change ) {
			$this.change( event );
		}
	});
	
};

/**
 * Resets the file input field
 */
FileUpload.prototype.reset = function () {
	this.fileInput.replaceWith ( this.fileInput = this.fileInput.clone ( true ) );
};
