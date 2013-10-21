var CalcForm = {
	
	getFieldLabel: function($field) {
		$formGroup = $field.closest('.form-group');
		$labelEl = $formGroup.find('.control-label');
		return $labelEl == null ? "": $labelEl.text();
	},
	removeErrors: function ($form) {
		$form.find('.form-group').removeClass('has-error');
		$form.find('.form-control').tooltip('destroy');
	},
	updateErrors: function($form, errors) {
		CalcForm.removeErrors($form);
		
		$.each(errors, function(i, error) {
			var fieldName = error.field;
			var $field = $form.find('[name=' + fieldName + ']');
			if ( $field != null ) {
				CalcForm._createErrorTooltip($field, error);
			}
		});
	},
	showResultMessage: function(message, isSuccess){
		// Add div if not defined
		var feedbackDiv = $("#messageContainer");
		
		if( isSuccess == true){
			feedbackDiv.addClass("alert-success");		
			feedbackDiv.removeClass("alert-danger");
		}else{
			feedbackDiv.addClass("alert-danger");		
			feedbackDiv.removeClass("alert-success");
		}
		
		feedbackDiv.find("span").html( message );
		
		// fade out
		feedbackDiv.fadeIn( 400 ).delay( 2000 ).fadeOut( 800 );
	},	
	disableForm: function(form){
		form.find('input, textarea, button, select').prop('disabled', true);
	},
	enableForm: function(form){
		form.find('input, textarea, button, select').prop('disabled',false);
	},
	_createErrorTooltip: function($field, error) {
		$formGroup = $field.closest('.form-group');
		$formGroup.addClass('has-error');
		
		var fieldLabel = CalcForm.getFieldLabel($field);
		var message = fieldLabel + " " + error.defaultMessage;
		
		$field.tooltip({
			title: message,
			container: 'body',
			template: '<div class="tooltip error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
		});
	},
	setFieldValues: function($form, $data) {
		$.each($data, function(fieldName, value) {
			var $inputFields = $('[name='+fieldName+']', $form);
			if ( $inputFields.length == 1 ) {
				var inputFieldEl = $inputFields[0];
				switch(CalcForm.getType(inputFieldEl)) {
					case "hidden":  
					case "text" :   
					case "textarea":  
						inputFieldEl.value = value;   
						break;
				}
			} else {
				$inputFields.each(function(i, $inputField) {
					switch(CalcForm.getType($inputField)) {
						case "radio" : 
						case "checkbox":
							var checked = $(this).attr('value') == value;
							$(this).attr("checked", checked); 
							break;  
					}
				});
			}
	    });
	},
	getType: function(inputField) {
		var type = inputField.type;
		if ( ! type ) {
			//e.g. textarea element
			type = inputField.nodeName.toLowerCase();
		}
		return type;
	},
	reset: function($form) {
		//TODO support other field types: select, radio buttons, checkboxes...
		$form.find("input[type=text], textarea").val("");
		CalcForm.removeErrors($form);
	}
};