ArrayUtils = function() {};

/**
 * Removes the specified item from the specified array, if found
 */
ArrayUtils.removeItem = function(array, item) {
	var index = array.indexOf(item);
	if ( index >= 0 ) {
		array.splice(index, 1);
	}
};