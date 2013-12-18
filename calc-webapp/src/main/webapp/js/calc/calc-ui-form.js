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
			if ( ! $formGroup.hasClass('has-error') ) {
				$formGroup.addClass('has-error');
	
				UI.Form.createErrorTooltip($field, error);
			}
		}
	});
};

/**
 * Returns the message associated to the error with the specified field name.
 * If field name is not specified, than the generic form error message is returned.
 */
UI.Form.getFieldErrorMessage = function(errors, field) {
	for (var i=0; i < errors.length; i++) {
		var error = errors[i];
		if ( ! field && ! error.field || field == error.field) {
			return error.defaultMessage;
		}
	}
	return null;
};

/**
 * Returns the error message associated to the error with no field name specified
 * or the one associated to the first error.
 */
UI.Form.getFormErrorMessage = function($form, errors) {
	var genericErrorMessage = UI.Form.getFieldErrorMessage(errors);
	if ( genericErrorMessage != null ) {
		errorMessage = genericErrorMessage;
	} else {
		var firstError = errors[0];
		var fieldName = firstError.field;
		var fieldErrorMessage = firstError.defaultMessage;
		var field = $form.find("[name='" + fieldName + "']");
		var fieldLabel = UI.Form.getFieldLabel(field);
		errorMessage =  fieldLabel + " " + fieldErrorMessage;
	}
	return errorMessage;
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
 * @param fieldLabel (optional) if not specified, field label will be assigned using getFieldLabel function
 */
UI.Form.createErrorTooltip = function($field, error, fieldLabel) {
	if ( ! fieldLabel ) {
		fieldLabel = UI.Form.getFieldLabel($field);
	}
	var errorMessage = typeof error == "string" ? error: error.hasOwnProperty("defaultMessage") ? error.defaultMessage: "error";
	var message = fieldLabel + " " + errorMessage;
	
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
 * Option value is set according to the specified valueKey and 
 * option text content is set according to the specified labelKey
 * 
 * @param $select
 * @param items
 * @param valueKey (optional, default value will be item.toString())
 * @param labelKey (optional, default is valueKey, if specified)
 * @param callback
 */
UI.Form.populateSelect = function($select, items, valueKey, labelKey) {
	$select.empty();
	$select.attr("disabled", "disabled");

	$.each(items, function(i, item) {
		var value = item.hasOwnProperty(valueKey) ? item[valueKey]: item;
		var label = item.hasOwnProperty(labelKey) ? item[labelKey]: value;
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

/**
 * Form client side validation functions
 */
UI.Form.validation = function() {};

/**
 * Returns true if the field is not empty, otherwise returns false and adds error feedback to the field
 */
UI.Form.validation.required = function($field, label) {
	var $formGroup = $field.closest(".form-group");
	$formGroup.removeClass('has-error');
	var val = $field.val();
	if ( val.trim().length == 0 ) {
		$formGroup.addClass('has-error');
		UI.Form.createErrorTooltip($field, "is required", label);
		return false;
	} else {
		return true;
	}
};

/**
 * Returns true if the field value is a valid number, otherwise returns false and adds error feedback to the field
 */
UI.Form.validation.numeric = function($field, label) {
	var $formGroup = $field.closest(".form-group");
	$formGroup.removeClass('has-error');
	var val = $field.val();
	if ( isNaN(val) ) {
		$formGroup.addClass('has-error');
		UI.Form.createErrorTooltip($field, "Invalid number");
		return false;
	} else {
		return true;
	}
};

/**
 * Returns true if the field value is greater than the specified value, otherwise returns false and adds error feedback to the field
 */
UI.Form.validation.greaterThan = function($field, label, value) {
	var $formGroup = $field.closest(".form-group");
	$formGroup.removeClass('has-error');
	var val = $field.val();
	var number = Number(val);
	if ( number > value ) {
		return true;
	} else {
		$formGroup.addClass('has-error');
		UI.Form.createErrorTooltip($field, "Must be greather than " + value);
		return false;
	}
};