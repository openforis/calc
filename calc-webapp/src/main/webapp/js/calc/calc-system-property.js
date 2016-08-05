/**
 * @author M. Togna
 */

SystemProperty = function() {

};

SystemProperty.prototype.loadAll = function(success) {
	var params = {
		url 	: 'rest/calc/system-properties.json',
		success : success
	}

	EventBus.dispatch("ajax", null, params);
};

SystemProperty.prototype.check = function(callback) {
	UI.lock();
	
	var success = function(properties){

		var editProperties = false;
		$.each(properties, function(i, obj) {
			if (!obj.value) {
				editProperties = true;
			}
		});

		if (editProperties) {
			Calc.scrollToSection($("#settings"));
			setTimeout(function() {
				Calc.navigateToSection('system-property.html');
				setTimeout(function(){
					Calc.backHomeBtn.stop().hide(0);
				}, 550);
			}, 700);

		}
	
	};
	
	this.loadAll( success );

};

SystemProperty.prototype.show = function() {
	UI.lock();
	
	var form 		= $("#system-property").find('form');
	var formSection = form.find('.form-section');
	formSection.empty();
	
	this.loadAll( function(properties){
		$.each(properties, function(i, obj) {
			addFormItem(obj, formSection);
		});
		UI.unlock();
	} );
	
	
	form.submit(function(e){
		e.preventDefault();
		UI.lock();
		
		var params = {
				url		: 'rest/calc/system-properties/save.json',
				type 	: 'POST',
				data	: form.serialize(),	
				success	: function(response) {
//					console.log(response);
					UI.Form.updateErrors(form, response.errors);
					
					if(response.status == "ERROR" ) {
			    		UI.showError("There are errors in the form. Please fix them before proceeding.", true);
			    	} else {
			    		UI.showSuccess("System properties saved", true);
			    		Calc.backHomeBtn.stop().show();
			    	}
					
					UI.unlock();
					
				}
		}
		EventBus.dispatch("ajax", null, params);
		
	});
};

var _systemProperties;
SystemProperty.getInstance = function() {
	if (!_systemProperties) {
		_systemProperties = new SystemProperty();
	}
	return _systemProperties;
};




// utility methods

var addFormItem = function(property , formSection ){

	var group = $('<div class="form-group"/>')
	formSection.append( group );
	
	var label = getFormItemLabel(property);
	group.append( label );
	
	var input = getFormItemInput(property);
	group.append( input );
	
};

var getFormItemLabel = function(property){
	var propertyLabels = {
			'r_exec_dir' : 'R executable directory'
	};
	
	var label = $('<label class="col-md-5 control-label"></label>');
	label.append( propertyLabels[property.name] );
	
	return label;
};

var getFormItemInput = function(property){
	var propertyPlaceholder = {
			'r_exec_dir' : 'e.g.  C:\\Program Files\\R\\R-3.3.1\\bin'
	}
	
	
	var div = $('<div class="col-md-7"/>');
	
	var input = $('<input type="text" class="form-control"/>');
	input.attr( 'name', property.name );
	input.attr( 'placeholder' , propertyPlaceholder[property.name] );
	input.val(property.value);
	
	div.append( input );
	return div;
};