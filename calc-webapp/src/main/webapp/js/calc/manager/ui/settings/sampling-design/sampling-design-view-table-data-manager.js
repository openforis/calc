SamplingDesignViewTableDataManager = function( container ){
	this.container 		= container;
	this.tableTitle		= this.container.find( '.table-title' );;
	this.dataContainer 	= this.container.find( '.data-container' );
	
	this.closeBtn		= this.container.find( 'button.close-btn' );
	this.closeBtn.click( $.proxy(function(e){
		e.preventDefault();
		this.container.animate({left:'100%'},600);
	},this) );
	
	this.aoiViewDataManager = new ReportingUnitViewDataManager( this.dataContainer );
	
	EventBus.addEventListener('calc.sampling-design.show-table-data', this.showTableData, this );
};


SamplingDesignViewTableDataManager.prototype.showTableData = function( evt , erdTable){
//	console.log( erdTable );
	
	this.aoiViewDataManager.reset();
	this.tableTitle.empty();
	
	var show 			= false;
	var dataProvider 	= erdTable.dataProvider;
	switch (dataProvider.tableType) {
	case CsvFileDataProvider.AOI_TABLE_TYPE :
		show = this.aoiViewDataManager.show();
		break;
	case CsvFileDataProvider.STRATUM_TABLE_TYPE :
		
		break;

	default:
		break;
	}
	
	if( show ){
		this.container.animate( {left:'0%'}, 600 );
		
		var tableHeader = ( dataProvider.tableAlias ) ?  dataProvider.tableAlias : dataProvider.tableName;
		this.tableTitle.html( tableHeader );
		
	} else {
		UI.showWarning( "Upload a valid CSV file in order to visualize the data.", true );
	}
	
};
