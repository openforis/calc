/**
 * manager for cluster settings page
 * @author Mino Togna
 */

ClusterManager = function(container , sdManager) {	
	this.container = $(container);
	this.sdManager = sdManager;
	
	this.tableColumnSelector = new TableColumnSelector( this.container.find(".table-column-selector") );
};

ClusterManager.prototype.show = function() {
	this.container.fadeIn(200);
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
		this.updateJoinColumn(ws);
	} , this ) );
};

ClusterManager.prototype.hide = function() {
	this.container.hide();
};

ClusterManager.prototype.updateJoinColumn = function(ws) {
	var sd = this.sdManager.samplingDesign;
	if( sd.cluster === true ) {
		
		var tableInfo = ( sd.twoPhases === true ) ? this.sdManager.phase1TableInfo : this.sdManager.samplingUnitTableInfo;
		var header = ( sd.twoPhases === true ) ? "phase 1 table cluster column" : this.sdManager.samplingUnitTableInfo.table + " table cluster column";
		this.tableColumnSelector.setTableInfo( tableInfo , header);
		
		var options = sd.clusterColumnSettings; 
		if( options ){
			this.tableColumnSelector.settings = options;
		}
		this.tableColumnSelector.show();
	} else {
		this.tableColumnSelector.hide();

	}
};

ClusterManager.prototype.validate = function() {
	if( this.tableColumnSelector.joinColumn && this.tableColumnSelector.joinColumn !== "" ) {
		return true;
	} else {
		UI.showError("Cluster column must be specified", false);
		return false;
	}
};

ClusterManager.prototype.joinOptions = function() {
	return this.tableColumnSelector.jsonSettings();
};

ClusterManager.prototype.setJoinOptions = function(options){
	this.tableColumnSelector.settings = options;
};
