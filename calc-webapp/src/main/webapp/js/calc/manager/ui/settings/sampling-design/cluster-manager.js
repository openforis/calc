/**
 * Manager for SSU table in Two Stages w/SRS sampling 
 * @author M. Togna
 */
ClusterManager = function( container , sdERDManager , stepNo ){
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		
		var clusterChange 	= $.proxy(this.clusterChange , this );
		var dataProvider 	= new EntityDataProvider( clusterChange );
		dataProvider.tableTitle ='Cluster: ';
		
//		this.psuJoin	= new ERDTableJoin( 'psuSsuJoin' );
//		this.psuJoin.rightJoinPointCssClass = 'anchor-right';
//		this.psuJoin.onChange = $.proxy( this.joinChange , this ); 
		
		this.baseUnitClusterJoin	= new ERDTableJoin( 'baseUnitClusterJoin' );
		this.baseUnitClusterJoin.leftColumnsReadOnly = true;
		this.baseUnitClusterJoin.rightColumnsReadOnly = true;
		SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
		
//		this.addJoin( this.psuJoin );
		
		EventBus.addEventListener( "calc.sampling-design.cluster-change", 	this.update, this );
//		EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
//		EventBus.addEventListener( "calc.sampling-design.psu-table-change", this.updateJoins , this );
		
		this.update();
		if( this.sd().clusterOriginalId ){
//			WorkspaceManager.getInstance().activeWorkspace(function(ws){
				
				var entity = ws.getEntityByOriginalId( $this.sd().clusterOriginalId );
				$this.clusterChange( entity.id );
//			});
		}

	} , this ) );
	
	
};
ClusterManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
ClusterManager.prototype.constructor 	= ClusterManager;


ClusterManager.prototype.update = function(){
	
	if( this.sd().cluster2 === true ){
		
//		if( !this.sd().twoStagesSettings ){
//			this.sd().twoStagesSettings = {};
//		}
		
		this.container.fadeIn();
//		this.psuJoin.show();
		this.highlight();
		
	} else {
//		$( '.two-stages-container' ).hide();
		this.container.hide();
//		this.psuJoin.disconnect();
	}

};



ClusterManager.prototype.clusterChange = function( entityId ){
//	if( ! this.sd().twoStagesSettings ){
//		this.sd().twoStagesSettings = {};
//	}
	this.dataProvider.setEntityId( entityId );
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		var entity = ws.getEntityById( entityId );
		var clusterOrigId = ( entity ) ? entity.originalId : null;
		$this.sd().clusterOriginalId	= clusterOrigId;
	});
	
	this.updateJoins();
};

ClusterManager.prototype.updateJoins = function(){
//	this.psuJoin.disconnect();
	this.baseUnitClusterJoin.disconnect();
	if( this.sd().cluster2 === true ){
		
//		this.psuJoin.setRightTable( this.table );
//		this.psuJoin.setLeftTable( this.sdERDManager.psuManager.table );
		
		if( this.dataProvider.getTableInfo() ){ //&&  this.sdERDManager.psuManager.dataProvider.getTableInfo() ){
//			this.psuJoin.connect( this.sd().twoStagesSettings.joinSettings );
			
			this.updateEditMode();
		}
		
		
		this.baseUnitClusterJoin.setRightTable( this.table );
		this.baseUnitClusterJoin.setLeftTable( this.sdERDManager.baseUnitManager.table );
		
		var lInfo = this.sdERDManager.baseUnitManager.dataProvider.getTableInfo();
		var rInfo = this.dataProvider.getTableInfo();
		if( lInfo && rInfo ){
			
			//{"rightTable":{"schema":"calc","table":"plot"},"columns":[{"left":"plot","right":"plot_no"},{"left":"cluster","right":"cluster_id"}],"leftTable":{"schema":"calc","table":"_phase1_plot_laputa"}}
			var joinSettings = {};
			joinSettings.rightTable 		= {};
			joinSettings.rightTable.schema 	= rInfo.fields.schema;
			joinSettings.rightTable.table 	= rInfo.fields.table;
			joinSettings.leftTable 		= {};
			joinSettings.leftTable.schema 	= lInfo.fields.schema;
			joinSettings.leftTable.table 	= lInfo.fields.table;
			joinSettings.columns	= [];
			var colJoin = {};
			colJoin.left 	= lInfo.fields.idColumn.column_name; 
			colJoin.right 	= rInfo.fields.idColumn.column_name; 
			joinSettings.columns.push( colJoin );
			this.baseUnitClusterJoin.connect(joinSettings);
			this.baseUnitClusterJoin.setEditMode(false);
		}
		
		
		
	} else {
//		this.psuJoin.reset();
//		this.psuJoin.hide();
	}
	
};

ClusterManager.prototype.joinChange = function(){
//	this.sd().twoStagesSettings.joinSettings = this.psuJoin.jsonSettings();
};