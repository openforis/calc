/**
 * jquery calc custom extentsions
 * @author Mino Togna
 * @author S. Ricci
 */

(function($){

	//Attach this new method to jQuery
 	$.fn.extend({ 
 		
 		// set visibility visible to element
 		visible: function() {
			//Iterate over the current set of matched elements
    		return this.each(function() {
    			$(this).css("visibility", "visible");
    		});
    	}
 		,
 		// set visibility hidden to element
 		invisible: function() {
			//Iterate over the current set of matched elements
    		return this.each(function() {
    			$(this).css("visibility", "hidden");
    		});
    	}

	});
	
//pass jQuery to the function, 
//So that we will able to use any valid Javascript variable name 
//to replace "$" SIGN. But, we'll stick to $ (I like dollar sign: ) )		
})(jQuery);