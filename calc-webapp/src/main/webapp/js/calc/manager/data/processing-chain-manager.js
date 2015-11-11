/**
 * Manager for processing chain
 * @author Mino Togna
 */

ProcessingChainManager = function() {
	
	
	this.contextPath = "rest/processing-chain/";
	
	this.wsManager	= WorkspaceManager.getInstance();
	
};

/**
 * Exports the processing chain 
 */
ProcessingChainManager.prototype.export = function() {
	var $this = this;
	this.wsManager.activeWorkspace( function(ws){
		
		var url = $this.contextPath + ws.name + "-processing-chain.zip";	
		UI.Form.download( url );
	});
};


// singleton instance of processing chain manager
var _pcManager = null;
ProcessingChainManager.getInstance = function() { 
	if(!_pcManager){
		_pcManager = new ProcessingChainManager();
	}
	return _pcManager;
};

