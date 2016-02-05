/**
 * 
 */
$(document).ready(function(){
	
	
	ConnectionManagerClass = function(){
		EventBus.addEventListener( 'calc.sampling-design.update-connections', this.updateConnections , this );
	};
	
	ConnectionManagerClass.prototype.updateConnections = function( evt , id ){
		var selector = 'connection';
		if( id ){
			selector += '.'+id;
		}
		$( selector ).connections('update');
	};
	
	ConnectionManager = new ConnectionManagerClass();
	
});