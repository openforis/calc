/**
 * Home Page data section manager
 * 
 * @author Mino Togna
 */
HomeDataManager = function( container ) {
	this.container = $( container );
	
	this.dataVisManager = new DataVisualisationManager( this.container.find(".data-vis") );
	this.dataVisManager.show();
};

HomeDataManager.prototype.init = function() {
	
};
