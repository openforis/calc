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
	
};

OptionButton.prototype.select = function(select) {
	var $this = this;
	this.select = function(){
		$this.button.removeClass("option-btn");
		$this.button.addClass("option-btn-selected");
		
		if(select) {
			select();
		} 		
	}; 
};

OptionButton.prototype.deselect = function(deselect) {
	var $this = this;
	this.deselect = function(){
		$this.button.removeClass("option-btn-selected");
		$this.button.addClass("option-btn");
		if(deselect) {
			deselect();
		}
	};
};
