/**
 * 
 * R script component
 * Extends Calc Autocomplete component 
 * 
 * It manages the interactions with a content assist dialog, searching in all R base library functions
 * 
 * @author S. Ricci
 */
function RScript($inputField) {
	
	this.super.apply(this, arguments);
	
	//cache the list of R functions
	this.rFunctions = null;
	
	//used to get the list of variables; set by outside
	this.entity = null;
}

RScript.prototype = (function() {
	
	/**
	* Overrides superclass method
	*/ 
	var init = function(callback) {
		var $this = this;
		$.proxy(superInit, $this)(arguments);
		
		//init R functions cache
		UI.disable($this.$inputField);
		$.proxy(loadRFunctions, $this)(function(response) {
			$this.rFunctions = response;
			UI.enable($this.$inputField);
		});
	};
	
	/**
	 * Overrides superclass method
	 */
	var calculateQuery = function() {
		var $this = this;
		var script = $this.$inputField.val();
		var caret = $this.$inputField.caret();
		var search = StringUtils.getLastWord(script, [" ", "\n"], caret);
		return search;
	};
	
	/**
	 * Overrides superclass method
	 */
	var lookupResultGroups = function() {
		var $this = this;
		var groups = new Array();
		var variables = $.proxy(filterVariables, $this)();
		if ( variables.length > 0 ) {
			groups.push(variables);
		}
		var functions = $.proxy(filterRFunctions, $this)();
		if ( functions.length > 0 ) {
			groups.push(functions);
		}
		return groups;
	};
	
	/**
	 * Overrides superclass method
	 */
	var itemSelectedHandler = function(item) {
		var $this = this;
		$.proxy(addItemToScript, $this)(item);
	};
	
	/**
	 * Filters the R functions and returns only the ones 
	 * starting with the query string
	 */
	var filterRFunctions = function() {
		var $this = this;
		
		var filtered = new Array();
		
		var totalItems = new Array();
		totalItems.push($this.entity.name);
		totalItems = totalItems.concat($this.rFunctions);
		
		$.each(totalItems, function(index, funct) {
			if ( StringUtils.startsWith(funct, $this.query) ) {
				filtered.push(funct);
			}
		});
		return filtered;
	};
	
	/**
	 * Filters the entity variables and 
	 * returns the ones starting with the query string
	 */
	var filterVariables = function() {
		var $this = this;
		var result = new Array();
		if ( $this.entity != null ) {
			var variables = $this.entity.hierarchyVariables(); 
			$.each(variables, function(index, variable) {
				var variableName = variable.name;
				var variableItem = $this.entity.name + "$" + variableName;
				if ( StringUtils.startsWith(variableName, $this.query) ||
						StringUtils.startsWith(variableItem, $this.query)) {
					result.push(variableItem);
				}
			});
		}
		result.sort();
		return result;
	};
	
	/**
	 * Adds the specified function to the text in the input field
	 */
	var addItemToScript = function(item) {
		var $this = this;
		var $field = $this.$inputField;
		var oldText = $field.val();
		var caret = $field.caret();
		var queryLength = $this.query.length;
		var textToInsertStartIndex = caret - queryLength;
		var textToInsert = item + " ";
		if (textToInsertStartIndex > 0 && queryLength == 0 ) {
			textToInsert = " " + textToInsert;
		}
		var newText = StringUtils.replaceText(oldText, textToInsert, textToInsertStartIndex, caret);
		$field.val(newText);
		$field.caret(textToInsertStartIndex + textToInsert.length);
	};
	
	/**
	 * Loads the R base library functions from the server
	 */
	var loadRFunctions = function(callback) {
		$.ajax({
			url:"rest/r/functions.json",
			dataType:"json"
		})
		.done(function(response) {
			if ( callback ) {
				callback(response);
			}
		})
		.error( function() {
			Calc.error.apply( this , arguments );
		});
	};
	
	//prototype extends Autocomplete prototype
	var parent = new Autocomplete();
	var proto = parent;
	proto.super = parent.constructor;
	
	proto.constructor = RScript;
	
	//extend super class methods
	var superInit = proto._init;
	proto._init = init;
	
	proto.calculateQuery = calculateQuery;
	
	proto.lookupResultGroups = lookupResultGroups;
	
	proto.itemSelectedHandler = itemSelectedHandler;
	
	return proto;
})();