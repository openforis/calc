/**
* Common utility functions
* @author Mino Togna
*/

Utils = function(){};

Utils.isFunction = function( obj ) {
	return !!(obj && obj.constructor && obj.call && obj.apply);
};

Utils.applyFunction = function( functx , params ) {
	if( Utils.isFunction(functx) ){
		functx( params );
	}
};

// left for backwards compatibility
Utils.isBlankString = function( string ){
	return StringUtils.isBlank( string );
}