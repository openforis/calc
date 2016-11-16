/**
 * @author M. Togna
 */

AuxiliaryTablesViewManager = function( container , manager ) {
	this.container 	= $( container );
	this.manager 	= manager;
	this.BASE_URI 	= this.manager.BASE_URI;
	
	this.listSection 		= this.container.find( '.list-section' );
	
	this.dataTableSection 	= this.container.find( '.data-table-section' ).show();
	this.dataTable			= new DataTable(  this.dataTableSection.find(".data-table") );
	this.dataTable.hide();
	this.dataProvider		= null;
	
	this.init();
}

AuxiliaryTablesViewManager.prototype.init = function(){
	
}

AuxiliaryTablesViewManager.prototype.show = function(reloadTables){
	this.container.fadeIn();
	if( reloadTables === true ){
		this.updateList();
	}
}

AuxiliaryTablesViewManager.prototype.hide = function(){
	this.container.hide();
}

AuxiliaryTablesViewManager.prototype.updateList = function(){
	this.listSection.empty();
	this.dataTable.hide();
	
	this.manager.activeTable 	= null;
	this.activeButton 			= null;
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace( function(ws) {
		$.each( ws.auxiliaryTables , function( i , table ){
			
			var addButton = function( ){
				var div = $( '<div class="row no-margin no-padding width100"></div>')
				div.hide();
				$this.listSection.append( div );
				
				var btn = $( '<button class="btn option-btn col-md-10"></button>' );
				btn.html( table.name );
				div.append( btn );
				
				var deleteBtn = $( '<button class="btn no-background col-md-2"><i class="fa fa-minus-square"></i></button>' );
				div.append( deleteBtn );
				
				deleteBtn.click( function(){

					var position = deleteBtn.offset();
					position.top -= 20; 
					position.left -= 20;
					UI.showConfirm( 
							"Do you want to delete " + table.name + " table? This operation cannot be undone." , 
							function(){
								var params = {
										url		: $this.BASE_URI + '/' + table.id + "/delete.json",
										success	:function(response) {
											
											WorkspaceManager.getInstance().activeWorkspace( function(ws) {
												if( response.status == "OK" ){
													UI.showSuccess( "Auxiliary table succesfully deleted" , true );
													ws.auxiliaryTables = response.fields.auxiliaryTables;
													$this.updateList();
												} else {
													UI.showError( response.fields.error , true );
												}
											});
										}
								}
								
								EventBus.dispatch("ajax.post", null, params);
							} , 
							null,
							position
							)
				});
				
				
				setTimeout( function(){
					div.fadeIn();
				} , i * 75);
				
				
				var optionBtn = new OptionButton( btn );
				
				var select = function( t ) {
					
					// unselect last selection 
					if( $this.activeButton ) {
						$this.activeButton.deselect();
					}
					
					$this.activeButton 			= optionBtn;					
					$this.manager.activeTable 	= t;
					
					$this.dataProvider 			= new TableDataProvider(t.schema, t.name);
					$this.dataTable.setDataProvider( $this.dataProvider );
					$this.dataTable.show();
//					UI.lock();
//					var tbody = $this.equationsTable.find("tbody");
//					tbody.empty();
//					$this.equationsTable.show();
					
//					$.ajax({
//						url			: $this.BASE_URI + list.id + "/equations.json" ,
//						method		: "GET" ,
//						dataType	: "json"
//					}).done( function(response) {
//						UI.unlock();
//						
//						var equations = response.fields.equations;
//						$.each( equations , function( j , equation) {
//							var tr = $( "<tr style='font-size:0.9em'></tr>" );
//							tr.hide();
//							tbody.append( tr );
//							
//							var td = $( "<td></td>" );
//							td.html( equation.code );
//							tr.append( td );
//							
//							td = $( "<td></td>" );
//							td.html( equation.equation );
//							tr.append( td );
//							
//							td = $( "<td></td>" );
//							td.html( equation.condition );
//							tr.append( td );
//							
//							setTimeout( function(){
//								tr.fadeIn();
//							} , j * 75);
//							
//							$this.selectedListButton = optionBtn;
//							$this.selectedList 		= list; 
//						});
//					}).error(function(){
//						Calc.error.apply( this, arguments );
//					})
				};
				optionBtn.select( select , table );
				
				var deselect = function( e ) {
//					$this.equationsTable.hide(0);
					$this.dataTable.hide();
					$this.dataProvider 			= null;
					
					$this.activeButton 			= null;					
					$this.manager.activeTable 	= null;
				};
				optionBtn.deselect( deselect , table );
			}
			
			addButton();
		});
		
	});
}