/**
 * manager for cluster settings page
 * @author Mino Togna
 */

AoiJoinManager = function(container , sdManager) {	
	this.container = $(container);
	this.sdManager = sdManager;
	
	this.tableColumnSelector = new TableColumnSelector( this.container.find(".table-column-selector") );
};

AoiJoinManager.prototype.show = function() {
	this.container.fadeIn(200);
	
	this.updateJoinColumn();
};

AoiJoinManager.prototype.hide = function() {
	this.container.hide();
};

AoiJoinManager.prototype.updateJoinColumn = function() {
	var sd = this.sdManager.samplingDesign;
	
	if( sd.samplingUnitId ) {
//		var tableInfo = ( sd.twoPhases === true ) ? this.sdManager.phase1TableInfo : this.sdManager.samplingUnitTableInfo;
//		var header = ( sd.twoPhases === true ) ? "phase 1 table aoi column" : this.sdManager.samplingUnitTableInfo.table + " table aoi column";
		var tableInfo 	= null;
		var header 		= null;
		if(  this.sdManager.samplingDesign.twoPhases === true  ){
			tableInfo 	= this.sdManager.phase1TableInfo;
			header 		= "phase 1 table aoi column";
		} else if(  this.sdManager.samplingDesign.twoStages === true  ){
			tableInfo 	= this.sdManager.primarySUTableInfo;
			header 		= "PSU table aoi column";
		} else {
			tableInfo 	= this.sdManager.samplingUnitTableInfo;
			header		= this.sdManager.samplingUnitTableInfo.table + " table aoi column";	
		}
		this.tableColumnSelector.setTableInfo( tableInfo , header);
		
		var options = sd.aoiJoinSettings; 
		if( options ){
			this.tableColumnSelector.settings = options;
		}
		
		this.tableColumnSelector.show();
	}
};

AoiJoinManager.prototype.validate = function() {
	if( this.tableColumnSelector.joinColumn && this.tableColumnSelector.joinColumn !== "" ) {
		return true;
	} else {
		UI.showError("Aoi column must be specified", false);
		return false;
	}
};

AoiJoinManager.prototype.joinOptions = function() {
	return this.tableColumnSelector.jsonSettings();
};
