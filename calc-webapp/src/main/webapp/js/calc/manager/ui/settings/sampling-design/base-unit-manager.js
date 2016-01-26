/**
 *  
 */
BaseUnitManager = function( container , editMode ){
	this.container 	= container;
	this.editMode	= editMode;
	
	this.table = new ERDTable( this.container );
};

BaseUnitManager.prototype.show = function( samplingDesign ){
	this.container.fadeIn();
	
	this.samplingDesign = samplingDesign;
	
	var $this = this;
	if( this.editMode ){
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			
			var onChange = function( val ){
				EventBus.dispatch( "calc.sampling-design.base-unit-change", null , val );
			};
			$this.table.setTableNameOptions( ws.entities, 'id','name', $this.samplingDesign.samplingUnitId, onChange);
		});
	}
};

