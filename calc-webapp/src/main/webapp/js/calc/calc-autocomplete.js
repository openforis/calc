/**
 * Basic autocomplete dropdown menu.
 * 
 * It can be associated to a textarea or to an input with type "text".
 * 
 * Only public methods will be visible to subclasses.
 * 
 */
function Autocomplete($inputField) {
	this.$inputField = $inputField;
	
	//dropdown menu component (to be initialized)
	this.$menu = null;
	
	//transient variable, stores the last search word used for script content assist dialog
	this.query = null;
	
	this.dropdownOpen = false;
	this.mouseOverItem = false;

	if (this.$inputField ) {
		this._init();
	}
}

Autocomplete.prototype = (function() {
	/**
	* Initializes the component and the event listeners
	*/ 
	var init = function(callback) {
		var $this = this;
		
		$.proxy(initDropdown, $this)();
		
		$this.$inputField
			.keydown($.proxy(inputKeyDownHandler, $this))
			.keyup($.proxy(inputKeyUpHandler, $this))
			.blur($.proxy(inputBlurHandler, $this))
			.click($.proxy(inputClickHandler, $this))
		;
	};
	
	/**
	* Initializes the dropdown component
	*/
	var initDropdown = function() {
		var $this = this;
		
		$this.$menu = $('<ul class="dropdown-menu context-menu scroll-menu" role="menu"></ul>');
		$this.$menu.appendTo('body');
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
			switch ( event.keyCode ) {
			case 16: //shift key
			case 20: //caps lock
				//do nothing
				break;
			case 9: //tab key pressed
				event.preventDefault();
				event.stopPropagation();
				$.proxy(activateSiblingItem, $this)();
				break;
			case 13: //enter key pressed
				event.preventDefault();
				event.stopPropagation();
				$.proxy(selectActiveItem, $this)();
				break;
			case 38: //arrow up pressed
				event.preventDefault();
				event.stopPropagation();
				$.proxy(activateSiblingItem, $this)(true);
				break;
			case 40: //arrow down pressed
				event.preventDefault();
				event.stopPropagation();
				$.proxy(activateSiblingItem, $this)();
				break;
			default:
				if (! (
					$.proxy(isQueryCharacter, $this)(event.keyCode) ||
						event.keyCode == 8 //backspace 
					)) {
					$.proxy(closeDropdown, $this)();
				}
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
				($.proxy(isQueryCharacter, $this)(keyCode) ||
					keyCode == 8 //backspace
				)) {	
			$.proxy(askForAutocomplete, $this)();
		}
	};
	
	var inputBlurHandler = function(event) {
		var $this = this;
		if ( ! $this.mouseOverItem && $this.dropdownOpen) {
			setTimeout(function () {
				$.proxy(closeDropdown, $this)();
			}, 200);
		}
	};
	
	var inputClickHandler = function(event) {
		$.proxy(inputBlurHandler, this)(event);
	};
	
	var itemMouseEnterHandler = function(e) {
		this.mouseOverItem = true;
		this.$menu.find('.active').removeClass('active');
		$(e.currentTarget).addClass('active');
	};

	var itemMouseLeaveHandler = function(e) {
		this.mouseOverItem = false;
	};
	
    /**
	 * Selects the next (or previous) item in the autocomplete dropdown,
	 * relative to the selected one.
	 * 
	 * @param previous
	 *            If true, select the previous sibling item, otherwise selects
	 *            the next one
	 * 
	 */
	var activateSiblingItem = function(previous) {
		var $this = this;
		var menu = $this.$menu;
		var active = menu.find('.active').removeClass('active');
		var newActive = previous ? active.prev() : active.next();

		if (newActive.length == 0) {
			newActive = $(menu.find('li')[0]);
		}
		newActive.addClass('active');
	};

	var selectActiveItem = function() {
		var $this = this;
		var active = $this.$menu.find('.active');
		if ( active.length > 0 ) {
			var anchor = active.find('a');
			anchor.click();
		}
	};
	
	/**
	 * Returns true if the specified keyCode corresponds to: - alphanumeric
	 * character - dot symbol (.) - dollar symbol ($)
	 */
	var isQueryCharacter = function(keyCode) {
		return keyCode >= 48 && keyCode <= 90 || //alphanumeric (0-9a-z)
			 keyCode == 190 || // . (dot)
			 keyCode == 36 // $ (dollar);
	};
	
	/**
	* Searches for functions according to the last word in the input field and
	* opens the dropdown if functions are found
	* otherwise closes the dropdown (if opened)
	*/
	var askForAutocomplete = function() {
		var $this = this;
		
		$this.query = $this.calculateQuery();

		var resultGroups = $this.lookupResultGroups();
		
		if ( resultGroups.length > 0 ) {
			//TODO if unique result found, add it to the script ??
			//show dropdown
			$.proxy(populateDropdown, $this)(resultGroups);
			$.proxy(showDropdown, $this)();
			$.proxy(activateSiblingItem, $this)();
		} else if ($this.dropdownOpen ) {
			$.proxy(closeDropdown, $this)();
		}
	};
	
	/**
	 * 
	 * Extracts the query val from the input field
	 * 
	 */
	var calculateQuery = function() {
		return this.$inputField.val();
	};

	/**
	 * Searches for results.
	 * 
	 * @return Array of groups of items 
	 */
	var lookupResultGroups = function() {
		return [];
	};
	
	/**
	 * Populates the dropdown with the specified list of string values
	 * @param itemGroups is an array of arrays. Each group of items will be separated by a separator element 
	 */
	var populateDropdown = function(itemGroups) {
		var $this = this;
		$this.$menu.empty();
		var itemTemplate = '<li class="dropdown"><a role="menuitem" tabindex="-1" href="#"></a></li>';
		var dividerTemplate = '<li role="presentation" class="divider"></li>';
		$.each(itemGroups, function(groupIndex, group) {
			if ( groupIndex > 0 && $this.$menu.size() > 0 ) {
				var separatorEl = $(dividerTemplate);
				$this.$menu.append(separatorEl);
			}
			$.each(group, function(index, item) {
				var itemEl = $(itemTemplate);
				var anchor = itemEl.find('a');
				var itemHtml = $.proxy(highlightMatch, $this)(item);
				anchor.html(itemHtml);
				
				//event handlers
				anchor.click(function(e) {
					$this.itemSelectedHandler(item);
					$.proxy(closeDropdown, $this)();
				});
				
				itemEl
					.mouseenter($.proxy(itemMouseEnterHandler, $this))
					.mouseleave($.proxy(itemMouseLeaveHandler, $this));
				
				$this.$menu.append(itemEl);
			});
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
		
		$this.$menu.css("left", dialogPosLeft);
		$this.$menu.css("top", dialogPosTop);
		
		if ( ! $this.dropdownOpen ) {
			$this.$menu.show();
			$this.dropdownOpen = true;
		}
	};
	
	/**
	 * Closes the dropdown
	 */
	var closeDropdown = function() {
		var $this = this;
		$this.$menu.hide();
		$this.dropdownOpen = false;
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
	var itemSelectedHandler = function(item) {
	};

	/**
	 * Highlights the part of an item matching the query string
	 * by wrapping the matching part with a "strong" tag.
	 */
	var highlightMatch = function(item) {
		var query = this.query;
		if ( StringUtils.startsWith(item, query ) ) {
			var lastPart = item.substring(query.length);
			var result = '<strong>' + query + '</strong>' + StringUtils.encodeHtml(lastPart);
			return result;
		} else {
			return StringUtils.encodeHtml(item);
		}
	};

	// prototype
	return {
		constructor : Autocomplete,
		
		//public methods
		_init : init
		,
		calculateQuery : calculateQuery
		,
		lookupResultGroups : lookupResultGroups
		,
		itemSelectedHandler : itemSelectedHandler
	};
	
})();
	

