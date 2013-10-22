var UI = {
	
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
		UI.removeErrors($form);
		
		$.each(errors, function(i, error) {
			var fieldName = error.field;
			var $field = $form.find('[name=' + fieldName + ']');
			if ( $field != null ) {
				UI._createErrorTooltip($field, error);
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
		
		var fieldLabel = UI.getFieldLabel($field);
		var message = fieldLabel + " " + error.defaultMessage;
		
		var $parentModal = $field.closest('.modal');
		var container = $parentModal.length == 0 ? 'body': $parentModal; 
		
		$field.tooltip({
			title: message,
			container: container,
			template: '<div class="tooltip error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
		});
	},
	setFieldValues: function($form, $data) {
		$.each($data, function(fieldName, value) {
			var $inputFields = $('[name='+fieldName+']', $form);
			if ( $inputFields.length == 1 ) {
				var inputFieldEl = $inputFields[0];
				switch(UI.getType(inputFieldEl)) {
					case "hidden":  
					case "text" :   
					case "textarea":  
						inputFieldEl.value = value;   
						break;
				}
			} else {
				$inputFields.each(function(i, $inputField) {
					switch(UI.getType($inputField)) {
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
		UI.removeErrors($form);
	}
};