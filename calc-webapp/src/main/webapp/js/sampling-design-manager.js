/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container) {
	
	this.container = container;
	
	//sampling desing select/combo. WTF!
	this.samplingUnitSelect = this.container.find('[name=sampling-unit]');
	this.samplingUnitCombo = this.samplingUnitSelect.combobox().data('combobox');
	
	this.samplingUnitSection = this.container.find(".sampling-unit-section");
	this.entitiesSection = this.samplingUnitSection.find(".entities");
	this.variablesSection = this.samplingUnitSection.find(".variables");
	this.variableSection = this.samplingUnitSection.find(".variable");
	
	this.workspaceManager = null; 
	//WorkspaceManager.getInstance();
	
	
	//init page
	this._init();
	
	
};

SamplingDesignManager.prototype = (function(){
	
	var init = function() {
		var $this = this;
		this.workspaceManager = WorkspaceManager.getInstance();

		//get the active workspace
		$this.workspaceManager.activeWorkspace( $.proxy(function(ws) {
			
			//refresh sampling design select. 
			//TODO Ask Stefano why to use the combo box always two objects are used. wouldn't make more sense having one object to wrap or a better incapsulation anyway???
			UI.Form.populateSelect($this.samplingUnitSelect, ws.entities, 'id','name');
			$this.samplingUnitCombo.refresh();
			
			
			//if sampling design is defined for active workspace update ui
			if(ws.samplingDesign){
				var sd = ws.samplingDesign;
				if(sd.samplingUnitId){
					var entity = ws.getEntityById(sd.samplingUnitId);
					if(entity){
						$this.samplingUnitCombo.selectValue(entity.id);
						$.proxy(samplingUnitUpdate , $this)(entity);
					}
				}
			}
			/**
			 * event handlers
			 */
			//when sampling unit changes, save it and shows entities
			$this.samplingUnitSelect.change( function(e){
				var entityId = $this.samplingUnitSelect.val();
				var entity = ws.getEntityById(entityId);
				$.proxy(samplingUnitChange , $this)(entity);
				
			});
			
		}) , this );
	};

	/**
	 * Handler for samplingUnit comobo change event
	 */
	var samplingUnitChange = function(entity) {
		var $this = this;
		if(entity) {
			UI.lock();
			$this.workspaceManager.activeWorkspaceSetSamplingUnit( entity, function(ws){
				UI.unlock();
				$.proxy(samplingUnitUpdate , $this)(entity);
			});
		}
	};

	/**
	 * update sampling unit ui
	 */
	var samplingUnitUpdate = function(entity) {
		var $this = this;
		if(entity) {
			$this.workspaceManager.activeWorkspace( $.proxy(function(ws) {
				
				var entities  = ws.getAggregableEntities(entity);
				$.proxy(entitiesUpdate , this)(entities);
				
			} , $this) ); 
				
//			});
		}
	};

	/**
	 * update entities section
	 */
	var entitiesUpdate = function(entities) {
		var $this = this;
		//empty entity and variable sections
		$this.entitiesSection.empty();
		$this.variablesSection.empty();
		$this.variableSection.empty();
		//
		//show  entities
		//
		var t = 80;
		$.each(entities, function(i, entity){
			var btn = $('<button type="button" class="btn default-btn"></button>');
			btn.hide();
			btn.html(entity.name);
			btn.click( function(e) {
				//disable current entity button
				UI.enable( $this.entitiesSection.find("button") );
				UI.disable($(e.currentTarget));
				
				//empty variables section
				$this.variablesSection.empty();
				$this.variableSection.empty();
				
				//set current entity
				$this.currentEntity = entity;
				//
				//show variables
				//
				$.proxy(variablesUpdate , $this)(entity.quantitativeVariables);
				
			});
			$this.entitiesSection.append(btn);
			
			setTimeout(function(){
				btn.fadeIn();
			},t);
			
			t+=15;
		});
	};
	
	/**
	 * update variables section
	 */
	var variablesUpdate = function(vars) {
		var $this = this;
		var x = 80;
		$.each(vars, function(i, variable) {
			var btn = $('<button type="button" class="btn default-btn"></button>');
			btn.hide();
			btn.html(variable.name);
			btn.click( function(e) {
				//disable current variable button
				UI.enable( $this.variablesSection.find("button") );
				UI.disable( $(e.currentTarget) );
				
				//empty variable section
				$this.variableSection.empty();
				
				// update variable section
				$.proxy(variableUpdate , $this)(variable.id);
			});
			$this.variablesSection.append(btn);
			
			setTimeout(function(){
				btn.fadeIn();
			},x);
			
			x+=15;
		});
	};
	
	/**
	 * update variable section 
	 */
	var variableUpdate = function(variableId) {
		var $this = this;
		$this.workspaceManager.activeWorkspace( $.proxy(function(ws) {
			
			var variable = ws.getQuantitativeVariableById($this.currentEntity.id, variableId);
			$this.variableSection.hide();
			
			//update variable section header
			var name = $('<div class="name">Varaible name</div>');
//		var name = $('<button type="button" class="btn default-btn" disabled></button>');
			name.html(variable.name);
			$this.variableSection.append(name);
			
			//update aggregates section
			$.proxy(variableAggregatesUpdate , $this)(variable, $this.variableSection);
			
			//show variable section
			$this.variableSection.fadeIn();
		} , $this) );
	};

	/**
	 * update variable aggregates section
	 */
	var variableAggregatesUpdate = function(variable, section) {
		var $this = this;
		
		//aggregates section ui
		var aggsSection = $('<div class="aggregates"></div');
		section.append(aggsSection);
		
		//iterates over the aggregate types allowed for the variable and add a checkbox 
		var x = 80;
		var aggs = variable.aggregateTypes;
		$.each(aggs , function(i,agg) {
			var row = $('<div class="row no-margin"></div>');
			aggsSection.append(row);

			// get button for variable aggregate
			var btn = $.proxy(getAggregateButton, $this)(variable, agg);
			row.append(btn);
			
			//show button
			setTimeout(function(){
				btn.fadeIn();
			}, x);
			x += 25;
			
//			var div = $('<div class="checkbox"></div>');
//			aggsSection.append(div);
//			var label = $("<label></label>");
//			div.append(label);
//			var checkbox = $('<input type="checkbox">');
//			label.append(checkbox);
//			label.append(agg);
//			console.log(agg);
		});
	};
	
	/**
	 * returns the button for the variable aggregate
	 */
	var getAggregateButton = function(variable , agg) {
		var $this = this;
		
		var varAgg = $.proxy(getVariableAggregate , $this)(variable , agg);
		var btnClass = (varAgg) ? "option-btn-selected" : "option-btn";
		var btn = $('<button type="button" class="btn '+btnClass+'"></button>');
		btn.hide();
		btn.html(agg);
		
		//bind click event
		btn.click(function(e) {
			//disable enbabled elements
			var enabledElements = $(document).find(":enabled");
			UI.disable(enabledElements);
			
			if(varAgg) { 
				//click to delete the aggregate for the variable
				$this.workspaceManager.activeWorkspaceDeleteVariableAggregate($this.currentEntity, variable, agg, function(variableUpdate) {
					//after creation replace button
					var btnUpdate = $.proxy(getAggregateButton, $this)(variableUpdate, agg);
					btnUpdate.show();
					btn.replaceWith( btnUpdate );
					//re enable elements
					UI.enable(enabledElements);
				});
			} else {	
				//click to create the aggregate for the variable
				$this.workspaceManager.activeWorkspaceCreateVariableAggregate($this.currentEntity, variable, agg, function(variableUpdate) {
					//after creation replace button
					var btnUpdate = $.proxy(getAggregateButton, $this)(variableUpdate, agg);
					btnUpdate.show();
					btn.replaceWith( btnUpdate );
					//re enable elements
					UI.enable(enabledElements);
				});
			}
		});
		
		return btn;
	};
	
	/**
	 * Utility method to extract the variable aggregate for the given agg name passed as parameter
	 */
	var getVariableAggregate = function(variable , agg) {
		var varAgg = null;
		$.each(variable.aggregates, function(i, vAgg) {
			if(vAgg.aggregateType == agg) {
				varAgg = vAgg;
				return false;
			}
		});
		return varAgg;
	};
	
	return {
		constructor : SamplingDesignManager
		,
		_init : init
	};
}) ();