SamplingDesignViewTableDataManager = function( container ){
	this.container 		= container;
	this.tableTitle		= this.container.find( '.table-title' );;
	this.dataContainer 	= this.container.find( '.data-container' );
	
	this.closeBtn		= this.container.find( 'button.close-btn' );
	this.closeBtn.click( $.proxy(function(e){
		e.preventDefault();
//		this.container.animate({left:'100%'},600);
		this.container.removeClass('opened').addClass('closed');

	},this) );
	
	this.aoiViewDataManager 	= new ReportingUnitViewDataManager( this.dataContainer );
	this.viewTableDataManager 	= new ViewTableDataManager( this.dataContainer );
	
	EventBus.addEventListener('calc.sampling-design.show-table-data', this.showTableData, this );
};


SamplingDesignViewTableDataManager.prototype.showTableData = function( evt , erdTable){
//	console.log( erdTable );
	
	this.dataContainer.empty();
	this.tableTitle.empty();
	
	var show 			= false;
	var dataProvider 	= erdTable.dataProvider;
	
	switch (dataProvider.tableType) {
	
	case CsvFileDataProvider.AOI_TABLE_TYPE :
		show = this.aoiViewDataManager.show();
		
		break;
		
	case CsvFileDataProvider.STRATUM_TABLE_TYPE :
		this.viewTableDataManager.dataProvider = new StrataDataProvider();
		show = this.viewTableDataManager.show();
		
		break;

	default:
		
		var schema 	= '';
		var table 	= ''; 
		if( dataProvider.getTableInfo() ){
			schema = dataProvider.getTableInfo().fields.schema;
			table = dataProvider.getTableInfo().fields.table;
		}
		this.viewTableDataManager.dataProvider = new TableDataProvider(schema, table);
		show = this.viewTableDataManager.show();
		
		break;
		
	}
	
	if( show ){
		this.container.removeClass('closed').addClass('opened');
		
		var tableHeader = ( dataProvider.tableAlias ) ?  dataProvider.tableAlias : dataProvider.tableName;
		this.tableTitle.html( tableHeader );
		
	} else {
		UI.showWarning( "Upload a valid CSV file in order to visualize the data.", true );
	}
	
};
