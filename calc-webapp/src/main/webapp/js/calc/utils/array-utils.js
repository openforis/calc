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

/**
 * Returns true if the array contains the specified item 
 */
ArrayUtils.contains = function(array, item) {
	return array.indexOf(item) >= 0;
};