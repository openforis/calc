/**
 * manager for Strata settings page
 * @author Mino Togna
 */

StratumManager = function(container) {
	
	this.container = $(container);
	
	// upload csv ui components
	this.uploadSection = this.container.find(".upload-section");
	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	this.table = this.container.find(".strata-table table");
	
	// buttons section
	this.buttonsSection = this.container.find( ".buttons-section" );
	this.saveBtn = this.buttonsSection.find( "[name=save-btn]" );
	this.cancelBtn = this.buttonsSection.find( "[name=cancel-btn]" );
	
	this.init();
};

StratumManager.prototype.init = function(){
	var $this = this;

	// upload csv form methods 
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	$this.import( response.fields.filepath );
	    },
	    error: function (e) {
	    	alert('Error uploading file' + e);
	    	UI.unlock();
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
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
	
	
	// update strata table
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.updateStrata(ws);
	});
	
	this.saveBtn.click(function(e){
		if($this.save){
			$this.save();
		}
	});
	this.cancelBtn.click(function(e){
		if($this.cancel) {
			$this.cancel();
		}
	});
};

StratumManager.prototype.cancel = null;
StratumManager.prototype.save = null;

StratumManager.prototype.show = function() {
	this.container.fadeIn(200);
};
StratumManager.prototype.hide = function() {
	this.container.hide();
};

StratumManager.prototype.import = function(filepath) {
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspaceImportStrata(filepath, function(ws){
		UI.unlock();
		
		$this.updateStrata(ws);
	});
	
};

StratumManager.prototype.updateStrata = function(ws) {
//	var $this = this;
	var thead = this.table.find("thead"), 
		tbody = this.table.find("tbody");
	
	tbody.empty();
	
	if( ws.strata && ws.strata.length > 0 ) {
		thead.show();
		
		$.each(ws.strata,function(i,stratum){
			var tr = $( "<tr></tr>" );
			tr.hide();
			
			var no = $( "<td></td>" );
			no.html( stratum.stratumNo );
			tr.append(no);
			
			var caption = $( "<td></td>" );
			caption.html( stratum.caption );
			tr.append(caption);
			
			tbody.append(tr);
			var delay = 25;
			setTimeout(function(e){
				tr.fadeIn(delay);
			} , (delay*i) );
		});
	} else {
		thead.hide();
	}
};
