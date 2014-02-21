/**
 * Data table
 * 
 * @author Mino Togna
 */

function DataTable($container) {
	/*
	 * ui components
	 */
	this.container = $container;
	this.dataTablePagination = this.container.find(".data-table-pagination");

	// table results
	this.table = this.container.find(".table-results");
	this.thead = this.table.find('thead');
	this.tbody = this.table.find('tbody');
	
	// pagination buttons
	this.nextButton = this.container.find(".data-table-buttons .next");
	this.prevButton = this.container.find(".data-table-buttons .prev");
	this.nextButton.hide();
	this.prevButton.hide();
	
	// export button
	this.csvExportButton = this.container.find(".csv-export");
	this.csvExportButton.hide();

	// data provider
	this.dataProvider = null;
	
	/*
	 * used for table settings
	 */
	this.rows = 20; // max number of rows
	this.offset = 0; // starting point for querying
	this.totalItems = -1; // total items to process
	
	this._init();
};

DataTable.prototype = (function(){
	
	// init function
	var init = function() {
		var $this = this;
		
		//event handlers
		this.nextButton.click(function(e){
			e.preventDefault();
			$this.offset = $this.offset += $this.rows;
			$.proxy(updateData , $this)();
		});
		this.prevButton.click(function(e){
			e.preventDefault();
			$this.offset = $this.offset -= $this.rows;
			$.proxy(updateData , $this)();
		});
		this.csvExportButton.click(function(e) {
			e.preventDefault();
			$this.dataProvider.exportToCsv();
		});
	};
	
	// set data provider and reset data table
	var setDataProvider = function(dataProvider) {
		this.dataProvider = dataProvider;
		$.proxy(reset, this)();
	};
	
	// private function that reset the data-table to its original state 
	var reset = function() {
		// reset counters
		this.offset = 0;
		this.totalItems = -1;
		
		//reset table results
		this.thead.empty();
		this.tbody.empty();
	};
	
	// hide
	var hide = function() {
		this.container.hide();
		this.csvExportButton.hide();
		this.nextButton.hide();
		this.prevButton.hide();
	};
	
	// show
	var show =  function() {
		this.container.fadeIn();
		if ( this.dataProvider && this.dataProvider.exportEnabled ) {
			this.csvExportButton.show();
		} else {
			this.csvExportButton.hide();
		}
		this.nextButton.show();
		this.prevButton.show();
		
		// in case it's still empty, gets the data
		if( this.totalItems < 0 ) {
			$.proxy(start, this)();
		}
	};
	
	// private function that starts showing the data
	var start = function() {
		var $this = this;

		// count total items 
		this.dataProvider.count(function(cnt){
			// set total items
			$this.totalItems = cnt;
			
			//update table headers and data
			$.proxy(updateHeaders, $this)();
			$.proxy(updateData, $this)();
		});
	};
	
	//update table headers
	var updateHeaders = function() {
		var tr = $("<tr></tr>");
		tr.hide();
		this.thead.append(tr);
		var th = $("<th></th>");
		th.html("Row #");
		tr.append(th);
		$.each(this.dataProvider.variables, function(i,field) {
			var th = $("<th></th>");
			th.html(field);
			tr.append(th);
		});
		tr.fadeIn(100);
	};
	
	//refresh the table by getting data from the current offset
	var updateData = function() {
		UI.disableAll();
		var $this = this;
		// get next data
		this.dataProvider.data( $this.offset , $this.rows , false , null, function(response) {			
			$.proxy(updateTbody , $this)(response);
			// and enable ui
			UI.enableAll();
			$.proxy(updateButtons , $this)();
		});
	};
	
	// disable / enable prev/next buttons
	var updateButtons = function(){
		if(this.offset == 0){
			UI.disable(this.prevButton);
		} else {
			UI.enable(this.prevButton);
		}
		if( (this.offset+this.rows) >= this.totalItems){
			UI.disable(this.nextButton);
		} else {
			UI.enable(this.nextButton);
		}
	};
	
	var formatNumber = d3.format(".4n");
	
	// update html table with data given as parameter
	var updateTbody = function(data) {
		var $this = this;
		//empty table before showing results
		this.tbody.empty();
		
		//update paging text
		var paging = (this.offset+1) + " - " + (this.offset + data.length) + " of " + this.totalItems;
		this.dataTablePagination.html( paging );
		
		// show items
		var delay = 0;
		var rowNum = this.offset + 1;
		// add rows to table
		$.each(data, function(i,record) {
			var $tr = $("<tr></tr>");
			$tr.hide();
			$this.tbody.append($tr);
			var $td = $("<td></td>");
			$td.html((rowNum++));
			$tr.append($td);
			
			var variables = $this.dataProvider.variables;
			$.each(variables, function(j, variable){
				var value = record.fields[variable];
				// format only numbers with decimal points
				var field = ( typeof value === "number" && value % 1 !== 0 ) ? formatNumber( value ) : value;
				var $td = $( "<td></td>" );
				$td.html(field);
				$tr.append($td);
			});
			setTimeout(function() {
				$tr.fadeIn(100);
			}, (delay += 50) );
			
			$tr.click( function(e){
				var scrollTop = $tr.offset().top;
				$this.tbody.stop().animate({scrollTop: scrollTop}, 800, "easeOutQuart");
			});
		});
		
	};
	
	return {
		
		constructor : DataTable,
		
		_init : init,
		
		show : show ,

		hide : hide ,
		
		setDataProvider : setDataProvider
	};

})();