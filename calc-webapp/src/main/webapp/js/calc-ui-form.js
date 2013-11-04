UI.Form = function() {};

/**
 * Returns the label associated to the specified field 
 * 
 * @param $field
 * @returns
 */
UI.Form.getFieldLabel = function($field) {
	$formGroup = $field.closest('.form-group');
	$labelEl = $formGroup.find('.control-label');
	return $labelEl == null ? "": $labelEl.text();
};
/**
 * Remove all validation errors shown in the form
 * 
 * @param $form
 */
UI.Form.removeErrors = function ($form) {
	$form.find('.form-group').removeClass('has-error');
	$form.find('.form-control').tooltip('destroy');
};

/**
 * Update the validation errors in the form
 * 
 * @param $form
 * @param errors
 */
UI.Form.updateErrors = function($form, errors) {
	UI.Form.removeErrors($form);
	
	$.each(errors, function(i, error) {
		var fieldName = error.field;
		var $field = $form.find('[name=' + fieldName + ']');
		if ( $field != null ) {
			var $formGroup = $field.closest('.form-group');
			$formGroup.addClass('has-error');

			UI.Form.createErrorTooltip($field, error);
		}
	});
};

/**
 * DEPRECATED. Use UI.showError or UI.showSuccess
 * Left here for back compatibility
 * 
 * Show a success or failure message in a popup window
 * 
 * @param message
 * @param isSuccess
 *  
 */
UI.Form.showResultMessage = function(message, success){
	// Add div if not defined
	if( success == true ) {
		UI.showSuccess(message, true);
	} else {
		UI.showError(message, true);
	}
};	

/**
 * Disable all input fields in a form
 * 
 * @param form
 */
UI.Form.disable = function(form){
	UI.enable( form.find('input, textarea, button, select') );
//	.prop('disabled', true);
};

/**
 * Disable an input field
 */
UI.Form.disableField = function(field) {
	field.attr("disabled", "disabled");
};

/**
 * Enable an input field
 */
UI.Form.enableField = function(field) {
	field.removeAttr("disabled");
};

/**
 * Enable all input fields in a form
 * 
 * @param form
 */
UI.Form.enable = function(form){
	UI.disable( form.find('input, textarea, button, select') );
};

/**
 * Create an error tooltip associated to a validation error
 * 
 * @param $field
 * @param error
 */
UI.Form.createErrorTooltip = function($field, error) {
	var fieldLabel = UI.Form.getFieldLabel($field);
	var message = fieldLabel + " " + error.defaultMessage;
	
	var $parentModal = $field.closest('.modal');
	var container = $parentModal.length == 0 ? 'body': $parentModal; 
	
	var inputType = UI.Form.getInputType($field);
	var $targetField = inputType == 'hidden' ? $targetField = $field.siblings('.form-control'): $field;
	
	$targetField.tooltip({
		title: message,
		container: container,
		template: '<div class="tooltip error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
	});
};

/**
 * Set the specified values into a form according to the field names
 * 
 * @param $form
 * @param $data
 */
UI.Form.setFieldValues = function($form, $data) {
	$.each($data, function(fieldName, value) {
		var $inputFields = $('[name='+fieldName+']', $form);
		if ( $inputFields.length == 1 ) {
			var inputFieldEl = $inputFields[0];
			switch(UI.Form.getInputType(inputFieldEl)) {
				case "hidden":  
				case "text" :   
				case "textarea":  
					inputFieldEl.value = value;   
					break;
			}
		} else {
			$inputFields.each(function(i, $inputField) {
				switch(UI.Form.getInputType($inputField)) {
					case "radio" : 
					case "checkbox":
						var checked = $(this).attr('value') == value;
						$(this).attr("checked", checked); 
						break;  
				}
			});
		}
    });
};

/**
 * Returns the input type of a field.
 * If the field is not a "input" element, then returns the node name of the element.
 *  
 * @param inputField
 * @returns
 */
UI.Form.getInputType = function(inputField) {
	if ( inputField instanceof jQuery ) {
		if ( inputField.length == 1 ) {
			var field = inputField.get(0);
			return UI.Form.getInputType(field);
		} else {
			//no single input field found
			return null;
		}
	}
	var type = inputField.type;
	if ( ! type ) {
		//e.g. textarea element
		type = inputField.nodeName.toLowerCase();
	}
	return type;
};

/**
 * Sets all empty values in every input field inside the form
 * 
 * @param $form
 */
UI.Form.reset = function($form) {
	//TODO support other field types: select, radio buttons, checkboxes...
	$form.find("input[type=text], textarea").val("");
	UI.Form.removeErrors($form);
};

UI.Form.setValue = function(element,item){
	element.val(item);
};

/**
 * Populate a select using a list of items
 * Option value is set according to the specified valueFieldName and 
 * option text content is set according to the specified labelFieldName
 * 
 * @param $select
 * @param items
 * @param valueFieldName
 * @param labelFieldName
 * @param callback
 */
UI.Form.populateSelect = function($select, items, valueFieldName, labelFieldName) {
	$select.empty();
	$select.attr("disabled", "disabled");

	$.each(items, function(i, item) {
		var value = item[valueFieldName];
		var label = item[labelFieldName];
		$select.append($("<option />").val(value).text(label));
	});
	$select.removeAttr("disabled");
	$select.val([]);
};

/**
 * Sets the focus on the first input field in the specified form
 */
UI.Form.setFocus = function($form) {
	var $firstField = $form.find("*:input[type!=hidden]:first");
	$firstField.focus();
};

UI.Form.toJSON = function($form) {
	var array = $form.serializeArray();
	var result = {};
    jQuery.each(array, function() {
    	result[this.name] = this.value || '';
    });
    return result;
};