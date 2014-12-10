/**
 * Tree layout (implemented using d3)
 * 
 * @author Mino Togna
 */

Tree = function(container) {
	this.container = $(container);

	this.container.attr( 'id', 'tree_' + $.now() );
};

Tree.prototype.hide = function() {
	this.container.hide();
};

Tree.prototype.show = function() {
	this.container.fadeIn();
};

Tree.prototype.init = function(node) {
	this.container.empty();

	var width 	= this.container.width();
	var height 	= this.container.height();

	var m = [ 20, 120, 20, 120 ], 
		w = width - m[1] - m[3], 
		h = height - m[0] - m[2], 
		i = 0, 
		root;

	var tree = d3
				.layout
				.tree()
				.size( [ h, w ] );

	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	var vis = d3
				.select( '#'+this.container.attr('id') )
				.append( "svg" )
				.attr( "class" , "tree-layout" )
				.attr( "width", w + m[1] + m[3] )
				.attr( "height", h + m[0] + m[2] )
				.append( "svg:g" )				
				.attr( "transform" , "translate(" + m[3] + "," + m[0] + ")" );
	
	

	var update = function(source) {
		var duration = d3.event && d3.event.altKey ? 5000 : 500;

		// Compute the new tree layout.
		var nodes = tree.nodes( root ).reverse();

		// Normalize for fixed-depth.
		nodes.forEach(function(d) {
			d.y = d.depth * 180;
		});

		// Update the nodes…
		var node = vis.selectAll("g.node").data(nodes, function(d) {
			return d.id || (d.id = ++i);
		});

		// Enter any new nodes at the parent's previous position.
		var nodeEnter = node
						.enter()
						.append("g")
						.attr( "class", "node" )
						.attr("transform", function(d) {
							return "translate(" + source.y0 + "," + source.x0 + ")";
						}).on("click", function(d) {
							toggle(d);
							update(d);
						});
		
		nodeEnter
			.append( "circle" )
			.attr( "r", "6" )
			.attr( "class" , function(d){
				var cssClass = ( d.children && d.children.length > 0 ) || (d._children && d._children.length > 0) ? "node" : "leaf";
				return cssClass;
			});

		nodeEnter.append("svg:text").attr("x", function(d) {
			return d.children || d._children ? -10 : 10;
		}).attr("dy", ".35em").attr("text-anchor", function(d) {
			return d.children || d._children ? "end" : "start";
		}).text(function(d) {
			return d.name;
		});

		// Transition nodes to their new position.
		var nodeUpdate = node.transition().duration(duration).attr("transform",
				function(d) {
					return "translate(" + d.y + "," + d.x + ")";
				});

		nodeUpdate
			.select("circle")
			.attr( "r", 8 )
			.attr( "class" , function(d){
				var cssClass = ( d.children && d.children.length > 0 ) || (d._children && d._children.length > 0) ? "node" : "leaf";
				return cssClass;
			});
		nodeUpdate
		.select("text")
		.attr( "class" , function(d){
			var cssClass = ( d.children && d.children.length > 0 ) || (d._children && d._children.length > 0) ? "node" : "leaf";
			return cssClass;
		});


		// Transition exiting nodes to the parent's new position.
		var nodeExit = node.exit().transition().duration(duration).ease( "out" ).attr(
				"transform", function(d) {
					return "translate(" + source.y + "," + source.x + ")";
				}).remove();

		nodeExit.select("circle").attr("r", 1e-6);

		nodeExit.select("text").style("fill-opacity", 1e-6);

		// Update the links…
		var link = vis.selectAll("path.link").data(tree.links(nodes),
				function(d) {
					return d.target.id;
				});

		// Enter any new links at the parent's previous position.
		link.enter().insert("svg:path", "g").attr("class", "link").attr("d",
				function(d) {
					var o = {
						x : source.x0,
						y : source.y0
					};
					return diagonal({
						source : o,
						target : o
					});
				}).transition().duration(duration).attr("d", diagonal);

		// Transition links to their new position.
		link.transition().duration(duration).attr("d", diagonal);

		// Transition exiting nodes to the parent's new position.
		link.exit().transition().duration(duration).attr("d", function(d) {
			var o = {
				x : source.x,
				y : source.y
			};
			return diagonal({
				source : o,
				target : o
			});
		}).remove();

		// Stash the old positions for transition.
		nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		});
	};

	// Toggle children.
	var toggle = function (d) {
		if (d.children) {
			d._children = d.children;
			d.children = null;
		} else {
			d.children = d._children;
			d._children = null;
		}
	};
	
	
	 var root = node;
	  root.x0 = h / 2;
	  root.y0 = 0;

	  var toggleAll = function( d ){
	    if (d.children) {
	      d.children.forEach(toggleAll);
	      toggle(d);
	    }
	  };

	  root.children.forEach(toggleAll);

	  update(root);
	
};