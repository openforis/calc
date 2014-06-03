/**
* Common utility functions
* @author Mino Togna
*/

Utils = function(){};

Utils.isFunction = function( obj ) {
	return !!(obj && obj.constructor && obj.call && obj.apply);
};
