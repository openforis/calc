/**
 * @author M. Togna
 */

// {
//     beforeSend  : function
//     complete    : function
//     success: success,
//     type | method: The HTTP method to use for the request (e.g. "POST", "GET", "PUT"),
//     url: url,
//     data: Type: PlainObject or String or Array,
//     dataType (default: Intelligent Guess (xml, json, script, or html)),
//     contentType (default: 'application/x-www-form-urlencoded; charset=UTF-8'),
//
//     error: Type: Function( jqXHR jqXHR, String textStatus, String errorThrown )
// }

$.ajaxSetup({
	cache : false,
	type : 'GET',
	dataType : 'json'
});

var ajaxRequest = function(evt, params) {
	$.ajax(params);
}

var getRequest = function(evt, params){
	params.type = 'GET';
	ajaxRequest(evt, params);
}

var postRequest = function(evt, params){
	params.type = 'POST';
	ajaxRequest(evt, params);
}

EventBus.addEventListener("ajax", ajaxRequest);
EventBus.addEventListener("ajax.get", getRequest);
EventBus.addEventListener("ajax.post", postRequest);