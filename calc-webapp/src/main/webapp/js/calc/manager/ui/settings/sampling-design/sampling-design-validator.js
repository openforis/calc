/**
 * 
 */

SamplingDesignValidator = function( samplingDesignEditManager ){
	this.samplingDesignEditManager = samplingDesignEditManager;
};

SamplingDesignValidator.prototype.sd = function(){
	return  this.samplingDesignEditManager.samplingDesign;
};

SamplingDesignValidator.prototype.isValid = function( stepNo ){
	var valid = true;
	
	var functx = this[ "validateStep" + stepNo ] ;
	if( Utils.isFunction(functx) ){
		valid = $.proxy( functx , this )();
	}
	
	return valid;
};

/**
 * Sampling unit validation
 */
SamplingDesignValidator.prototype.validateStep0 = function(){
	if( this.sd().samplingUnitId ){
		return true;
	} else {
		UI.showError("Select a valid sampling unit", false);
		return false;
	}
};

/**
 * Two phases validation
 */
SamplingDesignValidator.prototype.validateStep1 = function(){
	var $this = this;
	if( this.sd().twoPhases === true ){
		
		var valid = false;
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.phase1PlotTable ){
				UI.showError("Upload a valid first phase csv file", false);
				valid = false;
			} else {
				valid = $this.validateJoinSettings( $this.sd().phase1JoinSettings );
				if( !valid ){
					UI.showError("Join with base unit table is not valid.", false);
				}
			}
		} );
		return valid;
	} else {
		return true;
	}
};

SamplingDesignValidator.prototype.validateJoinSettings = function( joinSettings ){
	var valid = true;
	if( joinSettings ){
		if( !(joinSettings.rightTable && joinSettings.leftTable && joinSettings.rightTable.table && joinSettings.leftTable.table) ){
			valid = false;
		} else {
			var cols = joinSettings.columns;
			if( !cols ){
				valid = false;
			} else {
				for( var i in cols ){
					var col = cols[ i ];
					var left = col.left;
					var right = col.right;
					
					if( StringUtils.isBlank(left) || StringUtils.isBlank(right) ){
						valid = false;
						break;
					}
				}
			}
		}
	} else {
		valid = false;
	}
	return valid;
}

/**
 * Two stages validation
 */
//SamplingDesignValidator.prototype.validateStep2 = function(){
//	if( this.samplingDesign.twoStages === true ){
//		
//		var valid = false;
//		if (! this.primarySUTableInfo ){
//			UI.showError( "Upload a primary sampling unit csv", true );
//			valid = false;
//		} else {
//			valid = this.twoStagesManager.validate();
//			if(valid){
//				this.samplingDesign.twoStagesSettings.joinSettings = this.twoStagesManager.joinOptions();
//			}
//			
//			this.aoiJoinManager.updateJoinColumn();
//		}
//		return valid;
//	} else {
//		this.aoiJoinManager.updateJoinColumn();
//		return true;
//	}
//};
//
//
/**
 * Validate stratified sampling 
 */
SamplingDesignValidator.prototype.validateStep3 = function(){
	var $this = this;
	var valid = true;
	if( this.sd().stratified === true ){
		
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.strata || ws.strata.length <= 0) {
				UI.showError("Upload a valid Strata definition csv file", false);
				valid = false;
			} else {
				var settings = $this.sd().stratumJoinSettings; 
				if( !(settings.table && settings.schema && settings.column) ){
					UI.showError("Join with Stratum Labels table is not valid.", false);
					valid = false;
				}
			}
		} );
		
	} 
	return valid;
};

///**
// * Validate cluster settings
// */
//SamplingDesignValidator.prototype.validateStep4 = function(){
//	var valid = true;
//	
//	if( this.samplingDesign.cluster === true ){
//		valid =  this.clusterManager.validate();
//		if(valid) {
//			this.samplingDesign.clusterColumnSettings = this.clusterManager.joinOptions();
//		}
//	} 
//	
//	return valid;
//	
//};
//
///**
// * Validate aoi column 
// */
//SamplingDesignValidator.prototype.validateStep5 = function(){
//	var	valid =  this.aoiJoinManager.validate();
//	if(valid) {
//		this.samplingDesign.aoiJoinSettings = this.aoiJoinManager.joinOptions();
//	}
//	return valid;
//};