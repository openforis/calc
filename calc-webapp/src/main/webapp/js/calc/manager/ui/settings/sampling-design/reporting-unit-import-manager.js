/**
 * manager for Areas of interest page
 * @author Mino Togna
 */

ReportingUnitImportManager = function( container , importCallback, response ) {
	
	this.container = $(container);
	this.importCallback = importCallback;
	
	// aoi import section

	// levels to import
	this.levelsSection = this.container.find(".levels");
	this.levelSection = this.container.find(".level");
	
	this.importBtn = this.container.find( "button[name=import-btn]" );
	this.importBtn.click( $.proxy(function(e){
		this.import(); 
	}, this) );
	
	this.init( response );
};


ReportingUnitImportManager.prototype.init = function(response) {
//	var $this = this;
	
	this.filepath = response.fields.filepath;
	
	this.levelsSection.empty();
	
//	var levels = 0;
	var headers = response.fields.headers;
	var completed = false;
	
	var hasStrata 	=  headers[ headers.length - 1 ] == 'stratum_area';
	var levels 		= ( headers.length - 1 ) / 2;
	levels = ( hasStrata === true ) ? levels -1 : levels;

	for( var i=0; i<levels; i++ ){
		var levelNo = i+1;
		
		if( headers[i*2] != 'level_'+levelNo+'_code' ||  headers[i*2+1] != 'level_'+levelNo+'_label'){
			Calc.error( {}, "CSV file is not compatible" );
			$this.importBtn.disable();
		}
		// last level
		if( levelNo == levels && !hasStrata){
			if( headers[ headers.length - 1 ] != 'level_'+levelNo+'_area'){
				Calc.error( {}, "CSV file is not compatible" );
				$this.importBtn.disable();
			}
		}
		
		//append level section
		var l = this.levelSection.clone();
		var input = l.find("input[type=text]");
		input.attr( "name","level" );
		input.attr( "value","Level " + levelNo );
		this.levelsSection.append(l);
		l.show();
	}
	
	if( hasStrata ){
		if( headers[headers.length-3] != 'stratum_code' ||  headers[headers.length-2] != 'stratum_label' || headers[headers.length-1] != 'stratum_area'){
			Calc.error( {}, "CSV file is not compatible" );
			this.importBtn.disable();
		}
		
		var l = this.levelSection.clone();
		var input = l.find("input[type=text]");
		input.attr( "name","strata" );
		input.attr( "value","Stratum" );
		input.prop('disabled', true);
		
		this.levelsSection.append(l);
		l.show();
	}
	
	this.levelsToImport = levels;
	this.hasStrata		= hasStrata;
	
};

ReportingUnitImportManager.prototype.import = function(  ) {
	var $this = this;
	
	var s = $this.levelsSection.find("input[type=text]");
	var captions = [];
	$.each(s, function(i,e){
		var caption = $(e).val();
		if( StringUtils.isBlank(caption) ) {
			UI.showError( "Caption not valid for level " + (i+1) , true );
			return;
		}
		captions.push( caption );
	});
	
	WorkspaceManager.getInstance().activeWorkspaceImportAoi($this.filepath, $this.levelsToImport , $this.hasStrata, captions, function(ws){
		Utils.applyFunction( $this.importCallback );
	});
	
};

