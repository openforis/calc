/**
 * Used to visualize the variable filter button in the data section
 *
 * @author Mino Togna
 */
VariableFilterButton = function( filters ) {
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

VariableFilterButton.prototype.show = function( element ) {
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

VariableFilterButton.prototype.updateConditions = function( ) {
    this.uiConditions.hide();
    this.uiConditions.empty();
    
    var name = this.variable.name;
    var conditions = this.conditions[ name ];
    if( conditions && conditions.length > 0 ) {
	// add conditions 
    } else {
	// add empty condition
	this.conditions[ name ] = [];
	this.addCondition();
    }
    
    this.uiConditions.fadeIn();
};

VariableFilterButton.prototype.addCondition = function( condition ) {
    
    
    
    if( condition ) {
	
    } else {
	var name = this.variable.name;
	var conditions = this.conditions[ name ];
	var queryCondition = new VariableQueryCondition( this.variable , this.uiConditions );
	conditions.push ( queryCondition );
    }
};

VariableFilterButton.prototype.apply = function() {
    this.filterBtn.removeClass( "filter-btn" );
    this.filterBtn.addClass( "filter-btn-selected" );
    
    this.container.fadeOut( 200 );
};

VariableFilterButton.prototype.reset = function() {
    this.filterBtn.removeClass( "filter-btn-selected" );
    this.filterBtn.addClass( "filter-btn" );
    
    this.container.fadeOut( 200 );
};

VariableQueryCondition = function( variable , container ) {
    var select = this.getSelect( variable );
    container.append( select );
};

VariableQueryCondition.prototype.getSelect = function( variable , container ) {
    
    var select = $( '<select class="form-control"></select>' );
    var variableConditions = VariableQueryCondition.availableConditions[ variable.type ];
    
    for( var i in  variableConditions ) {
	var variableCondition = variableConditions[ i ];
	var opt = $( "<option></option>");
	opt.val( variableCondition );
	opt.html( variableCondition );
	select.append( opt );
    }
    
    return select;
};


VariableQueryCondition.availableConditions = {};
VariableQueryCondition.availableConditions[ "QUANTITATIVE" ] = [ " = " , " != " , " < " , " <= " , " > " , " >= "  , " IS NULL " , " IS NOT NULL "];
VariableQueryCondition.availableConditions[ "CATEGORICAL" ] = [ " = " , " != " , " < " , " <= " , " > " , " >= "  , " LIKE " , " NOT LIKE " , " IS NULL " , " IS NOT NULL "];

