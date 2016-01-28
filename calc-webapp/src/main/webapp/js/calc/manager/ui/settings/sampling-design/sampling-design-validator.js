/**
 * 
 */

SamplingDesignValidator = function( samplingDesignEditManager ){
	this.samplingDesignEditManager = samplingDesignEditManager;
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
	if( this.samplingDesignEditManager.samplingDesign.samplingUnitId ){
		return true;
	} else {
		UI.showError("Select a valid sampling unit", false);
		return false;
	}
};