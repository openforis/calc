/**
 * Manger for error calculation section
 * 
 *  @author Mino Togna
 */

ErrorCalculationManager = function( container ) {
	
	this.container = $( container );
	
	// UI Form components
	this.aoi		= this.container.find( ".aoi" );
	this.aoiSelect	= this.aoi.find( "select" ).combobox();;
	this.quantity	= this.container.find( ".quantity" );
	this.category	= this.container.find( ".category" );
	this.classes	= this.container.find( ".classes" );
	this.viewBtn 	= this.container.find( ".view-btn button" );
	
	// UI result components
	this.result 		= this.container.find( ".result" );
	this.resultTable 	= this.result.find( "table" );
	
	// form parameters
	this.params 			= {};
	this.params.aoi 		= null;
	this.params.quantity 	= null;
	this.params.category 	= null;
	this.params.classes 	= null;
	
	this.init();
};

ErrorCalculationManager.prototype.init = function() {
	this.resetUi();
	this.bindEvents();
	
	var $this = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		var aois = ws.getAdminUnitFlatAois();
		if( aois.length >0 ) { 
			// init aoi
			$this.aoiSelect.data( aois, 'id','caption' );
		} else {
			UI.showError( "Unable to calculate the error. Make sure you have a valid processing chain defined and executed"  , true );
		}
		
	});
}; 

ErrorCalculationManager.prototype.bindEvents = function() {
	var $this = this;
	
	this.aoiSelect.change( function(e) {
		var aoi = $this.aoiSelect.val();
		if( aoi && aoi !== "" ){
			$this.showQuantity();
			$this.params.aoi = parseInt( aoi ); 
		} else {
			$this.params.aoi 		= null;
			$this.params.quantity 	= null;
			$this.params.category 	= null;
			$this.params.classes 	= null;
			
			$this.quantity.hide();
			$this.category.hide();
			$this.classes.hide();
			$this.viewBtn.hide();
			
			$this.result.hide();
		}
	});
	
	this.viewBtn.click( function(e){
		var params = JSON.stringify( $this.params );
		UI.lock();
		$.ajax({
			url		: "rest/error/execute.json",
			dataType: "json",
			data	: { "params" : params }
		}).done( function(response) {
			$this.showResults( response );
			UI.unlock();
		}).error( function() {
			UI.unlock();
			Calc.error.apply( this , arguments );
		});
	});
};

ErrorCalculationManager.prototype.resetUi = function() {
	
	this.quantity.hide();
	this.category.hide();
	this.classes.hide();
	this.viewBtn.hide();
	
	this.result.hide();
	
};

ErrorCalculationManager.prototype.showQuantity = function() {
	var $this = this;
	$this.quantity.find("div.option").remove();
	var container = $( '<div class="height95 option" style="overflow: auto;"></div>' );
	$this.quantity.append( container );
	UI.lock();
	
	var vars = [];
	var divs = [];
	var btns = [];
	CalculationStepManager.getInstance().loadAll( function(steps) {
		WorkspaceManager.getInstance().activeWorkspace( function(ws) {
			for( var i in steps ) {
				var step = steps[i];
				var variableId = step.outputVariableId;
				var variable = ws.getVariableById( variableId );
				if( vars.indexOf(variableId) < 0 && variable.type == "QUANTITATIVE") {
					
					var initVariableButton = function(){
						var variable = ws.getVariableById( variableId ); 
						
						var div = $( '<div class="option width100" style="padding-bottom: 2px"></div>' );
						div.data( "variable" , variable );
						var btn = $( '<button class="btn option-btn width100"></button>' );
						btn.html( variable.name );
						div.append( btn );
						divs[variableId] = div;
						container.append( div );
						
						var optBtn = new OptionButton( btn );
						btns[variableId] = optBtn;
						var selectFunction = function( vId ) {
							// first deselect others
							for( var j in btns ){
								if( parseInt(j) != vId ) {
									var b = btns[j];
									b.deselect();
								}
							}
							
							
							$this.params.quantity 	= vId;
							$this.params.category 	= null;
							$this.params.classes 	= null;
							
							$this.showCategory( vId );
						};
						
						var deselectFunction = function( vId ) {
							$this.params.quantity 	= null;
							$this.params.category 	= null;
							$this.params.classes 	= null;
							
							$this.category.hide();
							$this.classes.hide();
							$this.viewBtn.hide();
							
							$this.result.hide();
						};
						
						optBtn.select( selectFunction , variableId );
						optBtn.deselect( deselectFunction , variableId );
						vars.push( variableId );
					};
					initVariableButton();
					
				}
			}
			UI.unlock();
			$this.quantity.fadeIn();
		});
	});
}; 


ErrorCalculationManager.prototype.showCategory = function( vId ) {
	var $this = this;
	$this.category.find("div.option").remove();
	var container = $( '<div class="height95 option" style="overflow: auto;"></div>' );
	$this.category.append( container );
	
	WorkspaceManager.getInstance().activeWorkspace( function(ws) {
		var v = ws.getVariableById( vId );
		var entity 	= ws.getEntityById( v.entityId );
		
		var divs = [];
		var btns = [];
		while( entity ) {
			
			var vars = entity.categoricalVariables();
			for( i in vars ) {
				var variable = vars[i];
				var variableId = variable.id;
				
				var initBtn = function(){
					
					var div = $( '<div class="option width100" style="padding-bottom: 2px"></div>' );
					div.data( "variable" , variable );
					var btn = $( '<button class="btn option-btn width100"></button>' );
					btn.html( variable.name );
					div.append( btn );
					divs[variableId] = div;
					container.append( div );
					
					var optBtn = new OptionButton( btn );
					btns[variableId] = optBtn;
					var selectFunction = function( vId ) {
						// first deselect others
						for( var j in btns ){
							if( parseInt(j) != vId ) {
								var b = btns[j];
								b.deselect();
							}
						}
						
						$this.params.category 	= vId;
						$this.params.classes 	= null;
						
						$this.showClasses( vId );
					};
					
					var deselectFunction = function( vId ) {
						$this.params.category 	= null;
						$this.params.classes 	= null;
						
						$this.classes.hide();
						$this.viewBtn.hide();
						
						$this.result.hide();
					};
					
					optBtn.select( selectFunction , variableId );
					optBtn.deselect( deselectFunction , variableId );
				};
				initBtn();
			}
			
			
			
			entity = entity.parent();
		}
		$this.category.fadeIn();
	});
	
};

ErrorCalculationManager.prototype.showClasses = function( vId ) {
	UI.lock();
	var $this = this;
	this.params.classes 	= [];
	$this.classes.find("div.option").remove();
	var container = $( '<div class="height95 option" style="overflow: auto;"></div>' );
	$this.classes.append( container );
	
	VariableManager.getInstance().getCategories( vId , function(categories) {
		
		if( categories ) {
			
			for( var i in categories ){
				var category	= categories[ i ];
				
				var addButton = function() {
					var code 		= category.code;
					var caption 	= category.caption;
					
					var div = $( '<div class="float-left width25 option" style="padding: 0.1em 0.1em;"></div>');
					container.append( div );
					var btn = $( '<button class="btn option-btn width100">' );
					btn.html( code );
					div.append( btn );
					
					// enable caption tooltip
					btn.tooltip({ title: caption, delay: { show: 200, hide: 100 }});
					
					var optionBtn = new OptionButton( btn );
					
					optionBtn.select( function(c){
//						$this.values.push( code );
						$this.params.classes.push( c );
						if( $this.params.classes.length > 0 ){
							$this.viewBtn.fadeIn();
						} else {
							$this.viewBtn.fadeOut();
							$this.result.hide();
						}
					} , code );
					
					optionBtn.deselect( function(c) {
						var index = $this.params.classes.indexOf( c );

						if (index > -1) {
							$this.params.classes.splice(index, 1);
						}
						if( $this.params.classes.length > 0 ){
							$this.viewBtn.fadeIn();
						} else {
							$this.viewBtn.fadeOut();
							$this.result.hide();
						}
					} , code  );
					
				};
				
				addButton();
			}
			
			
			$this.classes.fadeIn();
		} else {
			UI.showError( "Too many classes to show for this category. Not yet implemented for such categories." , true );
		}
		
		UI.unlock();	
	});
	
};


ErrorCalculationManager.prototype.showResults = function( results ) {
	var thead = this.resultTable.find( "thead" ); 
	var tbody = this.resultTable.find( "tbody" );
	var formatNumber = d3.format(".4n");
	
	thead.empty();
	tbody.empty();
	
	var fieldNames = results[0].fieldNames;
	
	var tr = $( "<tr></tr>" );
	thead.append(tr);
	$.each( fieldNames , function(i,field) {
		var th = $( "<th></th>" );
		th.html( field );
		tr.append( th );
	});
	
	
	$.each( results, function( i , record ) {
		var tr = $( "<tr></tr>" );		
		tbody.append( tr );
		
		$.each( fieldNames, function( j , f ) {
			var value = record.fields[ f ];
			// format only numbers with decimal points
			var field = ( typeof value === "number" && value % 1 !== 0 ) ? formatNumber( value ) : value;
			var td = $( "<td></td>" );
			td.html( field );
			tr.append( td );
		});

		
	});
	
	this.result.fadeIn();
	
};