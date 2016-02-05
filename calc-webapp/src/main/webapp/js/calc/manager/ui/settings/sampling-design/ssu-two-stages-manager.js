/**
 * Manager for SSU table in Two Stages w/SRS sampling 
 * @author M. Togna
 */
SSUTwoStagesManager = function( container , sdERDManager , stepNo ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
		var ssuChange 	= $.proxy(this.ssuChange , this );
		var dataProvider 	= new EntityDataProvider( ssuChange );
		dataProvider.tableTitle ='SSU: ';
		
		this.psuJoin	= new ERDTableJoin( 'baseUnitPhase1Join' );
		this.psuJoin.leftJoinPointCssClass = 'anchor-right';
		
		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
		this.addJoin( this.psuJoin );
		
		EventBus.addEventListener( "calc.sampling-design.two-stages-change", 	this.update, this );
		EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
		EventBus.addEventListener( "calc.sampling-design.psu-table-change", this.psuTableChange , this );
		
		if( this.sd().twoStagesSettings && this.sd().twoStagesSettings.ssuOriginalId ){
			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				
				var entity = ws.getEntityByOriginalId( $this.sd().twoStagesSettings.ssuOriginalId );
				$this.ssuChange( entity.id );
			});
		}

	} , this ) );
	
	
};
SSUTwoStagesManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
SSUTwoStagesManager.prototype.constructor 	= SSUTwoStagesManager;


SSUTwoStagesManager.prototype.update = function(){
	
	if( this.sd().twoStages === true ){
		
		if( !this.sd().twoStagesSettings ){
			this.sd().twoStagesSettings = {};
		}
		
		this.container.fadeIn();
		this.psuJoin.show();
		this.highlight();
		
	} else {
		$( '.two-stages-container' ).hide();
		this.container.hide();
		this.psuJoin.hide();
	}

};



SSUTwoStagesManager.prototype.ssuChange = function( entityId ){
	if( ! this.sd().twoStagesSettings ){
		this.sd().twoStagesSettings = {};
	}
	this.dataProvider.setEntityId( entityId );
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		var entity = ws.getEntityById( entityId );
		$this.sd().twoStagesSettings.ssuOriginalId	= entity.originalId;
	});
	
	this.updateJoins();
};
SSUTwoStagesManager.prototype.updateJoins = function(){
	if( this.sd().twoStages === true ){
		
		this.psuJoin.setRightTable( this.table );
		this.psuJoin.setLeftTable( this.sdERDManager.psuManager.table );
		
		if( this.dataProvider.getTableInfo() &&  this.sdERDManager.psuManager.dataProvider.getTableInfo() ){
			this.psuJoin.connect( this.sd().twoStagesSettings.joinSettings );
			
			this.updateEditMode();
		}
		
	} else {
		this.psuJoin.reset();
		this.psuJoin.hide();
	}
	
};

SSUTwoStagesManager.prototype.baseUnitChange = function(){
	//Reset phase 1 join settings
//	this.sd().phase1JoinSettings = {};
//	this.baseUnitPhase1Join.reset();
	
};
SSUTwoStagesManager.prototype.psuTableChange = function(){
	this.updateJoins();
};