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

/**
 * Returns the item that has the specified property equals to the specified value 
 */
ArrayUtils.getItemByProperty = function(array, propertyName, value) {
	for(var i=0; i < array.length; i++) {
		var item = array[i];
		if ( item.hasOwnProperty(propertyName) ) {
			var itemValue = item[propertyName];
			if ( itemValue == value ) {
				return item;
			}
		}
	}
	return null;
};

/**
 * Returns a copy of the given array
 */
ArrayUtils.clone = function(array) {
	return array.slice(0);
};