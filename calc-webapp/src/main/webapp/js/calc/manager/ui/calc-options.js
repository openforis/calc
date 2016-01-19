/**
 * 
 */
$(document).ready(function(){
	/**
	 * Option sections
	 */
	Calc.calcButton			= Calc.footer.find( ".calc-btn" );
	Calc.calcOptionsSection	= Calc.footer.find( ".calc-options" );
	Calc.calcOptionsSection.data( 'status' , 'hidden' );
	
	Calc.showOptions = function( ){
		Calc.calcOptionsSection.animate({left: "80%" }, 400);
		Calc.calcOptionsSection.data( 'status' , 'visible' );
	};
	Calc.hideOptions = function( ){
		Calc.calcOptionsSection.animate({left: "101%" }, 400);
		Calc.calcOptionsSection.data( 'status' , 'hidden' );
	};
	Calc.calcButton.click( function(e){
		e.preventDefault();
		Calc.calcButton.blur();
		if( Calc.calcOptionsSection.data( 'status' ) == 'hidden' ){
			Calc.showOptions();
		} else {
			Calc.hideOptions();
		} 
	});
});