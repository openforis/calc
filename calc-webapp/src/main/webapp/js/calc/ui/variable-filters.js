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
//	    alert('clicked outside');
	if( this.container ) {
	    this.container.hide();
	}
    } , this ) );

	this.container.click(function(event) {
	    //alert('clicked inside');
	    event.stopPropagation();
	});
    
};

VariableFilters.prototype.show = function( element ) {
    var variable = element.data( 'variable' );
    if( variable ) {
	this.variable = variable;
	this.filterBtn = element;
	
	var btnOffset = element.offset();
	this.container.css( "left" , ( btnOffset.left + 20 )  );
	this.container.fadeIn( 300 );
	this.updateConditions( );
	
	setTimeout( 
		$.proxy( function(){
		    this.container.stop().animate( {top: btnOffset.top}, 100, "easeOutQuart" );
		    this.container.css( "opacity" , 0.97  );
		} , this)
		, 150 
	);
	
    }
};

VariableFilters.prototype.updateConditions = function( ) {
    this.uiConditions.hide();
    this.uiConditions.empty();
    
    var name 		= this.variable.name;
    var conditions 	= this.conditions[ name ];
//    var condition 	= null;
    
    if( conditions && conditions.length > 0 ) {
	// right now only 1 condition per variable is managed
	this.condition = conditions[0];
    } else {
	// init conditions for the given variable
	this.condition = new VariableQueryCondition( this.uiConditions );
    }
    
//    this.addCondition( condition );
    this.condition.appendUIElements( this.uiConditions );
    this.uiConditions.fadeIn();
};


VariableFilters.prototype.apply = function() {
    var name = this.variable.name;
//    var condition = this.conditions[ name ] [0];
    
    if( this.condition.validate( this.variable ) ) {
	this.conditions[ name ] = [];
	this.conditions[ name ].push( this.condition );
	
	this.filterBtn.removeClass( "filter-btn" );
	this.filterBtn.addClass( "filter-btn-selected" );
	
	this.container.fadeOut( 200 );
    }
};

VariableFilters.prototype.reset = function() {
    var name 			= this.variable.name;
//    this.conditions[ name ] 	= null;
    for( var i in this.conditions ){
//	console.log( i );
//	console.log( this.conditions[i] ); 
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
 * @param container
 * @returns
 */
VariableQueryCondition = function( container ) {
    
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
//VariableQueryCondition.availableConditions = {};
//VariableQueryCondition.availableConditions[ "QUANTITATIVE" ] = [ " = " , " != " , " < " , " <= " , " > " , " >= "  , " IS NULL " , " IS NOT NULL "];
//VariableQueryCondition.availableConditions[ "CATEGORICAL" ] = [ " = " , " != " , " < " , " <= " , " > " , " >= "  , " LIKE " , " NOT LIKE " ,  " BETWEEN " , " NOT BETWEEN " , " IS NULL " , " IS NOT NULL "];

