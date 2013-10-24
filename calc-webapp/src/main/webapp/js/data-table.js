/**
 * Data table
 */

function DataTable($container) {
	/*
	 * ui components
	 */
	this.container = $container;
	this.dataTablePagination = this.container.find(".data-table-pagination");
	this.tableResults = this.container.find(".table-results");
	/*
	 * used for table settings
	 */
	this.offset = 0;
	this.rows = 50;
	this.max = 0;
	this.entity = null;
	this.fields = null;

}