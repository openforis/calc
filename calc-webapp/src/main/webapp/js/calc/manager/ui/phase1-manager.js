/**
 * manager for phase 1 plots manager
 * @author Mino Togna
 */

Phase1Manager = function(container) {
	
	this.container = $(container);
	
	// upload csv ui components
	this.uploadSection = this.container.find(".upload-section");
	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	this.table = this.container.find(".phase1-table table");
	
	this.init();
};

Phase1Manager.prototype.init = function(){
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
	    	$this.showImport( response.fields.filepath, response.fields.headers );
	    },
	    error: function (e) {
	    	alert('Error uploading file' + e);
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
	    	UI.unlock();
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
	
	
	// update aoi tree
//	$this.initSvg();
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.updateTable(ws);
	});
};

Phase1Manager.prototype.showImport = function( filepath, headers ){
	console.log( filepath );
};

Phase1Manager.prototype.import = function(filepath) {
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspaceImportPhase1Plots(filepath, function(ws){
		UI.unlock();
		
		$this.updateTable(ws);
	});
	
};


Phase1Manager.prototype.updateTable = function(ws) {
//	var $this = this;
	var thead = this.table.find("thead"), 
		tbody = this.table.find("tbody");
	
	tbody.empty();
	
	if( ws.strata && ws.strata.length > 0 ) {
//		thead.show();
//		
//		$.each(ws.strata,function(i,stratum){
//			var tr = $( "<tr></tr>" );
//			tr.hide();
//			
//			var no = $( "<td></td>" );
//			no.html( stratum.stratumNo );
//			tr.append(no);
//			
//			var caption = $( "<td></td>" );
//			caption.html( stratum.caption );
//			tr.append(caption);
//			
//			tbody.append(tr);
//			var delay = 50;
//			setTimeout(function(e){
//				tr.fadeIn(delay);
//			} , (delay*i) );
//		});
	} else {
		thead.hide();
	}
};
