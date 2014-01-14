/**
 * manager for Areas of interest page
 * @author Mino Togna
 */

AoiManager = function(container) {
	
	this.container = $(container);
	
	// aoi section
	this.aoiSection = this.container.find(".aoi-section");
	
	// upload csv ui components
	this.uploadSection = this.aoiSection.find(".upload-section");
	this.uploadBtn = this.aoiSection.find( "[name=upload-btn]" );
	this.file = this.aoiSection.find( "[name=file]" );
	this.form = this.aoiSection.find( "form" );
	this.uploadProgressBar = new ProgressBar( this.aoiSection.find(".progress"), this.aoiSection.find(".percent") );
	// upload progress
	this.uploadProgressSection = this.aoiSection.find(".progress-section");
	this.uploadProgressSection.hide();
	
	
	// aoi import section
	this.aoiImportSection = this.container.find(".aoi-import-section");
	this.aoiImportSection.hide();
	// levels to import
	this.levelsSection = this.aoiImportSection.find(".levels");
	this.levelSection = this.container.find(".level");
	
	this.importBtn = this.aoiImportSection.find( "[name=import-btn]" );
	this.init();
};

AoiManager.prototype.init = function(){
	var $this = this;

	// upload csv form methods 
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	        $(this).addClass('loading');
	        
	        $this.uploadSection.fadeOut();
			$this.uploadProgressSection.fadeIn();
			$this.uploadProgressBar.update(0, 100);
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    	$this.uploadProgressBar.update(position, total);
//	    	var percentVal = percentComplete+'%';
//	    	$progressBar.width(percentVal);
//	    	$progressPercent.html(percentVal);
	    },
	    success: function ( response ) {
	    	$this.uploadProgressBar.progressSuccess();
//	    	console.log( response );
//	    	JobManager.getInstance().checkJobStatus(function() {
//	    		homeCalculationManager.updateSteps();
//	    	});
	    	$this.showImport(response);
	    },
	    error: function (e) {
	    	alert('Error uploading file' + e);
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
			$this.uploadSection.fadeIn();
			$this.uploadProgressSection.fadeOut();
			$this.uploadProgressBar.update(0,100);
	    }
	});	
	
	this.uploadBtn.click(function(event) {
		event.preventDefault();
		$this.file.click();
	});
	
	this.file.change(function(event) {
		event.preventDefault();
		$this.form.submit();
	});
	
	this.importBtn.click( function(e){ $this.import(); } );
};

AoiManager.prototype.showImport = function(response) {
//	var $this = this;
	
	this.filepath = response.fields.filepath;
	
	this.aoiSection.hide(0);
	this.levelsSection.empty();
	this.aoiImportSection.fadeIn();
	
	var levels = 0;
	var headers = response.fields.headers;
	var completed = false;
	
	while( !completed ) {
		//TODO check that headers have right name
		var headerArea = headers[(levels+1)*2];
		if( headerArea === 'level_' + (levels+1) + '_area' ) {
			completed = true;
		}
		
		//append level section
		var l = this.levelSection.clone();
		var input = l.find("input[type=text]");
		input.attr("name","level");
		input.attr("value","Level " +(levels+1) );
		this.levelsSection.append(l);
		l.show();
		
		levels += 1;
	}
};

AoiManager.prototype.import = function() {
	var $this = this;
	
	var s = this.levelsSection.find("input[type=text]");
	var captions = [];
	$.each(s, function(i,e){
		var caption = $(e).val();
		if(caption=="") {
			UI.showError( "Specify a valid caption for level " + (i+1) , true );
			return;
		}
		captions.push( caption );
	});
	
	$.ajax({
		url : "rest/workspace/active/aoi/import.json",
		dataType : "json",
		method : "POST",
		data : {"filepath":$this.filepath, "captions":captions.join(",")} 
	}).done(function(response) {
		console.log(response);
	});
	
//	console.log(s);
};