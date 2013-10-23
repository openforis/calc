var UI = {
	
	Form: {
		/**
		 * Returns the label associated to the specified field 
		 * 
		 * @param $field
		 * @returns
		 */
		getFieldLabel: function($field) {
			$formGroup = $field.closest('.form-group');
			$labelEl = $formGroup.find('.control-label');
			return $labelEl == null ? "": $labelEl.text();
		},
		/**
		 * Remove all validation errors shown in the form
		 * 
		 * @param $form
		 */
		removeErrors: function ($form) {
			$form.find('.form-group').removeClass('has-error');
			$form.find('.form-control').tooltip('destroy');
		},
		/**
		 * Update the validation errors in the form
		 * 
		 * @param $form
		 * @param errors
		 */
		updateErrors: function($form, errors) {
			UI.Form.removeErrors($form);
			
			$.each(errors, function(i, error) {
				var fieldName = error.field;
				var $field = $form.find('[name=' + fieldName + ']');
				if ( $field != null ) {
					UI.Form._createErrorTooltip($field, error);
				}
			});
		},
		/**
		 * Show a success or failure message in a popup window
		 * 
		 * @param message
		 * @param isSuccess
		 */
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
		/**
		 * Disable all input fields in a form
		 * 
		 * @param form
		 */
		disable: function(form){
			form.find('input, textarea, button, select').prop('disabled', true);
		},
		/**
		 * Enable all input fields in a form
		 * 
		 * @param form
		 */
		enable: function(form){
			form.find('input, textarea, button, select').prop('disabled',false);
		},
		/**
		 * Private function: create an error tooltip associated to a validation error
		 * 
		 * @param $field
		 * @param error
		 */
		_createErrorTooltip: function($field, error) {
			$formGroup = $field.closest('.form-group');
			$formGroup.addClass('has-error');
			
			var fieldLabel = UI.Form.getFieldLabel($field);
			var message = fieldLabel + " " + error.defaultMessage;
			
			var $parentModal = $field.closest('.modal');
			var container = $parentModal.length == 0 ? 'body': $parentModal; 
			
			$field.tooltip({
				title: message,
				container: container,
				template: '<div class="tooltip error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
			});
		},
		/**
		 * Set the specified values into a form according to the field names
		 * 
		 * @param $form
		 * @param $data
		 */
		setFieldValues: function($form, $data) {
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
		},
		/**
		 * Returns the input type of a field.
		 * If the field is not a "input" element, then returns the node name of the element.
		 *  
		 * @param inputField
		 * @returns
		 */
		getInputType: function(inputField) {
			var type = inputField.type;
			if ( ! type ) {
				//e.g. textarea element
				type = inputField.nodeName.toLowerCase();
			}
			return type;
		},
		/**
		 * Sets all empty values in every input field inside the form
		 * 
		 * @param $form
		 */
		reset: function($form) {
			//TODO support other field types: select, radio buttons, checkboxes...
			$form.find("input[type=text], textarea").val("");
			UI.Form.removeErrors($form);
		},
		/**
		 * Populate a select using a ajax call to a rest url that returns a list of json items
		 * 
		 * @param $select
		 * @param sourceUrl
		 * @param valueFieldName
		 * @param labelFieldName
		 * @param callback
		 */
		populateSelect: function($select, items, valueFieldName, labelFieldName) {
			$select.empty();
			$select.attr("disabled", "disabled");

			$.each(items, function(i, item) {
				var value = item[valueFieldName];
				var label = item[labelFieldName];
				$select.append($("<option />").val(value).text(label));
			});
			$select.removeAttr("disabled");
			$select.val([]);
		}
	},
	/**
	 * Shows a modal with static backdrop to avoid user interaction
	 */
	lock: function() {
		$uiLockModal = $("#ui-lock-modal");
		$uiLockModal.modal({keyboard:false, backdrop:"static"});
		$('body').addClass('locked');
	},
	/**
	 * Remove the lock modal
	 */
	unlock: function() {
		$uiLockModal.modal('hide');
		$uiLockModal.modal('removeBackdrop');
		$('body').removeClass('locked');
	},
	
	//Reset progress bar to its original state
	resetProgressBar : function($progressBar) {
		$progressBar.removeClass();
		$progressBar.addClass("progress-bar");
		$progressBar.parent().removeClass();
		$progressBar.parent().addClass("progress");
	}
	
};