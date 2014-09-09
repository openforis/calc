/**
 * String utility functions
 * 
 * @author Mino Togna
 * @author S. Ricci
 */

StringUtils = function() {};

/**
 * Returns the last word in a string using the specified delimiter
 * 
 * @param str The string
 * @param delimiters (optional) Array of characters considered for splitting the text into words (empty space is the default)
 */
StringUtils.getLastWord = function (str, delimiters, endIndex) {
	if ( ! delimiters ) {
		delimiters = [' '];
	} else if ( typeof delimiters == "string" ) {
		delimiters = [delimiters];
	}
	if ( endIndex == 0 ) {
		return "";
	}
	if ( ! endIndex ) {
		endIndex = str.length;
	}
	var startIndex = 0;
	for(var i = 0; i < endIndex; i++ ) {
		var c = str.charAt(i);
		if ( delimiters.indexOf(c) >= 0 ) {
			startIndex = i+1;
		}
	}
	var result = str.substring(startIndex, endIndex);
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
 * Returns true if the string contains the value
 * Default is case insensitive
 */
StringUtils.contains = function( string , value , caseSensitive ){
	if ( !caseSensitive ){
		string = string.toLowerCase();
		value = value.toLowerCase();
	}
	var result = string.indexOf(value) >= 0;
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


/**
 * Returns true if the string passed as argument is blank
 * @param string
 * @returns {Boolean}
 */
StringUtils.isBlank = function( string ){
	if( string ){
		return $.trim( string ) == "";
	}
	return true;
};

/**
 * Returns false if the string passed as argument is blank
 * @param string
 * @returns
 */
StringUtils.isNotBlank = function( string ){
	return ! StringUtils.isBlank( string );
};
