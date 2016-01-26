/**
 * manager for two stages sampling with SRS
 * @author Mino Togna
 */

TwoStagesManager = function(container , sdManager) {
	
	this.container = $( container );
	this.sdManager = sdManager;
	
	// primary sampling unit section
	this.psuSection 		= this.container.find(".psu-section");
	this.areaCombo			= this.psuSection.find( '[name=area-column]' ).combobox({});
	this.noBaseUnitCombo	= this.psuSection.find( '[name=no-base-unit-column]' ).combobox({});
	
	this.ssuSection 		= this.container.find(".ssu-section");
	this.ssuCombo	 		= this.ssuSection.find( '[name=ssu]' ).combobox({});

	// upload section
	//upload form container
	this.uploadSection 		= this.container.find(".upload-section");
	
	// import psu csv section 
	this.importSection 		= this.container.find(".import-section");
	
	// table join settings
	this.tableJoin 			= new TableJoin( this.container.find(".table-join") );
	this.tableJoin.hide();
	
	//form file upload manager (to be initialized in the init method)
	this.formFileUpload = null;
	
	this.init();
};

TwoStagesManager.prototype.show = function() {
	this.container.fadeIn(200);
	
	this.psuSection.show();
	this.ssuSection.show();
	this.importSection.hide();
	
	// update join section
//	this.updateTableJoin();
	this.updatePSUSection();
};

TwoStagesManager.prototype.hide = function() {
	this.container.hide();
};

TwoStagesManager.prototype.init = function(){
	var $this = this;

	this.psuSection.show();
	this.ssuSection.show();
	this.importSection.hide();
	
	//file upload success handler
	var uploadSuccess = function ( response ) {
		$this.showImport( response.fields.filepath, response.fields.headers );
	};
	//form file upload manager
	this.formFileUpload = new FormFileUpload(this.uploadSection, null, uploadSuccess);
	
	// init psu section
	this.areaCombo.change( function(){
		if( ! $this.sdManager.samplingDesign.twoStagesSettings ){
			$this.sdManager.samplingDesign.twoStagesSettings = {};
		}
		
		var areaCol	= $this.areaCombo.val();
		$this.sdManager.samplingDesign.twoStagesSettings.areaColumn = areaCol;
	});
	this.noBaseUnitCombo.change( function(){
		if( ! $this.sdManager.samplingDesign.twoStagesSettings ){
			$this.sdManager.samplingDesign.twoStagesSettings = {};
		}
		
		var noBaseUnitCol = $this.noBaseUnitCombo.val();
		$this.sdManager.samplingDesign.twoStagesSettings.noBaseUnitColumn = noBaseUnitCol;
	});
	
	// init ssu section
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		$this.ssuCombo.data( ws.entities , 'originalId' , 'name');
		
		$this.ssuCombo.change( function(e){
			var ssuOrigId	= $this.ssuCombo.val();
			if( ! $this.sdManager.samplingDesign.twoStagesSettings ){
				$this.sdManager.samplingDesign.twoStagesSettings = {};
			}
			$this.sdManager.samplingDesign.twoStagesSettings.ssuOriginalId	= ssuOrigId;
			
			$this.updateSSUSection();
		});
	});
};

TwoStagesManager.prototype.showImport = function( filepath, headers ){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		
		$this.importTable 			= new CsvTableImport( $this.importSection, filepath, headers, ws.primarySUTableName );
		$this.importTable.extSchema = true;
		// import csv table
		$this.importTable.import(function(schema, table) {
			UI.lock();
				
			$this.importSection.hide(0);
			$this.psuSection.fadeIn();
			$this.ssuSection.fadeIn();
			
			$this.updatePSUSection();
			
			UI.unlock();
		});
		
		$this.psuSection.hide(0);
		$this.ssuSection.hide(0);
		$this.tableJoin.hide();
		
		$this.importSection.fadeIn();
	
	});
};

/**
 * Update primary sampling unit table
 */
TwoStagesManager.prototype.updatePSUSection = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( $this.sdManager.samplingDesign.twoStages === true ) {
			
			UI.lock();
			$this.sdManager.loadPrimarySUTableInfo( function(){
				// $this.sdManager.primarySUTableInfo
				if( $this.sdManager.primarySUTableInfo ){
					var defaultAreaColumnVal 	= null;
					var defaultNoBaseUnitColumn = null;
					if( $this.sdManager.samplingDesign.twoStagesSettings ){
						
						var areaCol = $this.sdManager.samplingDesign.twoStagesSettings.areaColumn;
						if( StringUtils.isNotBlank(areaCol) ){
							defaultAreaColumnVal = areaCol;
						}
						
						var noBaseUnitCol = $this.sdManager.samplingDesign.twoStagesSettings.noBaseUnitColumn;
						if( StringUtils.isNotBlank(noBaseUnitCol) ){
							defaultNoBaseUnitColumn = noBaseUnitCol;
						}
						
					}
					
					$this.areaCombo.data(  $this.sdManager.primarySUTableInfo.fields.columns , 'column_name' , 'column_name' , defaultAreaColumnVal );
					$this.noBaseUnitCombo.data(  $this.sdManager.primarySUTableInfo.fields.columns , 'column_name' , 'column_name' , defaultNoBaseUnitColumn );
					
				}
				
				$this.updateSSUSection();
				
				UI.unlock();
			} );
		} 
	});
};
/**
 * Update primary sampling unit table
 */
TwoStagesManager.prototype.updateSSUSection = function(){
	var $this  = this;

	if( $this.sdManager.primarySUTableInfo && $this.sdManager.samplingDesign.twoStagesSettings &&  $this.sdManager.samplingDesign.twoStagesSettings.ssuOriginalId){
		var ssuOriginalId = $this.sdManager.samplingDesign.twoStagesSettings.ssuOriginalId;
		
		$this.ssuCombo.val( ssuOriginalId );
		
		WorkspaceManager.getInstance().activeWorkspace(function(ws){
			
			var entity = ws.getEntityByOriginalId( ssuOriginalId );

			var tableInfo = function(){
				this.table 			= entity.name;
				this.schema 		= ws.inputSchema;
				this.fields 		= {};
				this.fields.table 	= entity.name;
				this.fields.schema 	= ws.inputSchema;
				this.fields.columns = [];
				
				var vars = entity.hierarchyVariables();
				for( var i in vars ){
					var variable = vars[ i ];
					this.fields.columns.push( { 'column_name' : variable.name } );
				}
			};

			$this.ssuTableInfo = new tableInfo();
			
			$this.tableJoin.setTableInfo( $this.sdManager.primarySUTableInfo , $this.ssuTableInfo , "PSU table" );
			
			var joinSettings = $this.sdManager.samplingDesign.twoStagesSettings.joinSettings;
			if( joinSettings ){
				$this.tableJoin.settings = joinSettings;
			}
			$this.tableJoin.show();
		});
	} else {
		$this.tableJoin.hide();
		$this.tableJoin.empty();
		
		if( ! $this.sdManager.samplingDesign.twoStagesSettings ){
			$this.sdManager.samplingDesign.twoStagesSettings = {};
		} 
		$this.sdManager.samplingDesign.twoStagesSettings.joinSettings = null;
		$this.tableJoin.settings	= null;
	}
};

TwoStagesManager.prototype.updateTableJoin = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( $this.sdManager.samplingDesign.twoStages === true ) {
			
			UI.lock();
			$this.sdManager.loadPrimarySUTableInfo( function(){
				$this.tableJoin.setTableInfo( $this.sdManager.primarySUTableInfo , $this.sdManager.samplingUnitTableInfo , "primary sampling unit table" );
				$this.tableJoin.show();
				
				UI.unlock();
			} );
		} else {
			$this.tableJoin.hide();
		}
	});
	
};

TwoStagesManager.prototype.validate = function() {
	if( StringUtils.isBlank( this.areaCombo.val() ) ){
		UI.showError( "Select a valid area column" , true );
		return false;
	}
	if( StringUtils.isBlank( this.noBaseUnitCombo.val() ) ){
		UI.showError( "Select a valid number of base unit column" , true );
		return false;
	}
	if( StringUtils.isBlank( this.ssuCombo.val() ) ){
		UI.showError( "Select a secondary sampling unit" , true );
		return false;
	}
	var rows = this.tableJoin.rows;
	for( var i in rows ){
		var row = rows[i];
		if( !row.isFilled() ){
			UI.showError( "Select valid columns for joining the two tables", true );
			return false;
		}
	}
	return true;
};

TwoStagesManager.prototype.joinOptions = function(){
	return this.tableJoin.jsonSettings();
};
TwoStagesManager.prototype.setJoinOptions = function(options){
	this.tableJoin.settings = options;
};

