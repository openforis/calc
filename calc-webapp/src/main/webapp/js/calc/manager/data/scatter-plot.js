/**
 * @author Mino Togna 
 */

function ScatterPlot(container) {
	// ui container
	this.container = container;
	
	// options section
	var optionsSection = this.container.find('.options');	
	this.xCombo = optionsSection.find('[name=x]').combobox();
	this.yCombo = optionsSection.find('[name=y]').combobox();	
	this.refreshBtn = optionsSection.find('[name=refresh]');
	
	// progress for data loading
	this.progressBar = new ProgressBar( optionsSection.find(".progress") , optionsSection.find(".percent") );
	// hide for now progress bar. temporary
	optionsSection.find(".progress,.percent").hide();
	//chart container
	this.chartContainer = this.container.find(".chart");
//	this.chartContainer = this.container.find(".chart").replaceWith( $("<div id='ssss'></div") );
	this.chartContainer.attr('id', 'scatter-chart');
	this.chart = null;
	
	// data provider
	this.dataProvider = null;
	
	this.xVariable = null;
	this.yVariable = null;
	this.offset = 0;
	this.totalItems = -1;
	
	// TODO move out
	this.chartinfo = {
		    chart: {
		    	renderTo: this.chartContainer.attr("id"),
		    	type: 'scatter',
                zoomType: 'xy',
                borderWidth: 0,
                borderRadius: 0,
                backgroundColor: null,
		    },
		    credits: {
		    	enabled:false
	    	},
		    title: {
		        text: '',
		        style: {
		        	color: '#496d7e',
		        	font: '1.2em Trebuchet MS, Verdana, sans-serif'
		        }
		    },
		    xAxis: {
                title: {
                    enabled: true,
                    text: '',
    		        style: {
    		        	color: '#496d7e',
    		        	font: '1.4em Trebuchet MS, Verdana, sans-serif'
    		        }
                },
                lineWidth: 1,
                lineColor: "#ecf0f1",
                gridLineColor: "#ecf0f1",
                tickColor: "#ecf0f1",
                labels: {
                    style: {
                       color: '#ecf0f1',
                       font: '0.9em Trebuchet MS, Verdana, sans-serif'
                    }
                 },
                 events: {
                 	afterSetExtremes : function (e) {
                 	   
                 	    var url;
                 	    var currentExtremes = this.getExtremes();
                 	    var range = e.max - e.min;
                 	    console.log("after set extrems: ", currentExtremes);
//                 	    var chart = $('#container').highcharts();
//
//                 	    var min = 0;
//                 	    var max = 1.35e12;
//                 	    if(!isReset)
//                 	    {
//                 	        min = e.min;
//                 	        max = e.max;
//                 	    }
//                 	     chart.showLoading('Loading data from server...');
//                 	    $.getJSON('http://www.highcharts.com/samples/data/from-sql.php?start=' + Math.round(min) +
//                 	        '&end=' + Math.round(max) + '&callback=?', function (data) {
//
//                 	        chart.series[0].setData(data);
//                 	           
//                 	        chart.hideLoading();
//                 	            
//
//                 	    });

                 	},
                 	setExtremes: function (e) {
                 		console.log("setExtremes ", e);
                     
                     if (e.max == null || e.min == null) {
                        isReset = true;                            
                     }
                     else
                     {
                      isReset = false;   
                     }
                 }
             }
//                startOnTick: true,
//                endOnTick: true,
//                showLastLabel: true
            },
            yAxis: {
                title: {
                    text: '',
    		        style: {
    		        	color: '#496d7e',
    		        	font: '1.4em Trebuchet MS, Verdana, sans-serif'
    		        }
                }
            ,
//                lineWidth: 1,
//                lineColor: "#ecf0f1"
            	gridLineColor: "#ecf0f1",
            	labels: {
                    style: {
                       color: '#ecf0f1',
                       font: '0.9em Trebuchet MS, Verdana, sans-serif'
                    }
                 }
            },
            legend: {
                layout: 'vertical',
                align: 'left',
                verticalAlign: 'top',
                x: 100,
                y: 70,
                floating: true,
                backgroundColor: '#FFFFFF',
                borderWidth: 1
            },
            plotOptions: {
                scatter: {
                	color:"ecf0f1",
                    marker: {
                        radius: 5,
                        states: {
                            hover: {
                                enabled: true,
                                lineColor: 'rgb(100,100,100)'
                            }
                        }
                    },
                    states: {
                        hover: {
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    tooltip: {
//                        headerFormat: '<b>{series.name}</b><br>',
                        pointFormat: '{point.x} , {point.y}'
                    }
                }
            	,
            	series : {
            		turboThreshold : 0
            	}
            },
            series: [{
                name: '',
                color: 'rgba(108, 159, 183, .5)',
                data: []
            }]
		};
	
	this.workspaceManager = WorkspaceManager.getInstance();

	this._init();

};

ScatterPlot.prototype = (function(){
	
	// init
	var init = function() {
		var $this = this;
		//event handlers
		//change x and y variables
		$this.xCombo.change(function(e) {
			e.preventDefault();
			$this.xVariable = $this.xCombo.val();
		});
		$this.yCombo.change(function(e){
			e.preventDefault();
			$this.yVariable = $this.yCombo.val();
		});
		
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
	
	// set data provider and reset data table
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
		
		this.workspaceManager.activeWorkspace(function(ws) {
			var entity = ws.getEntityById($this.dataProvider.entityId);
			var variables = entity.quantitativeVariables;
			
			$this.xCombo.data(variables, 'name', 'name');
			$this.yCombo.data(variables, 'name', 'name');
		});
		
		// reset options
		this.xCombo.val("");
		this.yCombo.val("");
		
		// reset chart
		$.proxy(resetChart, this)();
	};
	
	// reset chart
	var resetChart = function(){
		if( this.chart ) {
			this.chart.destroy();
			this.chart = null;
		}
	};
	
	// show / hide functions
	var show = function() {
		this.container.fadeIn();

		// if it hasn't started yet and there are 2 variables to show, then it starts
		if ( this.dataProvider.variables.length == 2 && this.totalItems < 0) {
			// update combo boxes
			var xVar = this.dataProvider.variables[0];
			var yVar = this.dataProvider.variables[1];
			this.xVariable = xVar;
			this.yVariable = yVar;
			this.xCombo.val(xVar);
			this.yCombo.val(yVar);
			
			$.proxy(start, this)();
		}
	};
	
	var hide = function(){
		this.container.hide();
	};
	
	// private function that starts showing the data
	var start = function() {
		var $this = this;
		// count total items 
		this.dataProvider.count(function(cnt){
			// reset chart
			$.proxy(resetChart, $this)();
			
			// set total items
			$this.totalItems = cnt;
			$this.offset = 0;
			
			// create the chart
			$this.chartinfo.xAxis.title.text = this.xVariable;
			$this.chartinfo.yAxis.title.text = this.yVariable;
//			$this.chart = new Highcharts.Chart($this.chartinfo);
			// reset progress bar
			$this.progressBar.reset();
			// starts getting data next data
			$.proxy(getNextData, $this)();
		});
	};
	
	// get next available data from data provider and add them to the chart
	var getNextData = function() {
		var $this = this;
		if( this.xVariable != null && this.yVariable != null ) {
//			console.log("upd chart");
//			console.log(this);
			var vars = [this.xVariable, this.yVariable];
			this.dataProvider.data( $this.offset , 1000 , null , vars, function(response) {
				$.proxy(addPoints, $this)(response);

				//get next data if not all data have been loaded
//				if( $this.offset < $this.totalItems ){
//					$.proxy(getNextData, $this)();
//				}
			});
		}
	};
	
	// add points to the chart
	var addPoints = function(data){
		var start = $.now();
		console.log("Start : " + start);
		
		
		var $this = this;
		
		var series = $this.chartinfo.series[0];
		var seriesData = series.data;
		// prepare chart data
		$.each(data,function(i,record){
			// get x and y values from record.fields 
			var seriesItem = [ record.fields[$this.xVariable] , record.fields[$this.yVariable] ] ;
			
			seriesData.push( seriesItem );
			
			//disabled now 
//			$this.chart.series[0].addPoint(seriesItem, false);
//			if($this.chart.series[0].data.length % 10000 == 0) {
//				$this.chart.redraw();
//			}
		});
//		console.log($this.chart);
		// redraw chart
//		$this.chart.redraw();
		
//		$this.chart = new Highcharts.Chart($this.chartinfo);
		// highcharts 0.32ms
		
		$.proxy(d3test, $this)(seriesData);
		//d3: 0.039 ms
		
		
		
		// update offset
//		$this.offset = $this.chart.series[0].data.length;
		$this.offset += data.length;
		// update progress
		$this.progressBar.update($this.offset , $this.totalItems);
		
		
		
		var end = ( $.now() - start );
		console.log("End : " + end + " msSeconds");
		console.log("End : " +  end/1000 + " seconds");
	};
	
	var d3test = function(dataset) {
		console.log("==== start testing d3");
		
		
		var padding = 50;
		var w = this.chartContainer.width();
		var h = this.chartContainer.height() - 10;
		
		//Create scale functions
		var xScale = d3.scale.linear()
//							 .domain([0, xMax])
							 .domain([0, d3.max(dataset, function(d) { return d[0]; })])
							 .range([padding, w - padding]);

		var yScale = d3.scale.linear()
//							 .domain([0, yMax])
							 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
							 .range([h - padding, padding]);

		var rScale = d3.scale.linear()
							 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
							 .range([3, 5]);

		//Define X axis
		var xAxis = d3.svg.axis()
						  .scale(xScale)
						  .orient("bottom")
						  .ticks(10);

		//Define Y axis
		var yAxis = d3.svg.axis()
						  .scale(yScale)
						  .orient("left")
						  .ticks(10);
		
		//Create SVG element
		var svg = d3.select("#" + this.chartContainer.attr('id') )
					.append("svg")
					.attr("width", w)
					.attr("height", h);
		//Create circles
		var circles = svg.selectAll("circle")
		   .data(dataset)
		   .enter()
		   .append("circle")
		   .attr("class", "scatter-plot-point")
//		   .attr("cx", function(d) {
//		   		return xScale(d[0]);
//		   })
//		   .attr("cx", Math.floor(w/2) - padding)
//		   .attr("cy", Math.floor(h/2) - padding)
		   .attr("cx", -1)
		   .attr("cy", -1)
//		   .attr("cy", function(d) {
//		   		return yScale(d[1]);
//		   })
		   .attr("r", "8");
//		   .style("opacity",.4);
		
//		setTimeout(function(){
			
		var delay = 100;
		circles
//			.transition()
//			.delay(100)			
//			.attr("opacity", .4)
			.transition()
			.delay(function(d,i){
//				console.log( Math.floor(i/100) );				
//				return (delay+=300);
//				return ( Math.floor(i/100) * 250 );
				return i + 200;
			})
			.duration(400)
			.ease( "circle" )
			.attr( "cx", Math.floor(w/2) - padding )
			.attr( "cy", Math.floor(h/2) - padding )
			.styleTween("opacity", function() { return d3.interpolate(0, .4); })
			.transition()
			.delay(function(d,i){
//				console.log( Math.floor(i/100) );				
//				return (delay+=300);
//				return ( Math.floor(i/100) * 250 );
				return i + 600;
			})
			.duration(400)
			.ease("elastic")
			.attr("cy", function(d,i) {
				return yScale(d[1]);
			})
			.attr("cx", function(d,i) {
				return xScale(d[0]);
			})
			;
//		}, 1000);
//			.styleTween("cy", function(d) { return d3.interpolate(-10, yScale(d[1])); } );
//		.transition()
//		   .delay(function(d,i){
//				return (delay+=500);
//			})
//			.duration(100)
//			.style("opacity", .4)
//		   .ease("elastic");
			
//		   .attr("r", function(d) {
//		   		return rScale(d[1]);
//		   });
		
		// what to do when we mouse over a bubble
		var mouseOn = function() { 
			var circle = d3.select(this);
			var data = circle.data();
			console.log(circle.data());
		// transition to increase size/opacity of bubble
			
			circle.transition()
				.duration(600)
				.style("opacity", 1)
				.attr("r", 14)
				.ease("elastic");
//
//			console.log(circle.attr("cx"));
//			console.log(circle.attr("cy"));
//			console.log( (circle.attr("cy") + 26) );
//			console.log("width:" +w);
//			console.log("h:" +h);
//			
			// append lines to bubbles that will be used to show the precise data points.
			var y1 = parseFloat(circle.attr("cy")) + 26;
			svg.append("g")
				.attr("class", "guide")
			.append("line")
				.attr("x1", circle.attr("cx"))
				.attr("x2", circle.attr("cx"))
				.attr("y1", Math.min( y1 , h-padding) )
//				.attr("y1", y1 )
//				.attr("y2", h - margin.t - margin.b)
				.attr("y2", h - padding)
//				.attr("transform", "translate(0," + (h - padding) + ")")
//				.attr("transform", "translate(40,20)")
				.style("stroke", circle.style("fill"))
				.style("opacity", 0)
				.transition().delay(50).duration(300).styleTween("opacity", 
							function() { return d3.interpolate(0, .4); });

			var x1 = parseFloat(circle.attr("cx")) - 26;
			svg.append("g")
				.attr("class", "guide")
			.append("line")
				.attr("x1", Math.max(x1, padding) )
//				.attr("x1", circle.attr("cx") )
				.attr("x2", padding)
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
//			.attr("opacity",.1);
			.styleTween("opacity", function() { return d3.interpolate(.7, .2); });
//			d3
//			.selectAll(".axis")
//			.transition()
//			.duration(400)
//			.attr("opacity",.1);
			
			var format = d3.format(".3n");
			// 	show point data values
			svg.append("g")
				.attr("class", "axis-tick")
				.append("text")
				.attr("opacity",0)
				.attr("x",circle.attr("cx"))
				.attr("y", h-(padding/2))
				.text( format(data[0][0]) )
				.transition()
				.duration(400)
//			.attr("opacity",.1);
				.styleTween("opacity", function() { return d3.interpolate(0, .7); });

			svg.append("g")
			.attr("class", "axis-tick")
			.style("text-anchor", "right")
			.append("text")
			.attr("opacity",0)
			.attr("x",padding/2)
			.attr("y",circle.attr("cy"))
			.text( format(data[0][1]) )
			.transition()
			.duration(400)
//			.attr("opacity",.1);
			.styleTween("opacity", function() { return d3.interpolate(0, .7); });
//				.attr("x", circle.attr("cx"))
//				.attr("y", circle.attr("cy"))
//				.attr("width", 400)
//				.attr("height", 400)
//				.append("text")
//				.attr("x", circle.attr("cx"))
//				.attr("y", circle.attr("cy"))
//				.text( this.xVariable + " : " +data[0] +"\n"+this.yVariable +" : " + data[1]);
			
		// function to move mouseover item to front of SVG stage, in case
		// another bubble overlaps it
			d3.selection.prototype.moveToFront = function() { 
			  return this.each(function() { 
				this.parentNode.appendChild(this); 
			  }); 
			};

		// skip this functionality for IE9, which doesn't like it
//			if (!$.browser.msie) {
			circle.moveToFront();
//				}
		};
		// what happens when we leave a bubble?
		var mouseOff = function() {
			var circle = d3.select(this);

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
		};

		// run the mouseon/out functions
		circles.on("mouseover", mouseOn);
		circles.on("mouseout", mouseOff);
		
		
		
		//Create labels
//		svg.selectAll("text")
//		   .data(dataset)
//		   .enter()
//		   .append("text")
//		   .text(function(d) {
//		   		return d[0] + "," + d[1];
//		   })
//		   .attr("x", function(d) {
//		   		return xScale(d[0]);
//		   })
//		   .attr("y", function(d) {
//		   		return yScale(d[1]);
//		   })
//		   .attr("font-family", "sans-serif")
//		   .attr("font-size", "11px")
//		   .attr("fill", "red");
		
		//Create X axis
		svg.append("g")
			.attr("class", "axis")
			.attr("transform", "translate(0," + (h - padding) + ")")
			.call(xAxis);
		
		//Create Y axis
		svg.append("g")
			.attr("class", "axis")
			.attr("transform", "translate(" + padding + ",0)")
			.call(yAxis);
		
		// create x / y labels
		svg.append("text")
		.attr("class", "axis-label")
		.attr("text-anchor", "end")
		.attr("x", w - 150)
		.attr("y", h - padding - 5)
		.text( this.xVariable );

		svg.append("text")
			.attr("class", "axis-label")
			.attr("text-anchor", "end")
			.attr("x", -(padding))
			.attr("y", padding + 10)
			.attr("dy", ".75em")
			.attr("transform", "rotate(-90)")
			.text( this.yVariable );
		
	};
	
	return {
		constructor : ScatterPlot,
		
		_init : init,
		
		setDataProvider : setDataProvider,
		
		show : show ,
		
		hide : hide
		
	};
})();
