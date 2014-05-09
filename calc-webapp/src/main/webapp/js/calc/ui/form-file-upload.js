/**
 * Manages the upload of a file using a form together with a progress section
 * 
 * @author S. Ricci
 * 
 */
FormFileUpload = function ( formSection, progressSection, uploadCallback ) {
	//main UI containers
	this.formSection 		= $(formSection);
	this.progressSection 	= progressSection == null ? null : $(progressSection);

	//success function called when upload completes successfully
	this.uploadCallback = uploadCallback;
	
	//upload UI elements
	this.form 				= this.formSection.find("form");
	this.uploadButton 		= this.formSection.find("button[name=upload-btn]");
	this.fileInput 			= this.form.find("input[type=file]");
	
	//progress UI elements
	this.progressBar = this.progressSection == null ? null: new ProgressBar( this.progressSection, this.progressSection.find(".percent") );
	
	this.init();
};

FormFileUpload.prototype.init = function() {

	var $this = this;
	
	if ( this.progressSection ) {
		this.progressSection.hide();
	}
	
	this.form.ajaxForm( {
	    dataType : 'json',
	    
	    beforeSubmit: function() {
	    	UI.lock();
	    	
	        $this.form.addClass('loading');
	        
	        $this.formSection.fadeOut();
	        
	        if ( $this.progressSection ) {
				$this.progressSection.fadeIn();
				$this.progressBar.update(0, 100);
	        }
	    },
	    
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    	if ( $this.progressSection ) {
	    		$this.progressBar.update(position, total);
	    	}
	    },
	    
	    success: function ( response ) {
	    	if ( $this.progressSection ) {
	    		$this.progressBar.progressSuccess();
	    	}
	    	if ( $this.uploadCallback ) {
	    		$this.uploadCallback( response );
	    	}
	    },
	    
	    error: function () {
			Calc.error.apply( this , arguments );
	    },
	    
	    complete: function() {
	    	UI.unlock();
	    	
	    	// replace input file with a new one (IE compatibility)
	    	$this.fileInput.replaceWith ( $this.fileInput = $this.fileInput.clone ( true ) );
	    	
	    	$this.formSection.fadeIn();
	    	
	    	if ( $this.progressSection ) {
	    		$this.progressSection.fadeOut();
	    		$this.progressBar.update(0,100);
	    	}
	    }
	});	
	
	this.uploadButton.click(function(event) {
		$this.fileInput.click();
	});
	
	this.fileInput.change( function(event) {
		$this.form.submit();
	});
};
