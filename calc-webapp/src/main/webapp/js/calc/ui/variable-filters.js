/**
 * Used to visualize the variable filter button in the data section
 *
 * @author Mino Togna
 */
VariableFilters = function( filters ) {
    this.filters = filters;
    
    this.template = $( "#variable-filter-popover" );
    
    this.container 	=  this.template.clone();
    this.container.attr( "id" ,"");
    $( 'body' ).append( this.container );
    
    this.uiConditions 	= this.container.find( ".conditions" );
    this.conditions 	= [];
    
    // buttons
    this.applyBtn 	= this.container.find( "button[name=apply]" );
    this.applyBtn.click( $.proxy( function(){
	this.apply();
    } , this ) );
    
    this.resetBtn 	= this.container.find( "button[name=reset]" );
    this.resetBtn.click( $.proxy( function(){
	this.reset();
    } , this ) );
    
    // current variable
    this.variable 	= null;
    // current filter button
    this.filterBtn 	= null;
    
    
    
    $(document).click( $.proxy( function() {
		if( this.container ) {
		    this.container.hide();
		}
    } , this ) );

	this.container.click(function(event) {
	    event.stopPropagation();
	});
    
};

VariableFilters.prototype.show = function( element ) {
	var $this = this;
    var variable = element.data( 'variable' );
    var container = element.data( 'container' );
    
    if( variable ) {
		this.variable = variable;
		this.filterBtn = element;
		
		var btnOffset = element.offset();
		this.container.css( "left" , ( btnOffset.left + 15 )  );
		this.container.fadeIn( 300 );
		this.updateConditions( );
		
		setTimeout(
			$.proxy( function(){
			    this.container.stop().animate( {top: btnOffset.top}, 100, "easeOutQuart" );
			    this.container.css( "opacity" , 0.97  );
			} , this)
			, 150 
		);
		
		// on container scroll, it hides the popup
		if( container ) {
			var scroll = function(e){
				e.stopPropagation();
				$this.container.fadeOut( 225 );
				container.off( "scroll" , scroll );
			};
			container.scroll( scroll );
		}
    }
};

VariableFilters.prototype.updateConditions = function( ) {
    this.uiConditions.hide();
    this.uiConditions.empty();
    
    var name 		= this.variable.name;
    var conditions 	= this.conditions[ name ];
    
    if( conditions && conditions.length > 0 ) {
    	// right now only 1 condition per variable is managed
    	this.condition = conditions[0];
    	this.condition.appendUIElements( this.uiConditions );
    } else {
    	if( this.variable.type === "CATEGORICAL" ) {
    		
    		var progressBar = $( '<div class="progress progress-striped active"><div class="progress-bar progress-bar-info width100"></div></div>' );
    		this.uiConditions.append( progressBar );
    		VariableManager.getInstance().getCategories( this.variable.id , $.proxy( function(categories) {
    			
    			progressBar.fadeOut();
				progressBar.remove();
    			
    			if( categories ) {
    				this.condition = new CategoricalVariableQueryCondition( categories );
    				$( this.condition ).data( "categories" , categories );
    			} else {
    				this.condition = new VariableQueryCondition();
    			}
    			
    			this.condition.appendUIElements( this.uiConditions );
    			
    		} , this ) );
    		
    	} else {
    		
    		// 	init conditions for the given variable
    		this.condition = new VariableQueryCondition();
    		this.condition.appendUIElements( this.uiConditions );
    	}
    	
    }
    
    this.uiConditions.fadeIn();
};


VariableFilters.prototype.apply = function() {
	var name = this.variable.name;
	// var condition = this.conditions[ name ] [0];

	if ( this.condition.validate(this.variable) ) {
		this.conditions[name] = [];
		this.conditions[name].push(this.condition);

		this.filterBtn.removeClass("filter-btn");
		this.filterBtn.addClass("filter-btn-selected");

		this.container.fadeOut(200);
	}
};

VariableFilters.prototype.reset = function() {
    var name 			= this.variable.name;
    for( var i in this.conditions ){
		if( i === name ){
		    delete this.conditions[ name ];
		}
    }
    
    this.filterBtn.removeClass( "filter-btn-selected" );
    this.filterBtn.addClass( "filter-btn" );
    
    this.container.fadeOut( 200 );
};

VariableFilters.prototype.getConditions = function() {
    var conditions = [];
    for( var variable in this.conditions ) {
		var obj = {};
		obj.variable = variable;
		obj.conditions = this.conditions[variable] ;
		conditions.push( obj );
    }
    
    return JSON.stringify( conditions );
};

/**
 * Single condition element
 * @returns
 */
VariableQueryCondition = function() {
    
    this.condition 	= VariableQueryCondition.availableConditions[0];
    this.value1 	= "";
    this.value2		= "";
    
};

VariableQueryCondition.prototype.appendUIElements = function( container ) {
    
    var select = $( '<select class="form-control"></select>' );
    
    for( var i in  VariableQueryCondition.availableConditions ) {
		var variableCondition = VariableQueryCondition.availableConditions[ i ];
		var opt = $( "<option></option>");
		opt.val( variableCondition );
		opt.html( variableCondition );
		select.append( opt );
    }
    
    var div = $( '<div class="form-item width50 float-left"></div>' );
    div.append( select );
    container.append( div );

    
    var input1 = $( '<input type="text" class="form-control">' );
    div = $( '<div class="form-item width25 float-left"></div>' );
    div.append( input1 );
    container.append( div );
    
    
    var input2 = input1.clone();
    div = $( '<div class="form-item width25 float-left"></div>' );
    div.append( input2 );
    container.append( div );
    
    select.change( $.proxy( function(){
	this.condition = select.val();
	this.updateButtonsState( input1 , input2 );
    } , this ) );
    
    input1.change( $.proxy( function(){
    	this.value1 = input1.val();
    } , this ) );
    
    input2.change( $.proxy( function(){
    	this.value2 = input2.val();
    } , this ) );
    
    
    if( this.condition !== "" ) {
    	select.val( this.condition );
    }
    if( this.value1 !== "" ) {
    	input1.val( this.value1 );
    }
    if( this.value2 !== "" ) {
    	input2.val( this.value2 );
    }
    
    this.updateButtonsState( input1 , input2 );
};

/**
 * Update ui input element statuses based on the current values  
 */
VariableQueryCondition.prototype.updateButtonsState = function( input1 , input2 ) {
    input1.visible();
    
    switch ( this.condition ) {
	case "BETWEEN":
	case "NOT BETWEEN":
	    input2.visible();
	    break;
	case "IS NULL":
	case "IS NOT NULL":
	    input1.invisible();
	    this.value1 = null;
	default:
	    this.value2 = null;
	    input2.invisible();
	    break;
    }
    
}; 

VariableQueryCondition.prototype.validate = function( variable ) {
    var quantitative = variable.type === "QUANTITATIVE";
    var valid = true;
    switch ( this.condition ) {
	case "BETWEEN":
	case "NOT BETWEEN":
	    if( this.value1 !== "" && this.value2 !== "" ) {
		if( quantitative ){
		    valid = $.isNumeric( this.value1 ) && $.isNumeric( this.value2 ); 
		} 
	    }  else {
		valid = false;
	    } 
	    break;
	case "IS NULL":
	case "IS NOT NULL":
	    valid = true;
	    break;
	default:
	    if( this.value1 !== "" ) {
		if( quantitative ){
		    valid = $.isNumeric( this.value1 ); 
		}
	    }  else {
		valid = false;
	    } 
	}
    
    if( !valid ) {
    	UI.showError( "Conditions not valid" , true );
    }
    
    return valid;
};

VariableQueryCondition.availableConditions = [ "=" , "!=" , "<" , "<=" , ">" , ">=" , "LIKE" , "NOT LIKE" , "BETWEEN" , "NOT BETWEEN" , "IS NULL" , "IS NOT NULL" ];




CategoricalVariableQueryCondition = function() {
	this.condition = "IN";
	this.values 	= [];
};

CategoricalVariableQueryCondition.prototype.appendUIElements = function( container ) {
	var categoriesContainer = $( '<div class="width100 float-left"></div>');
	container.append( categoriesContainer );
	
	var $this = this;
	var categories = $( this ).data( "categories" );
	for( var i in categories ){
		var category	= categories[ i ];
		
		var addButton = function() {
			var code 		= category.code;
			var caption 	= category.caption;
			
			var div = $( '<div class="float-left width25" style="padding: 0.1em 0.1em;"></div>');
			categoriesContainer.append( div );
			var btn = $( '<button class="btn option-btn width100" style="font-size: 0.8em;">' );
			btn.html( code );
			div.append( btn );
			
			// enable caption tooltip
			btn.tooltip({ title: caption, delay: { show: 200, hide: 100 }});
			
			var optionBtn = new OptionButton( btn );
			
			optionBtn.select( function(){
				$this.values.push( code );
			} );
			
			optionBtn.deselect( function() {
				for( var i in $this.values ) {
					var c = $this.values[i]; 
					if( c === code ){
						$this.values.splice( i , 1 );
						return;
					}
				}
			}  );
			
			// select button
			if( $.inArray( code , $this.values) > -1  ) {
				optionBtn.select();
			}
		};
		
		addButton();
	}
};

CategoricalVariableQueryCondition.prototype.validate = function( variable ) {
	if( this.values.length <= 0 ){
		UI.showError( "At least one category must be selected" , true );
		return false;
	}
	return true;
}

