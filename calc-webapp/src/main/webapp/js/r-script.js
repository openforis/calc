/**
 * 
 * R script component
 * It can be associated to a textarea or to an input with type "text".
 * It manages the interactions with a content assist dialog, searching in all R base library functions
 * 
 */
function RScript($inputField) {
	this.$inputField = $inputField;
	
	//dropdown component (to be initialized)
	this.$dropdown = null;
	
	//transient variable, stores the last search word used for script content assist dialog
	this.lastSearch = null;
	
	//cache the list of R functions
	this.rFunctions = null;
	
	this.dropdownOpen = false;
	
	//used to get the list of variables; set by outside
	this.selectedEntity = null;
	
	this._init();
}

RScript.prototype = (function() {
	
	/**
	* Initializes the component and the event listeners
	*/ 
	var init = function(callback) {
		var $this = this;
		
		//init R functions cache
		UI.disable($this.$inputField);
		$.proxy(loadRFunctions, $this)(function(response) {
			$this.rFunctions = response;
			UI.enable($this.$inputField);
		});
		
		$.proxy(initDropdown, $this)();
		
		$this.$inputField.keydown(function(event) {
			$.proxy(inputKeyDownHandler, $this)(event);
		});
		$this.$inputField.keyup(function(event) {
			$.proxy(inputKeyUpHandler, $this)(event);
		});
		
	};
	
	/**
	* Initializes the dropdown component
	*/
	var initDropdown = function() {
		var $this = this;
		
		$this.$dropdown = $(
				'<div class="dropdown context-dropdown">' + 
					'<a class="dropdown-toggle" role="button" data-toggle="dropdown" data-target="#"></a>' +
					'<ul class="dropdown-menu context-menu scroll-menu" role="menu"></ul>' +
				'</div>');
		$this.$inputField.parent().append($this.$dropdown);

		var $toggle = $this.$dropdown.find('.dropdown-toggle');
		$toggle.dropdown();

		//set focus on input field when the dropdown is closed
		$this.$dropdown.on('hide.bs.dropdown', function () {
			$.proxy(dropdownCloseHandler, $this)();
		});
	};
	
	/**
	* Input field key down handler
	* 
	* - opens the dropdown when CTRL + SPACE is pressed
	* - set focus on the first dropdown item when ARROW-DOWN is pressed
	* - closes the dropdown when non numeric character is pressed
	*/ 
	var inputKeyDownHandler = function(event) {
		var $this = this;
		if ( event.ctrlKey && event.keyCode == 32) { //ctrl + spacebar pressed
			$.proxy(askForAutocomplete, $this)();
		} else if ( $this.dropdownOpen ) {
			if ( event.keyCode == 40 ) { //arrow down pressed
				$.proxy(focusOnFirstDropdownItem, $this)();
			} else if (! (
					$.proxy(isFunctionNameCharacter, $this)(event.keyCode) ||
						event.keyCode == 8 //backspace 
					)) {
				$.proxy(closeDropdown, $this)();
			}
		}
	};
	
	/**
	* Input field key up handler
	* Updates the dropdown when it's opened
	*/
	var inputKeyUpHandler = function(event) {
		var $this = this;
		var keyCode = event.keyCode;
		if ( $this.dropdownOpen && 
				($.proxy(isFunctionNameCharacter, $this)(keyCode) ||
					keyCode == 8 //backspace
				)) {	
			$.proxy(askForAutocomplete, $this)();
		}
	};
	
	/**
	* Returns true if the specified keyCode corresponds to: 
	* - alphanumeric character
	* - dot symbol (.)
	* - dollar symbol ($)
	*/
	var isFunctionNameCharacter = function(keyCode) {
		return keyCode >= 48 && keyCode <= 90 || //alphanumeric (0-9a-z)
			 keyCode == 190 || // . (dot)
			 keyCode == 36 // $ (dollar);
	}
	
	/**
	* Searches for functions according to the last word in the input field and
	* opens the dropdown if functions are found
	* otherwise closes the dropdown (if opened)
	*/
	var askForAutocomplete = function() {
		var $this = this;
		
		var script = $this.$inputField.val();
		var caret = $this.$inputField.caret();
		var search = StringUtils.getLastWord(script, ' ', caret);
		$this.lastSearch = search;

		var variables = $.proxy(filterVariables, $this)();
		var functions = $.proxy(filterRFunctions, $this)();
		
		if ( variables.length > 0 || functions.length > 0 ) {
			//TODO if unique result found, add it to the script ??
			//show dropdown
			$.proxy(populateDropdown, $this)(variables, functions);
			$.proxy(showDropdown, $this)();
		} else if ($this.dropdownOpen ) {
			$.proxy(closeDropdown, $this)();
		}
	};
	
	/**
	 * Filters the R functions and returns only the ones 
	 * starting with the specified value
	 */
	var filterRFunctions = function() {
		var $this = this;
		var startsWith = $this.lastSearch;
		var result = new Array();
		$.each($this.rFunctions, function(index, funct) {
			if ( StringUtils.startsWith(funct, startsWith) ) {
				result.push(funct);
			}
		});
		return result;
	};
	
	var filterVariables = function() {
		var $this = this;
		var startsWith = $this.lastSearch;
		var result = new Array();
		if ( $this.selectedEntity != null ) {
			var variables = $this.selectedEntity.getAncestorsVariables(); 
			$.each(variables, function(index, variable) {
				var variableName = variable.name;
				if ( StringUtils.startsWith(variableName, startsWith) ||
						StringUtils.startsWith("$" + variableName, startsWith)) {
					result.push("$" + variableName + "$");
				}
			});
		}
		result.sort();
		return result;
	};
	
	/**
	 * Populates the dropdown with the specified list of string values
	 */
	var populateDropdown = function(variables, functions) {
		var $this = this;
		var $menu = $this.$dropdown.find('.dropdown-menu');
		$menu.empty();
		var itemTemplate = '<li class="dropdown"><a role="menuitem" tabindex="-1" href="#"></a></li>';
		var dividerTemplate = '<li role="presentation" class="divider"></li>';
		var items = variables.concat(functions);
		$.each(items, function(index, item) {
			if ( index > 0 && index == variables.length ) {
				var separatorEl = $(dividerTemplate);
				$menu.append(separatorEl);
			}
			var itemEl = $(itemTemplate);
			var anchor = itemEl.find('a');
			anchor.text(item);
			anchor.click(function(e) {
				$.proxy(dialogItemClickHandler, $this)(e);
			});
			$menu.append(itemEl);
		});
	};
	
	/**
	 * Opens the dropdown and aligns it to the caret position in the input field
	 */
	var showDropdown = function() {
		var $this = this;
		
		var caretPos = $this.$inputField.caretpixelpos();
		var dialogPosLeft = caretPos.left;
		var dialogPosTop = caretPos.top - 10;

		$this.$dropdown.css("left", dialogPosLeft);
		$this.$dropdown.css("top", dialogPosTop);
		
		if ( ! $this.dropdownOpen ) {
			var $toggle = $this.$dropdown.find('.dropdown-toggle');
			$toggle.click();
			$this.dropdownOpen = true;
		}
	};
	
	/**
	 * Closes the dropdown
	 */
	var closeDropdown = function() {
		var $this = this;
		var $toggle = $this.$dropdown.find('.dropdown-toggle');
		$toggle.click();
	};
	
	/**
	 * Sets the focus on the first item in the dropdown
	 */
	var focusOnFirstDropdownItem = function() {
		var $this = this;
		var $menu = $this.$dropdown.find('.dropdown-menu');
		//set focus on first item
		$menu.find('a:first').focus();
	};
	
	/**
	 * Called when the dropdown is closed
	 */
	var dropdownCloseHandler = function() {
		var $this = this;
		$this.$inputField.focus();
		$this.dropdownOpen = false;
	};
	
	/**
	 * Manages the click on an item in the dropdown
	 */
	var dialogItemClickHandler = function(e) {
		var $this = this;
		var anchor = e.currentTarget;
		var funct = anchor.text;
		$.proxy(addFunctionToScript, $this)(funct);
	};
	
	/**
	 * Adds the specified function to the text in the input field
	 */
	var addFunctionToScript = function(funct) {
		var $this = this;
		var $field = $this.$inputField;
		var oldText = $field.val();
		var caret = $field.caret();
		var lastSearchLength = $this.lastSearch.length;
		var textToInsertStartIndex = caret - lastSearchLength;
		var textToInsert = funct + " ";
		if (textToInsertStartIndex > 0 && lastSearchLength == 0 ) {
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
		});
	};
	
	//prototype
	return {
		constructor : RScript,
		
		//public methods
		_init : init
	};
})();