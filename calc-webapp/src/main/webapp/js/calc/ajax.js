/**
 * @author M. Togna
 */

$.ajaxSetup({
	cache : false,
	type : 'GET',
	dataType : 'json'
});

var ajaxRequest = function(evt, params) {
	$.ajax(params);
}

EventBus.addEventListener("ajax", ajaxRequest);