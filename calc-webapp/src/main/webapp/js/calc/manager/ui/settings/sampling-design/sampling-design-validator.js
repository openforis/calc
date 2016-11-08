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
 * AOI validation
 */
SamplingDesignValidator.prototype.validateStep0 = function(){
	var valid = true;
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		if( !ws.aoiHierarchies || ws.aoiHierarchies.length <= 0 ) {
			UI.showError( "Upload a valid 'Reporting Unit (AOI)' csv file.", true );
			valid = false;
		}
	});
	return valid;
};


/**
 * Base unit validation
 */
SamplingDesignValidator.prototype.validateStep1 = function(){
	if( this.sd().samplingUnitId ){
		return true;
	} else {
		UI.showError( "Select a valid Base Unit", true );
		return false;
	}
};

/**
 * Two phases validation
 */
SamplingDesignValidator.prototype.validateStep2 = function(){
	var $this = this;
	if( this.sd().twoPhases === true ){
		
		var valid = false;
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.phase1PlotTable ){
				UI.showError("Upload a valid first phase csv file", true);
				valid = false;
			} else {
				valid = $this.validateJoinSettings( $this.sd().phase1JoinSettings );
				if( !valid ){
					UI.showError("Join with base unit table is not valid.", true);
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
SamplingDesignValidator.prototype.validateStep3 = function(){
	var valid = true;
	if( this.sd().twoStages === true ){
		var settings 		= this.sd().twoStagesSettings;
		var joinSettings 	= settings.joinSettings;
		
		if(  !this.samplingDesignEditManager.samplingDesignERDManager.psuManager.dataProvider.getTableInfo() ){
			valid = false;
			UI.showError( "Upload a valid primary sampling unit (PSU) csv file", true );
		}
		else if( StringUtils.isBlank(settings.areaColumn) ){
			valid = false;
			UI.showError( "Select a valid 'Area' column", true );
		}
		else if( StringUtils.isBlank(settings.noBaseUnitColumn) ){
			valid = false;
			UI.showError( "Select a valid 'No. base unit' column", true );
		} else if( ! settings.ssuOriginalId ){
			valid = false;
			UI.showError( "Select a valid secondary sampling unit (SSU)", true );
		} else if( !this.validateJoinSettings(joinSettings) ){
			valid = false;
			UI.showError("Join between PSU and SSU tables is not valid.", true);
		}
	}
	return valid;
};


/**
 * Validate stratified sampling 
 */
SamplingDesignValidator.prototype.validateStep4 = function(){
	var $this = this;
	var valid = true;
	if( this.sd().stratified === true ){
		
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.strata || ws.strata.length <= 0) {
				UI.showError("Upload a valid Strata definition csv file", true);
				valid = false;
			} else {
				var settings = $this.sd().stratumJoinSettings; 
				if( !(settings.table && settings.schema && settings.column) ){
					UI.showError("Join with Stratum Labels table is not valid.", true);
					valid = false;
				}
			}
		} );
		
	} 
	return valid;
};

/**
 * Validate cluster settings
 */
SamplingDesignValidator.prototype.validateStep5 = function(){
	var valid = true;
	if( this.sd().cluster2 === true ){
		
		var settings = this.sd().clusterColumnSettings;
		if(!this.sd().clusterOriginalId ){
			UI.showError( "Select a valid cluster entity", true );
			valid = false;
		}
		
	}
//	if( this.sd().cluster === true ){
//		
//		var settings = this.sd().clusterColumnSettings;
//		if(!(settings 
//				&& StringUtils.isNotBlank(settings.table) 
//				&& StringUtils.isNotBlank(settings.schema) 
//				&& StringUtils.isNotBlank(settings.column) 
//			)){
//			UI.showError( "Select a valid cluster column", true);
//			valid = false;
//		}
//		
//	}
	return valid;
};

/**
 * Validate aoi column 
 */
SamplingDesignValidator.prototype.validateStep6 = function(){
	var valid = true;
	var settings = this.sd().aoiJoinSettings;
	if(!(settings 
			&& StringUtils.isNotBlank(settings.table) 
			&& StringUtils.isNotBlank(settings.schema) 
			&& StringUtils.isNotBlank(settings.column) 
		)){
		UI.showError( "Join with Reporting Unit (AOI) table is not valid.", true );
		valid = false;
	}
	
	return valid;
};