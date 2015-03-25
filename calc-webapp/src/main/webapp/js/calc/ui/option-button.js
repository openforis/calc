/**
 * Option Button 
 * 
 * @author Mino Togna
 */
OptionButton = function(button) {
	
	this.button = $(button);
	this.button.addClass("option-btn");
	
	var $this = this;
	
	// on click function
	var click = function(e) {
		if( $this.button.hasClass("option-btn") ) {
			// button is not selected. it gets selected
			if( $this.select ) {
				$this.select();
			}
		} else {
			if( $this.deselect ) {
				$this.deselect();
			}
		}
	};
	
	this.button.click( click );
	
	// set to true when disable is not allowed
	this.disableUnselect	= false;
	this.disableOpacity		= 1;
};

OptionButton.cssClassSelected 	= "option-btn-selected";
OptionButton.cssClassUnselected = "option-btn";

OptionButton.prototype.select = function( select ) {
	var args = Array.prototype.slice.call( arguments );
	if ( args.length > 1 ) { 
	    args.shift(); 
	}
	
	var $this = this;
	this.select = function(){
		$this.updateUI( true );
		if( select ) {
			select.apply( this , args );
		} 		
	}; 
};

OptionButton.prototype.deselect = function( deselect ) {
	var args = Array.prototype.slice.call( arguments );
	if ( args.length > 1 ) { 
	    args.shift(); 
	}
	
	var $this = this;
	this.deselect = function(){
		$this.updateUI( false );
		
		if( deselect ) {
			deselect.apply( this , args );
		}
	};
};

OptionButton.prototype.disable =function(){
	UI.disable( this.button );
};

OptionButton.prototype.enable =function(){
	UI.enable( this.button );
};

OptionButton.prototype.displayAsSelected = function(){
	this.updateUI( true );
};

OptionButton.prototype.displayAsUnelected = function(){
	this.updateUI( false );
};

/**
 * Update UI state based on the selected argument
 * @param selected
 */
OptionButton.prototype.updateUI = function( selected ){
	this.button.removeClass( OptionButton.cssClassSelected ).removeClass( OptionButton.cssClassUnselected );
	var $this = this;
	if( selected ){
		this.button.addClass( OptionButton.cssClassSelected );
		if( this.disableUnselect ){
			this.disable();
		} else {
			this.enable();
		}
		
		if( this.disableOpacity != 1 ){
			setTimeout( function(){
				$this.button.animate( {opacity:1} , 400 );
			}, 100);
		}
	} else {
		this.button.addClass( OptionButton.cssClassUnselected );
		if( this.disableOpacity != 1 ){
			setTimeout( function(){
				$this.button.animate( {opacity:$this.disableOpacity} , 400 );
			}, 100);
		}
	}
};

OptionButton.prototype.show = function(){
	this.button.fadeIn();
};

OptionButton.prototype.hide = function(){
	this.button.hide();
};