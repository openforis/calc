/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container) {
	
	this.container = container;
	
	// sampling unit combo
	this.samplingUnitCombo =  this.container.find('[name=sampling-unit]').combobox();

	//sampling design buttons
	this.srsBtn = new OptionButton( this.container.find('[name=srs]') );
	this.systematicBtn = new OptionButton( this.container.find('[name=systematic]') );
	this.stratifiedBtn = new OptionButton( this.container.find('[name=stratified]') );
	this.clusterBtn = new OptionButton( this.container.find('[name=cluster]') );
	this.twoPhasesBtn = new OptionButton( this.container.find('[name=twoPhases]') );
	
	// save btn
	this.saveBtn = this.container.find("[name=save-btn]");
	
	// inatance variable that contains all sampling design settings
	this.samplingDesign = {};
	
	this.init();
};

SamplingDesignManager.prototype.init = function(){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		//refresh sampling unit select.
		$this.samplingUnitCombo.data(ws.entities, 'id','name');
		
		//if sampling design is defined for active workspace update ui
		if(ws.samplingDesign) {
			var sd = ws.samplingDesign;
			$this.setSamplingDesign(sd);
		} else {
			$this.disableButtons();
		}
		
	});
	
	
	
	// bind event handlers
	// sampling unit change
	this.samplingUnitCombo.change( $.proxy(function(e){

		e.preventDefault();
		this.samplingDesign.samplingUnitId = $this.samplingUnitCombo.val();
		if ( ! this.samplingDesign.samplingUnitId ){
			this.disableButtons();
		}
		
	} , this) );
	
	
	// simple random sampling
	this.srsBtn.select( $.proxy(function(){
		this.samplingDesign.srs = true;
		this.samplingDesign.systematic = false;
		this.systematicBtn.deselect();
	}, this) );	
	this.srsBtn.deselect( $.proxy(function(){
		this.samplingDesign.srs = false;
	}, this) );
	// systematic
	this.systematicBtn.select( $.proxy(function(){
		this.samplingDesign.srs = false;
		this.samplingDesign.systematic = true;
		this.srsBtn.deselect();
	}, this) );	
	this.systematicBtn.deselect( $.proxy(function(){
		this.samplingDesign.systematic = false;
	}, this) );
	//stratified
	this.stratifiedBtn.select( $.proxy(function(){
		this.samplingDesign.stratified = true;
	}, this) );
	this.stratifiedBtn.deselect( $.proxy(function(){
		this.samplingDesign.stratified = false;
	}, this) );	
	// cluster
	this.clusterBtn.select( $.proxy(function(){
		this.samplingDesign.cluster = true;
	}, this) );	
	this.systematicBtn.deselect( $.proxy(function(){
		this.samplingDesign.cluster = false;
	}, this) );
	//2 phases
	this.twoPhasesBtn.select( $.proxy(function(){
		this.samplingDesign.twoPhases = true;
	}, this) );	
	this.twoPhasesBtn.deselect( $.proxy(function(){
		this.samplingDesign.twoPhases = false;
	}, this) );
	
	this.saveBtn.click( $.proxy( $this.saveSamplingDesign, this ) );
};

SamplingDesignManager.prototype.setSamplingDesign = function(sd){
	
	WorkspaceManager.getInstance().activeWorkspace($.proxy(function(ws){
		this.samplingDesign = sd;
//		console.log(this);
		if(sd.samplingUnitId){
			var entity = ws.getEntityById(sd.samplingUnitId);
			if(entity){
				this.samplingUnitCombo.val(entity.id);
			}
		}
	} , this) );
};

SamplingDesignManager.prototype.disableButtons = function(){
	this.srsBtn.disable();
	this.systematicBtn.disable();
	this.stratifiedBtn.disable();
	this.clusterBtn.disable();
	this.twoPhasesBtn.disable();
//	UI.disable( this.saveBtn );
};

SamplingDesignManager.prototype.enableButtons = function(){
	this.srsBtn.enable();
	this.systematicBtn.enable();
	this.stratifiedBtn.enable();
	this.clusterBtn.enable();
	this.twoPhasesBtn.enable();
//	UI.disable( this.saveBtn );
};

SamplingDesignManager.prototype.saveSamplingDesign = function(){
	console.log("save");
	if( this.samplingDesign.samplingUnitId ){
		// validate
		WorkspaceManager.getInstance().activeWorkspaceSetSamplingDesign( this.samplingDesign, $.proxy( function(ws){
			console.log(ws);
		} , this) );
		
	} else {
		
	}
};