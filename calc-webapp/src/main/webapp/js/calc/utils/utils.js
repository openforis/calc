/**
* Common utility functions
* @author Mino Togna
*/

Utils = function(){};

Utils.isFunction = function( obj ) {
	return !!(obj && obj.constructor && obj.call && obj.apply);
};

Utils.applyFunction = function( functx ) {

	if( Utils.isFunction(functx) ){
		var args = null;
		if( arguments.length > 1 ){
			args = Array.prototype.splice.call(arguments, 1, arguments.length);
		}
		
		functx.apply( null , args );
	}
};

// left for backwards compatibility
Utils.isBlankString = function( string ){
	return StringUtils.isBlank( string );
};