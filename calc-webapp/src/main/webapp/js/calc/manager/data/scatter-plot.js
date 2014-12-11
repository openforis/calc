/**
 * @author Mino Togna 
 */

function ScatterPlot(container) {
	// ui container
	this.container = container;
	
	// options section
	var optionsSection = this.container.find('.options');	
//	this.xCombo = optionsSection.find('[name=x]').combobox();
//	this.yCombo = optionsSection.find('[name=y]').combobox();	
	this.refreshBtn = optionsSection.find('[name=refresh]');
	
	// progress for data loading
	this.progressBar = new ProgressBar( optionsSection.find(".progress") , optionsSection.find(".percent") );
	// hide for now progress bar. temporary
	optionsSection.find(".progress,.percent").hide();
	//chart container
	this.chartContainer = this.container.find(".chart");
	this.chartContainer.attr( 'id', 'scatter-chart-' + new Date().getTime() );
//	this.chart = null;
	
	// data provider
	this.dataProvider = null;
	
	this.xVariable = null;
	this.yVariable = null;
	this.offset = 0;
	this.totalItems = -1;
	
	this.workspaceManager = WorkspaceManager.getInstance();

	this._init();

};

ScatterPlot.prototype = (function(){
	
	// init
	var init = function() {
		var $this = this;
		//event handlers
		//change x and y variables
//		$this.xCombo.change(function(e) {
//			e.preventDefault();
//			$this.xVariable = $this.xCombo.val();
//		});
//		$this.yCombo.change(function(e){
//			e.preventDefault();
//			$this.yVariable = $this.yCombo.val();
//		});
		
		//refresh chart button
		$this.refreshBtn.click(function(e){
			e.preventDefault();
			if( $this.xVariable == null || $this.yVariable == null ){
				UI.showError( "x and y must be set", true );
			} else {
				$.proxy(start , $this)();
			}
		});
	};
	
	// set data provider and reset svg
	var setDataProvider = function(dataProvider) {
		this.dataProvider = dataProvider;
		$.proxy(reset, this)();
	};
	
	var reset = function() {
		var $this = this;
		// reset counters
		this.offset = 0;
		this.totalItems = -1;
		
		this.xVariable = null;
		this.yVariable = null;
		
		// populate input combos
//		this.workspaceManager.activeWorkspace(function(ws) {
//			var entity = ws.getEntityById($this.dataProvider.entityId);
//			var variables = entity.quantitativeVariables();
			
//			$this.xCombo.data(variables, 'name', 'name');
//			$this.yCombo.data(variables, 'name', 'name');
//		});
		
		// reset options
//		this.xCombo.val("");
//		this.yCombo.val("");
		
		// reset chart
//		$.proxy(resetChart, this)();
	};
	
	// show / hide functions
	var show = function() {
		if( this.dataProvider ){
		
			this.container.fadeIn();
			
			if( !this.svg ){
				$.proxy(initSvg, this)();
			}
			// if it hasn't started yet and there are 2 variables to show, then it starts
			if ( this.dataProvider.variables.length == 2 && this.totalItems < 0) {
				// update combo boxes
				var xVar = this.dataProvider.variables[0];
				var yVar = this.dataProvider.variables[1];
				this.xVariable = xVar;
				this.yVariable = yVar;
//				this.xCombo.val(xVar);
//				this.yCombo.val(yVar);
				
				$.proxy(start, this)();
			}
		}
	};
	
	var hide = function(){
		this.container.hide();
	};
	
	/**
	 * Initialize the svg element
	 */
	var initSvg = function(){
		if( !this.svg ){
			this.padding = 50;
			var w = this.chartContainer.width();
			var h = this.chartContainer.height() - 10;
			
			var dataset = [[50,50]];
			
			//Create SVG element
			this.svg = d3.select( "#" + this.chartContainer.attr('id') )
						.append("svg")
						.attr("width", w)
						.attr("height", h);
			
			// create axis 
			$.proxy(createAxis, this)(dataset);

			this.svg.append("g")
				.attr("class", "axis xaxis")
				.attr("transform", "translate(0," + (h - this.padding) + ")")
				.call( this.xAxis.ticks(0) );
			this.svg.append("g")
				.attr("class", "axis yaxis")
				.attr("transform", "translate(" + this.padding + ", 0)")
				.call( this.yAxis.ticks(0) );
			
			// reference to the circles added to the scatter plot
			this.circles = null;
		}
	};
	
	// private function that starts showing the data
	var start = function() {
		var $this = this;
		UI.disableAll();
		this.offset = 0;
		
		if( this.xVariable != null && this.yVariable != null ) {
			var vars = [this.xVariable, this.yVariable];
			this.dataProvider.data( $this.offset , 1000 , null , vars, function(response) {
				$.proxy(updateChart, $this)( response );
				//get next data if not all data have been loaded
//				if( $this.offset < $this.totalItems ){
//					$.proxy(getNextData, $this)();
//				}
				UI.enableAll();
			});
		}
		// count total items 
//		this.dataProvider.count(function(cnt){
			// reset chart
//			$.proxy(resetChart, $this)();
			
			// set total items
//			$this.totalItems = cnt;
						
			// reset progress bar
//			$this.progressBar.reset();
			// get data and update chart
//			$.proxy(getData, this)();
//		});
	};
	
	// get next available data from data provider and add them to the chart
	var getData = function() {
		var $this = this;
		if( this.xVariable != null && this.yVariable != null ) {
			var vars = [this.xVariable, this.yVariable];
			this.dataProvider.data( $this.offset , 1000 , null , vars, function(response) {
				$.proxy(updateChart, $this)(response);
				//get next data if not all data have been loaded
//				if( $this.offset < $this.totalItems ){
//					$.proxy(getNextData, $this)();
//				}
			});
		}
	};

	// highcharts 0.32ms
	//d3: 0.039 ms
	/**
	 * Update chart with the given dataset
	 */
	var updateChart = function(data) {
		var $this = this;
//		var start = $.now();
//		console.log("Start : " + start);
		var dataset = [];
		// prepare chart data
		$.each(data, function(i,record){
			// get x and y values from record.fields 
			var seriesItem = [ record.fields[$this.xVariable] , record.fields[$this.yVariable] ] ;
			dataset.push( seriesItem );
		});
		
		
		$.proxy(createAxis, this)(dataset, true);	
		
		//Create circles and enter the data
		this.circles = this.svg
		.selectAll("circle")
		.data(dataset);
		
		this.circles.enter()
		.append("circle")
		.attr("class", "scatter-plot-point")
		.attr("cx", -1)
		.attr("cy", -1)
		.attr("r", "8");

		// exit and remove old elements
        this.circles.exit()
            .attr("class", "exit")
            .transition(750)
            .ease("out")
            .style("opacity", 0.2)
            .remove();

        // position the bubbles
        var $this = this;	
		this.circles
		.transition()
			.delay(function(d,i){
				return i + 200;
			})
			.duration(400)
			.ease( "circle" )
			.attr( "cx", Math.floor( this.svg.attr("width")/2) - this.padding )
			.attr( "cy", Math.floor( this.svg.attr("height")/2) - this.padding )
			.styleTween("opacity", function() { return d3.interpolate(0, .4); })
		.transition()
			.delay(function(d,i){
				return i + 600;
			})
			.duration(400)
			.ease("elastic")
			.attr("cy", function(d,i) {
				return $this.yScale(d[1]);
			})
			.attr("cx", function(d,i) {
				return $this.xScale(d[0]);
			})
		;
		
		// on mouse over a bubble shows data values
		var mouseOver = function() { 
			var circle = d3.select(this);

			if( ! circle.classed("opened") ) {
				closeBubbles();

				var data = circle.data()[0];

				// transition to increase size/opacity of bubble
				circle.transition()
					.duration(600)
					.style("opacity", 1)
					.attr("r", 14)
					.ease("elastic");
				
				// append lines to bubbles that will be used to show the precise data points.
				var y1 = parseFloat(circle.attr("cy")) + 26;
				y1 = Math.min( y1 , $this.svg.attr("height")-$this.padding );
				$this.svg.append("g")
					.attr("class", "guide")
				.append("line")
					.attr("x1", circle.attr("cx"))
					.attr("x2", circle.attr("cx"))
					.attr("y1", y1 )
					.attr("y2", $this.svg.attr("height") - $this.padding)
					.style("stroke", circle.style("fill"))
					.style("opacity", 0)
					.transition().delay(50).duration(300).styleTween("opacity", 
								function() { return d3.interpolate(0, .4); });

				var x1 = parseFloat(circle.attr("cx")) - 26;
				x1 =  Math.max(x1, $this.padding);
				$this.svg.append("g")
					.attr("class", "guide")
				.append("line")
					.attr("x1", x1)
					.attr("x2", $this.padding)
					.attr("y1", circle.attr("cy"))
					.attr("y2", circle.attr("cy"))
					.style("stroke", circle.style("fill"))
					.style("opacity", 0)
					.transition().delay(50).duration(300).styleTween("opacity", 
								function() { return d3.interpolate(0, .4); });
				
				// set low opacity to axis
				d3
				.selectAll(".axis,.axis-label")
				.transition()
				.duration(400)
				.styleTween("opacity", function() { return d3.interpolate(.7, .2); });
				
				var format = d3.format(".3n");
				// 	show point data values on the axis
				$this.svg.append("g")
					.attr("class", "axis-tick")
				.append("text")
					.attr("opacity",0)
					.attr("x",circle.attr("cx"))
					.attr("y", $this.svg.attr("height") - ($this.padding/2) )
					.text( format(data[0]) )
					.transition()
					.duration(400)
					.styleTween("opacity", function() { return d3.interpolate(0, .7); });

				$this.svg.append("g")
				.attr("class", "axis-tick")
				.append("text")
				.attr("opacity",0)
				.attr("x",$this.padding/2)
				.attr("y",circle.attr("cy"))
				.text( format(data[1]) )
				.transition()
				.duration(400)
				.styleTween("opacity", function() { return d3.interpolate(0, .7); });
				
			// function to move mouseover item to front of SVG stage, in case
			// another bubble overlaps it
				d3.selection.prototype.moveToFront = function() { 
				  return this.each(function() { 
					this.parentNode.appendChild(this); 
				  }); 
				};

				if( circle.moveToFront ){
					circle.moveToFront();
				}
				
				//add 'opened' class
				circle.classed("opened", true);
			}
			
			
		};
		
		// on mouse out remove data values
		var mouseOut = function() {
			closeBubbles();
		};
		
		/**
		 * Closes the specified circle and restore it's style to the default one.
		 */
		var closeBubbles = function() {
			var circle = d3.selectAll(".opened");
			
			// go back to original size and opacity
			circle.transition()
			.duration(800).style("opacity", .4)
			.attr("r", 8).ease("elastic");
			
			// increase opacity to axis
			d3
			.selectAll(".axis,.axis-label")
			.transition()
			.duration(400)
			.styleTween("opacity", function() { return d3.interpolate(.2, .7); });
			
			// fade out data points
			d3
			.selectAll(".axis-tick")
			.transition()
			.duration(100)
			.styleTween("opacity", function() { return d3.interpolate(.7, 0); })
			.remove();
			
			// fade out guide lines, then remove them
			d3
			.selectAll(".guide,.scatter-point-legend")
			.transition()
			.duration(100)
			.styleTween("opacity", function() { return d3.interpolate(.5, 0); })
			.remove();
			
			//remove 'opened' class
			circle.classed("opened", false);
			
			var opened = d3.selectAll(".opened");
			
			opened.transition()
				.duration(800).style("opacity", .4)
				.attr("r", 8).ease("elastic");
			
			//remove "opened" class
			circle.classed("opened", false);
		};
		
		// run the mouseon/out functions
		this.circles.on("mouseover", mouseOver);
		this.circles.on("mouseout", mouseOut);
		
		// create x / y labels
		this.svg
		.append("text")
		.attr("class", "axis-label")
		.attr("text-anchor", "end")
		.attr("x", this.svg.attr("width") - 150)
		.attr("y", this.svg.attr("height") - this.padding - 5)
		.text( this.xVariable );

		this.svg
		.append("text")
		.attr("class", "axis-label")
		.attr("text-anchor", "end")
		.attr("x", -(this.padding))
		.attr("y", this.padding + 10)
		.attr("dy", ".75em")
		.attr("transform", "rotate(-90)")
		.text( this.yVariable );
		
		
		// update offset
		$this.offset += data.length;
		// update progress
//		$this.progressBar.update($this.offset , $this.totalItems);
//		var end = ( $.now() - start );
//		console.log("End : " + end + " msSeconds");
//		console.log("End : " +  end/1000 + " seconds");
	};
	/**
	 * Private function to create axis. it also creates x/y scales. 
	 * if param update === true it updates the ui as well
	 */
	var createAxis = function(dataset, update) {
		var w = this.svg.attr("width") , 
			h = this.svg.attr("height");

		this.xScale = 
			d3.scale.linear()
			.domain([0, d3.max(dataset, function(d) { return d[0]; })])
			.range([this.padding, w - this.padding]);

		this.yScale = d3.scale.linear()
		 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
		 .range([h - this.padding, this.padding]);
		//not used now
//		var rScale = d3.scale.linear()
//							 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
//							 .range([3, 5]);
		this.xAxis = d3.svg.axis()
		  .scale(this.xScale)
		  .orient("bottom")
		  .ticks(10);
		this.yAxis = d3.svg.axis()
		  .scale(this.yScale)
		  .orient("left")
		  .ticks(10);
		
		//remove labels
		this.svg
		.selectAll(".axis-label")
		.transition()
		.duration(400)
		.attr("y",-5)
		.remove();
		
		if( update === true) {
			this.svg.select(".yaxis")
//		.attr("transform", "translate(" + ( this.padding) + ", 0)")
			.transition().duration(500).ease("in-out").call(this.yAxis);
			
			this.svg.select(".xaxis")
//		.attr("transform", "translate(0," + (h  - this.padding) + ")")
			.transition().duration(500).ease("in-out").call(this.xAxis);
		}
	};
	
	return {
		constructor : ScatterPlot,
		
		_init : init,
		
		setDataProvider : setDataProvider,
		
		show : show ,
		
		hide : hide
		
	};
})();
