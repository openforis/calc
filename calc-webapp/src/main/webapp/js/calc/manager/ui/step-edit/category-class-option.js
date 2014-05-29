/**
 * Step edit type category.
 * Class options ui manager
 * 
 * @author Mino Togna
 */

CategoryClassOption = function( cls , editManager , parentContainer ){
	var container = $( '<div class="row no-margin" style="padding-top : 5px"></div>' );
	
	var divCode = $( '<div class="col-md-2"></div>' );
	divCode.html( cls.code );
	var inputCode = $( '<input type="hidden"> ');
	inputCode.attr( "name" , "categoryClassIds['" + cls.id + "']" );
	inputCode.val( cls.id );
	divCode.append( inputCode );
	container.append( divCode );
	
	var divVar = $( '<div class="col-md-3"></div>' );
	container.append( divVar );
	var selectVar = $( '<select class="form-control" name=""></select>' );
	selectVar.attr( "name" , "categoryClassVariables['" + cls.id + "']" );
	divVar.append( selectVar );
	var selectCombo = selectVar.combobox();
	selectCombo.data( editManager.getSelectedEntity().getAncestorsVariables() , "id" , "name" ); 
	
	var divVarFiler = $( '<div class="col-md-6"></div>' );
	container.append( divVarFiler );
	var v = new CategoryClassOptionCondition( divVarFiler , cls.id );
	
	parentContainer.append( container );
};


CategoryClassOptionCondition = function( container , cls ) {
    
    this.condition 	= CategoryClassOptionCondition.conditions[0];
    this.value1 	= "";
    this.value2		= "";
  
    this.appendUIElements( container , cls );
};

CategoryClassOptionCondition.prototype.appendUIElements = function( container , cls ) {
    
    var select = $( '<select class="form-control"></select>' );
    select.attr( "name" , "categoryClassConditions['" + cls + "']" );
    
    for( var i in  CategoryClassOptionCondition.conditions ) {
		var variableCondition = CategoryClassOptionCondition.conditions[ i ];
		var opt = $( "<option></option>");
		opt.val( variableCondition );
		opt.html( variableCondition );
		select.append( opt );
    }
    
    var div = $( '<div class="form-item width50 float-left"></div>' );
    div.append( select );
    container.append( div );

    
    var input1 = $( '<input type="text" class="form-control">' );
    input1.attr( "name" , "categoryClassLeftConditions['" + cls + "']" );
    div = $( '<div class="form-item width25 float-left"></div>' );
    div.append( input1 );
    container.append( div );
    
    
    var input2 = input1.clone();
    input2.attr( "name" , "categoryClassRightConditions['" + cls + "']" );
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
CategoryClassOptionCondition.prototype.updateButtonsState = function( input1 , input2 ) {
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

//CategoryClassOptionCondition.prototype.validate = function( variable ) {
//    var quantitative = variable.type === "QUANTITATIVE";
//    var valid = true;
//    switch ( this.condition ) {
//	case "BETWEEN":
//	case "NOT BETWEEN":
//	    if( this.value1 !== "" && this.value2 !== "" ) {
//			if( quantitative ){
//			    valid = $.isNumeric( this.value1 ) && $.isNumeric( this.value2 ); 
//			} 
//	    }  else {
//	    	valid = false;
//	    } 
//	    break;
//	case "IS NULL":
//	case "IS NOT NULL":
//	    valid = true;
//	    break;
//	default:
//	    if( this.value1 !== "" ) {
//		if( quantitative ){
//		    valid = $.isNumeric( this.value1 ); 
//		}
//	    }  else {
//		valid = false;
//	    } 
//	}
//    
//    if( !valid ) {
//    	UI.showError( "Conditions not valid" , true );
//    }
//    
//    return valid;
//};

CategoryClassOptionCondition.conditions = [ "=" , "!=" , "<" , "<=" , ">" , ">=" , "LIKE" , "NOT LIKE" , "BETWEEN" , "NOT BETWEEN" , "IS NULL" , "IS NOT NULL" ];

