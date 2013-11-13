StringUtils = function() {};

/**
 * Returns the last word in a string using the specified delimiter
 * 
 * @param str The string
 * @param delimiter (optional)
 */
StringUtils.getLastWord = function (str, delimiter, endIndex) {
	if ( ! delimiter ) {
		delimiter = ' ';
	}
	if ( endIndex == 0 ) {
		return "";
	}
	if ( ! endIndex ) {
		endIndex = str.length;
	}
	var result = "";
	for(var i = 0; i < endIndex; i++ ) {
		var c = str.charAt(i);
		if ( c == delimiter ) {
			result = "";
		} else {
			result += c;
		}
	}
	return result;
};

/**
 * Replaces the text between the specified indexes in a string with a new one 
 * @param str
 * @param startIndex
 * @param endIndex
 */
StringUtils.replaceText = function (str, value, startIndex, endIndex) {
	var firstPart = str.substring(0, startIndex);
	var lastPart = str.substring(endIndex);
	var result = firstPart + value + lastPart;
	return result;
};

/**
 * Returns true is the String str starts with the given value 
 * 
 * @param str
 * @param value
 * @returns {Boolean}
 */
StringUtils.startsWith = function (str, value, caseSensitive) {
	if ( ! caseSensitive ) {
		str = str.toLowerCase();
		value = value.toLowerCase();
	}
	var result = str.indexOf(value) == 0;
	return result;
};

/**
 * Encodes a string into HTML
 * 
 * @param text
 * @returns
 */
StringUtils.encodeHtml = function (text) {
	return $('<div/>').text(text).html();
};

/**
 * Decodes a encoded HTML string
 * 
 * @param text
 * @returns
 */
StringUtils.decodeHtml = function (html) {
	return $('<div/>').html(html).text();
};
