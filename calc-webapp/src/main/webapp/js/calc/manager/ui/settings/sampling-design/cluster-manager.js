/**
 * Manager for Cluster sampling 
 * @author M. Togna
 */
ClusterManager = function( container , sdERDManager , stepNo ){
	var $this = this;
//	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
//		var ssuChange 	= $.proxy(this.ssuChange , this );
//		var dataProvider 	= new EntityDataProvider( ssuChange );
		this.sdERDManager		= sdERDManager;
		this.stepNo				= stepNo;
		this.clusterColumn 		= new ERDTableColumnSelector( 'cluster_column', 'Cluster column' );

//		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
		EventBus.addEventListener( "calc.sampling-design.phase1-table-change", 	this.update, this );
		EventBus.addEventListener( "calc.sampling-design.two-phases-change", 	this.update, this );
		EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.update , this );
		EventBus.addEventListener( "calc.sampling-design.cluster-change", this.update , this );

//	} , this ) );
	
		EventBus.addEventListener( "calc.sampling-design.show-step", this.showEditStep , this );
};

ClusterManager.prototype.showEditStep = function( evt, stepNo ){
	this.currentStepNo = stepNo;
	
	if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo ){
		this.highlight();
		this.clusterColumn.setEditMode( true );
	} else {
		this.clusterColumn.setEditMode( false );
	}
	
};

ClusterManager.prototype.highlight = function(){
	if( this.sdERDManager.editMode === true && this.currentStepNo == this.stepNo ){
		this.clusterColumn.highlight();
	}
};

ClusterManager.prototype.sd = function(){
	return this.sdERDManager.samplingDesign;
};
//ClusterManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
//ClusterManager.prototype.constructor 	= ClusterManager;


ClusterManager.prototype.update = function(){
	this.clusterColumn.disconnect();
	
	if( this.sd().cluster === true ){
		
		var table = null;
		if( this.sd().twoPhases === true ){

			if( this.sdERDManager.twoPhasesManager.dataProvider.getTableInfo() ){
				table = this.sdERDManager.twoPhasesManager.table;
			}
			
		} else {
			table = this.sdERDManager.baseUnitManager.table;
		}
		
		if( table ){
			var value = this.sd().clusterColumnSettings.column;
			var onChange = function(){
				
			};
			this.clusterColumn.connect( table, value, $.proxy(onChange,this) );

			if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo ){
				this.clusterColumn.setEditMode( true );
			} else {
				this.clusterColumn.setEditMode( false );
			}
			
			this.highlight();
		}
		
	}
	
	EventBus.dispatch( 'calc.sampling-design.update-connections', null );
//		
//		if( !this.sd().twoStagesSettings ){
//			this.sd().twoStagesSettings = {};
//		}
//		
//		this.container.fadeIn();
//		this.psuJoin.show();
//		this.highlight();
//	} else {
//		$( '.two-stages-container' ).hide();
//		this.container.hide();
//		this.psuJoin.hide();
//	}

};


//
//ClusterManager.prototype.ssuChange = function( entityId ){
//	if( ! this.sd().twoStagesSettings ){
//		this.sd().twoStagesSettings = {};
//	}
//	this.dataProvider.setEntityId( entityId );
//	
//	var $this = this;
//	WorkspaceManager.getInstance().activeWorkspace(function(ws){
//		var entity = ws.getEntityById( entityId );
//		$this.sd().twoStagesSettings.ssuOriginalId	= entity.originalId;
//	});
//	
//	this.updateJoins();
//};

ClusterManager.prototype.baseUnitChange = function(){
	//Reset phase 1 join settings
//	this.sd().phase1JoinSettings = {};
//	this.baseUnitPhase1Join.reset();
	console.log( this.sdERDManager.baseUnitManager.dataProvider.getTableInfo() );	
};
