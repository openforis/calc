/**
 * manager for Areas of interest page
 * @author Mino Togna
 */

ReportingUnitViewDataManager = function(container) {
	
	this.container = $( container );
	
	// aoi section
//	this.aoiSection = this.container.find(".aoi-section");
	

	// aoi tree section (it contains the svg that shows the aoi tree structure)
	
	
	//form file uplaod manager (to be initialized in the init method)
	this.formFileUpload = null;
	
//	this.init();
};

ReportingUnitViewDataManager.prototype.reset = function(){
	if( this.aoiTreeSection ){
		this.aoiTreeSection.hide();
		this.aoiTreeSection.remove();
	}
	this.aoiTreeSection = null;
};


ReportingUnitViewDataManager.prototype.show = function() {
	var show = false;
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		if( ws.aoiHierarchies && ws.aoiHierarchies.length > 0 ) {
			show = true;
			
			this.aoiTreeSection = $( '<div class="aoi-tree-section"></div>' );
			this.container.append( this.aoiTreeSection );
//			this.aoiTreeSection = this.container.find( ".aoi-tree-section" );
			this.aoiTreeSection.css( "width", this.container.width() + "px" );
			this.aoiTreeSection.css( "height", this.container.height() + "px" );
//			this.aoiTreeSection.css( "width", Math.floor( this.container.width()*(10/12) )+"px" );
//			this.aoiTreeSection.css( "height", Math.floor( this.container.height()*0.79 )+"px" );
			this.aoiTreeSection.attr("id","aoi-tree-svg");
			
			this.updateAoiTree(ws);
		} 
		
	},this) );
	
	return show;
};


ReportingUnitViewDataManager.prototype.initSvg = function(){
	var padding = 10,
		width = this.aoiTreeSection.width()  - padding,
    	height = this.aoiTreeSection.height()  - padding;
	
	this.diameter = Math.min(width, height);
	this.format = d3.format("0,000");
	
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

ReportingUnitViewDataManager.prototype.updateAoiTree = function(ws) {
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
