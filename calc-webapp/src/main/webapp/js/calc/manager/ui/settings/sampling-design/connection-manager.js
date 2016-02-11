/**
 * 
 */
$(document).ready(function(){
	
	
	ConnectionManagerClass = function(){
		
		EventBus.addEventListener( 'calc.sampling-design.update-connections', this.updateConnections , this );
		
		EventBus.addEventListener( 'calc.page-update', function(evt, page){
			if(page=='home'){
				$('connection').invisible();
			}
		} , this );
		
		$(window).resize( function(e){
			EventBus.dispatch( 'calc.sampling-design.update-connections', null );
		});
		
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