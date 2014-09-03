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

Utils.isBlankString = function( string ){
	if( string ){
		return $.trim( string ) == "";
	}
	
	return true;
}