/**
 * manager for aggregation settings
 * 
 * @author Mino Togna
 */

AggregationManager = function( container ) {
    
    this.container = container;
    
    this.samplingUnitCombo = this.container.find('[name=sampling-unit]').combobox();
    
    // sampling unit UI elements
    // this.samplingUnitSection = this.container.find(".sampling-unit-section");
    this.entitiesSection = this.container.find(".entities");
    
    // aggregate settings ui sections (each entity has their own aggregate settings)
    // not used anymore. TODO remove ununsed code
    this.aggregateSettingsSection 	= this.container.find(".aggregate-settings-section");
    this.plotAreaSection 			= this.container.find(".plot-area-section");
    this.variablesSection 			= this.container.find(".variables");
    this.variableSection 			= this.container.find(".variable");
    this.variablePerHaSection 		= this.container.find(".variable-per-ha");
    
    // r script
    this.rScriptInput 	= this.container.find("[name=plot-area]");
    this.rScript 		= new RScript( this.rScriptInput );
    
    // save button
    this.saveBtn 	=	this.container.find( 'button[name=save-btn]' );
    
    this.init();
};

// AggregationManager.prototype = (function(){

AggregationManager.prototype.init = function() {
    var $this = this;
    
    WorkspaceManager.getInstance().activeWorkspace( function( ws ) {
	
	
    	// if sampling design is defined for active workspace update ui
    	if ( ws.samplingDesign ){
			var sd = ws.samplingDesign;
		    if ( sd.samplingUnitId ){
		    	var entity = ws.getEntityById(sd.samplingUnitId);
		    	if( entity ){
		    		$this.samplingUnitUpdate(entity.id);
		    	}
		    }
		}
    	
    	$this.saveBtn.click(function(e){
    		e.preventDefault();
    		
    		UI.lock();
    	    var entityId = $this.currentEntity.id;
    	    
    	    WorkspaceManager.getInstance().activeWorkspaceSetEntityPlotArea( entityId, $this.rScriptInput.val(), function( ws ) {
    	    		$this.currentEntity = ws.getEntityById(entityId);
    	    		UI.unlock();
    	    });
    	});
	
		// refresh sampling design select.
		// $this.samplingUnitCombo.data(ws.entities, 'id','name');
	// when sampling unit changes, save it and shows entities	
	// $this.samplingUnitCombo.change( function(e){
	// var entityId = $this.samplingUnitCombo.val();
	// $.proxy(samplingUnitChange , $this)(entityId);
	// });
	
    } );
};

/**
 * Handler for samplingUnit combo change event
 */
//AggregationManager.prototype.samplingUnitChange = function( entityId ) {
//    var $this = this;
//    $.proxy(emptyAllSections, $this)();
//    
//    UI.lock();
//    WorkspaceManager.getInstance().activeWorkspace(function( ws ) {
//	var entity = ws.getEntityById(entityId);
//	
//	WorkspaceManager.getInstance().activeWorkspaceSetSamplingUnit(entity, function( ws ) {
//	    UI.unlock();
//	    $.proxy(samplingUnitUpdate, $this)(entityId);
//	});
//    });
//};

/**
 * update sampling unit ui
 */
AggregationManager.prototype.samplingUnitUpdate = function( entityId ) {
    if (entityId) {
	WorkspaceManager.getInstance().activeWorkspace($.proxy(function( ws ) {
	    var entities = ws.getAggregableEntities(entityId);
	    this.entitiesUpdate(entities);
	}, this));
    }
};

AggregationManager.prototype.emptyAllSections = function() {
    var $this = this;
    $this.entitiesSection.empty();
    $this.variablesSection.empty();
    $this.variableSection.empty();
    $this.variablePerHaSection.empty();
    $this.aggregateSettingsSection.hide();
};

/**
 * update entities section
 */
AggregationManager.prototype.entitiesUpdate = function( entities ) {
    var $this = this;
    // empty entity and variable sections
    // $this.entitiesSection.empty();
    // $this.variablesSection.empty();
    // $this.variableSection.empty();
    // $this.variablePerHaSection.empty();
    
    // add header to entities section
    $this.entitiesSection.append($('<div class="name">Entity</div>'));
    //
    // show entities
    //
    var t = 80;
    $.each(entities, function( i , ent ) {
	var btn = $('<button type="button" class="btn default-btn"></button>');
	btn.hide();
	btn.html(ent.name);
	
	btn.click(function( e ) {
	    WorkspaceManager.getInstance().activeWorkspace(function( ws ) {
		var entity = ws.getEntityById(ent.id);
		// disable current entity button
		UI.enable($this.entitiesSection.find("button"));
		UI.disable($(e.currentTarget));
		
		// empty variables section
		$this.variablesSection.empty();
		$this.variableSection.empty();
		$this.variablePerHaSection.empty();
		
		// set current entity
		$this.currentEntity = entity;
		// update rScript with current entity
		$this.rScript.entity = entity;
		$this.rScriptInput.val(entity.plotAreaScript);
		// show plot area section
		$this.plotAreaSection.show();
		
		// show variables
//		$.proxy(variablesUpdate, $this)(entity.quantitativeVariables);
		
	    });
	});
	
	$this.entitiesSection.append(btn);
	
	setTimeout(function() {
	    btn.fadeIn();
	}, t);
	
	t += 15;
    });
};




/*
 * ==========================================
 * variable aggregation section not used now
 * ==========================================
 */
/**
 * update variables section
 */
AggregationManager.prototype.variablesUpdate = function( vars ) {
    var $this = this;
    // add header
    $this.variablesSection.append($('<div class="name">Variable</div>'));
    
    var x = 80;
    $.each(vars, function( i , variable ) {
	var btn = $('<button type="button" class="btn default-btn"></button>');
	btn.hide();
	btn.html(variable.name);
	btn.click(function( e ) {
	    // disable current variable button
	    UI.enable($this.variablesSection.find("button"));
	    UI.disable($(e.currentTarget));
	    
	    // empty variable section
	    $this.variableSection.empty();
	    $this.variablePerHaSection.empty();
	    
	    // update variable section
	    $.proxy(variableUpdate, $this)(variable.id);
	});
	$this.variablesSection.append(btn);
	
	setTimeout(function() {
	    btn.fadeIn();
	}, x);
	
	x += 15;
    });
};

/**
 * update variable section
 */
AggregationManager.prototype.variableUpdate = function( variableId ) {
    var $this = this;
    WorkspaceManager.getInstance().activeWorkspace($.proxy(function( ws ) {
	
	// empty variable-per-ha section
	$this.variablePerHaSection.empty();
	
	var variable = $this.currentEntity.getQuantitativeVariableById(variableId);
	// $this.variableSection.hide();
	
	// update variable section header
	var name = $('<div class="name"></div>');
	name.html(variable.name);
	name.hide();
	$this.variableSection.append(name);
	name.fadeIn();
	
	// update aggregates section
	$.proxy(variableAggregatesUpdate, $this)(variable, $this.variableSection);
	// update variable-per-ha section
	$.proxy(variablePerHaUpdate, $this)(variableId);
	
    }, $this));
};

/**
 * update variable aggregates section
 */
AggregationManager.prototype.variableAggregatesUpdate = function( variable , section ) {
    var $this = this;
    
    // aggregates section ui
    var aggsSection = $('<div class="aggregates"></div');
    section.append(aggsSection);
    
    // iterates over the aggregate types allowed for the variable and add a
    // button
    var x = 80;
    var aggs = variable.aggregateTypes;
    $.each(aggs, function( i , agg ) {
	var row = $('<div class="row no-margin"></div>');
	aggsSection.append(row);
	
	var perHa = section.hasClass('variable-per-ha') ? true : false;
	// get button for variable aggregate
	var btn = $.proxy(getAggregateButton, $this)(variable, agg, perHa);
	row.append(btn);
	
	// show button
	setTimeout(function() {
	    btn.fadeIn();
	}, x);
	x += 25;
	
	// var div = $('<div class="checkbox"></div>');
	// aggsSection.append(div);
	// var label = $("<label></label>");
	// div.append(label);
	// var checkbox = $('<input type="checkbox">');
	// label.append(checkbox);
	// label.append(agg);
	// console.log(agg);
    });
};

/**
 * returns the button for the variable aggregate
 * 
 * @param perHa
 *                if updating the per-ha variable aggregates section
 */
AggregationManager.prototype.getAggregateButton = function( variable , agg , perHa ) {
    var $this = this;
    
    var varAgg = $.proxy(getVariableAggregate, $this)(variable, agg);
    var btnClass = (varAgg) ? "option-btn-selected" : "option-btn";
    var btn = $('<button type="button" class="btn ' + btnClass + '"></button>');
    btn.html(agg);
    btn.hide();
    
    // bind click event
    btn.click(function( e ) {
	// disable enbabled elements
	UI.disableAll();
	
	var updateBtn = function( variableUpdate ) {
	    // after update replace button
	    var btnUpdate = $.proxy(getAggregateButton, $this)(variableUpdate, agg, perHa);
	    btnUpdate.show();
	    // replace old button
	    btn.replaceWith(btnUpdate);
	    // re enable elements
	    UI.enableAll();
	};
	
	if (varAgg) {
	    // click to delete the aggregate for the variable
	    WorkspaceManager.getInstance().activeWorkspaceDeleteVariableAggregate($this.currentEntity, variable, agg, function( variableUpdate ) {
		if (perHa == true) {
		    variableUpdate = variableUpdate.variablePerHa;
		}
		updateBtn(variableUpdate);
	    });
	} else {
	    // click to create the aggregate for the variable
	    WorkspaceManager.getInstance().activeWorkspaceCreateVariableAggregate($this.currentEntity, variable, agg, function( variableUpdate ) {
		if (perHa == true) {
		    variableUpdate = variableUpdate.variablePerHa;
		}
		updateBtn(variableUpdate);
	    });
	}
    });
    
    return btn;
};

/**
 * update variable-per-ha section
 */
AggregationManager.prototype.variablePerHaUpdate = function( variableId ) {
    var $this = this;
    WorkspaceManager.getInstance().activeWorkspace($.proxy(function( ws ) {
	var variable = $this.currentEntity.getQuantitativeVariableById(variableId);
	var variablePerHa = variable.variablePerHa;
	
	// append btn to add/remove variable-per-ha
	var headerBtn = $.proxy(getVariablePerHaHeaderBtn, $this)(variable);
	$this.variablePerHaSection.append(headerBtn);
	
	// if variable-per-ha is set, it updates the aggregates section
	if (variablePerHa) {
	    $.proxy(variableAggregatesUpdate, $this)(variable.variablePerHa, $this.variablePerHaSection);
	}
	
    }, $this));
};

AggregationManager.prototype.getVariablePerHaHeaderBtn = function( variable ) {
    var $this = this;
    
    var variablePerHa = variable.variablePerHa;
    
    var cssClass = (variablePerHa) ? "option-btn-selected" : "option-btn";
    var btn = $('<button type="button" class="btn ' + cssClass + ' header-btn"></button>');
    btn.html(variable.name + " / per ha");
    
    btn.click(function( e ) {
	UI.disableAll();
	
	var afterClick = function( variable , updateAggregates ) {
	    // replace btn
	    var btnUpdate = $.proxy(getVariablePerHaHeaderBtn, $this)(variable);
	    btn.replaceWith(btnUpdate);
	    // remove aggregates section
	    var aggregatesSection = $this.variablePerHaSection.find('.aggregates');
	    aggregatesSection.remove();
	    
	    if (updateAggregates == true) {
		$.proxy(variableAggregatesUpdate, $this)(variable.variablePerHa, $this.variablePerHaSection);
	    }
	    
	    UI.enableAll();
	};
	
	if (variablePerHa) {
	    WorkspaceManager.getInstance().activeWorkspaceDeleteVariablePerHa($this.currentEntity.id, variable.id, function( variable ) {
		afterClick(variable);
	    });
	} else {
	    WorkspaceManager.getInstance().activeWorkspaceAddVariablePerHa($this.currentEntity.id, variable.id, function( variable ) {
		afterClick(variable, true);
	    });
	}
    });
    
    return btn;
};

/**
 * Utility method to extract the variable aggregate for the agg name for the
 * variable (passed as parameters)
 */
AggregationManager.prototype.getVariableAggregate = function( variable , agg ) {
    var varAgg = null;
    $.each(variable.aggregates, function( i , vAgg ) {
	if (vAgg.aggregateType == agg) {
	    varAgg = vAgg;
	    return false;
	}
    });
    return varAgg;
};

// return {
// constructor : AggregationManager
// ,
// _init : init
// };
// }) ();
