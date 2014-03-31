/**
 * Manger for error calculation section
 * 
 *  @author Mino Togna
 */

ErrorCalculationManager = function( container ) {
	
	this.container = $( container );
	
	this.init();
};

ErrorCalculationManager.prototype.init = function(){

	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		console.log( ws );
	});
}; 