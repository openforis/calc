/**
 * manager for Areas of interest page
 * @author Mino Togna
 */

AoiManager = function(container) {
	
	this.container = $(container);
	
	// aoi section
	this.aoiSection = this.container.find(".aoi-section");
	
	// upload csv ui components
	this.uploadFormSection = this.aoiSection.find(".upload-form-section");
	this.uploadProgressSection = this.aoiSection.find(".upload-progress-section");
	
	// aoi import section
	this.aoiImportSection = this.container.find(".aoi-import-section");
	this.aoiImportSection.hide();
	// levels to import
	this.levelsSection = this.aoiImportSection.find(".levels");
	this.levelSection = this.container.find(".level");
	
	this.importBtn = this.aoiImportSection.find( "[name=import-btn]" );
	
	
	// aoi tree section (it contains the svg that shows the aoi tree structure)
	this.aoiTreeSection = this.aoiSection.find( ".aoi-tree-section" );
	this.aoiTreeSection.css( "width", Math.floor( this.container.width()*(10/12) )+"px" );
	this.aoiTreeSection.css( "height", Math.floor( this.container.height()*0.79 )+"px" );
	this.aoiTreeSection.attr("id","aoi-tree-svg");
	
	//form file uplaod manager (to be initialized in the init method)
	this.formFileUpload = null;
	
	this.init();
};

AoiManager.prototype.init = function(){
	var $this = this;

	//form file upload manager
	this.formFileUpload = new FormFileUpload(this.uploadFormSection, this.uploadProgressSection, $.proxy($this.showImport, $this));
	
	this.importBtn.click( function(e){ $this.import(); } );
	
	// update aoi tree
//	$this.initSvg();
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.updateAoiTree(ws);
	});
};

AoiManager.prototype.showImport = function(response) {
//	var $this = this;
	
	this.filepath = response.fields.filepath;
	
	this.aoiSection.hide(0);
	this.levelsSection.empty();
	this.aoiImportSection.fadeIn();
	
	var levels = 0;
	var headers = response.fields.headers;
	var completed = false;
	
	while( !completed ) {
		//TODO check that headers have right name
		var headerArea = headers[(levels+1)*2];
		if( headerArea === 'level_' + (levels+1) + '_area' ) {
			completed = true;
		}
		
		//append level section
		var l = this.levelSection.clone();
		var input = l.find("input[type=text]");
		input.attr( "name","level" );
		input.attr( "value","Level " +(levels+1) );
		this.levelsSection.append(l);
		l.show();
		
		levels += 1;
	}
};

AoiManager.prototype.import = function() {
	var $this = this;
	
	var s = $this.levelsSection.find("input[type=text]");
	var captions = [];
	$.each(s, function(i,e){
		var caption = $(e).val();
		if(caption=="") {
			UI.showError( "Caption not valid for level " + (i+1) , true );
			return;
		}
		captions.push( caption );
	});
	
	WorkspaceManager.getInstance().activeWorkspaceImportAoi($this.filepath, captions, function(ws){
		$this.aoiImportSection.hide(0);
		$this.aoiSection.fadeIn();
		
		$this.updateAoiTree(ws);
	});
	
};

AoiManager.prototype.initSvg = function(){
	var padding = 10,
		width = this.aoiTreeSection.width()  - padding,
    	height = this.aoiTreeSection.height()  - padding;
	
	this.diameter = Math.min(width, height);
	this.format = d3.format(",d");
	
	this.pack = d3.layout.pack()
    .size([this.diameter - padding, this.diameter])
    .value(function(d) { 
    	return d.landArea; 
	});

	this.aoiTreeSection.empty();
	this.svg = d3
	.select( "#"+this.aoiTreeSection.attr("id") )
	.append("svg")
    .attr("width", this.diameter)
    .attr("height", this.diameter)
    .append("g")
    .attr("transform", "translate(0,0)");

};

AoiManager.prototype.updateAoiTree = function(ws) {
	var $this = this;
	this.aoiTreeSection.empty();
	if( ws.aoiHierarchies && ws.aoiHierarchies.length > 0 ) {
		this.initSvg();
		
		var root = ws.aoiHierarchies[0].rootAoi;

		this.nodes = 
			this.svg	
			.selectAll(".node")
			.data(this.pack.nodes(root))
			;
		
		this.nodes.enter()
			.append("g")
			.attr("class", function(d) { 
				return d.children ? "pack-node" : "pack-node-leaf pack-node"; 
			})
			.attr("transform", function(d) { 
				return "translate(" + d.x + "," + d.y + ")"; 
			});

		this.nodes.append("title")
			.text(function(d) { 
				return d.caption + " (" + $this.format(d.landArea) + ") " ; 
			});
		
		this.nodes.append("circle")
			.attr("r", 0)
			.transition()
			.delay(function(d,i){
				return d.depth * 200;
			})
			.duration(400)
			.ease( "out-in" )
			.attr("r", function(d) { 
				return d.r; 
			})
			.styleTween("opacity", function() { return d3.interpolate(0, 1); })
			;

		this.nodes
//		.filter(function(d) { 
//			return !d.children; 
//		})
		.append("text")		
		.attr("transform", function(d) {
			var y = (d.children) ? - d.r : 0;
			y += (d.depth == 0) ? 20:0;
			return "translate(" + 0 + "," + y + ")";
		})
//		.attr("dy", function(d){
//			var dy = (d.children) ? "-5em" : ".3em";
//			return dy;
//		}) 
//				".3em")
		.style("text-anchor", "middle")
		.style("opacity","0")
		.style("font-size",function(d,i){
			return ( 1 - (d.depth*0.125) ) + "em";
		})
		.text(function(d) {
			return d.caption;
		})
		.transition()
		.delay(function(d,i){
			return (d.depth+1) * 200;
		})
		.ease( "out-in" )
		.styleTween( "opacity", function() { return d3.interpolate(0, 1); } )
		;

		d3.select(self.frameElement).style("height", this.diameter + "px");
		
		this.nodes.on("click", function(d){
//			console.log(d);
		});
		
	} else {
		// empty tree
//		this.aoiTreeSection.empty();
		
	}
};
