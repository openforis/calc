var CalcForm = {
	
	getFieldLabel: function($field) {
		$formGroup = $field.closest('.form-group');
		$labelEl = $formGroup.find('.control-label');
		return $labelEl == null ? "": $labelEl.text();
	},
	removeErrors: function (form) {
		form.find('.form-group').removeClass('has-error');
		form.find('.form-control').tooltip('destroy');
	},
	updateErrors: function(form, errors) {
		CalcForm.removeErrors(form);
		
		$.each(errors, function(i, error) {
			var fieldName = error.field;
			var $field = form.find('[name=' + fieldName + ']');
			if ( $field != null ) {
				CalcForm._createErrorTooltip($field, error);
			}
		});
	},
	_createErrorTooltip: function($field, error) {
		$formGroup = $field.closest('.form-group');
		$formGroup.addClass('has-error');
		
		var fieldLabel = CalcForm.getFieldLabel($field);
		var message = fieldLabel + " " + error.defaultMessage;
		
		$field.tooltip({
			title: message,
			container: 'body'
		});
	}
	

};