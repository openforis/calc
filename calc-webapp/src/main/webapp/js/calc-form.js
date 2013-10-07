var CalcForm = {
	
	getFieldLabel: function($field) {
		$formGroup = $field.closest('.form-group');
		$labelEl = $formGroup.find('.control-label');
		return $labelEl == null ? "": $labelEl.text();
	},
	removeErrors: function (form) {
		form.find('.form-group').removeClass('has-error');
	},
	updateErrors: function(form, errors) {
		CalcForm.removeErrors(form);
		
		$.each(errors, function(i, error) {
			var fieldName = error.field;
			var $field = form.find('[name=' + fieldName + ']');
			if ( $field != null ) {
				$formGroup = $field.closest('.form-group');
				$formGroup.addClass('has-error');
				
				var fieldLabel = CalcForm.getFieldLabel($field);
				var message = fieldLabel + " " + error.defaultMessage;
				
				$field.tooltip({
					title: message,
					container: 'body'
				});
			}
		});
	}
	

};