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
			this.dataProvider.data( $this.offset , 5000 , null , vars, function(response) {
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
		$this.chart = new Highcharts.Chart($this.chartinfo);
		
		
		

		
		
		
		// update offset
//		$this.offset = $this.chart.series[0].data.length;
		$this.offset += data.length;
		// update progress
		$this.progressBar.update($this.offset , $this.totalItems);
		
	};
	
	var d3test = function(){
		console.log("==== start testing d3");
//		var w = this.chartContainer.width();
//		var h = this.chartContainer.height();
		var w = 900;
		var h = 1000;
		
		var dataset = seriesData;
//		               var dataset = [
//						[5, 20], [480, 90], [250, 50], [100, 33], [330, 95],
//						[410, 12], [475, 44], [25, 67], [85, 21], [220, 88]
//					  ];

		//Create SVG element
		var svg = d3.select("#" + this.chartContainer.attr('id') )
					.append("svg")
					.attr("width", w)
					.attr("height", h);

		svg.selectAll("circle")
		   .data(dataset)
		   .enter()
		   .append("circle")
		   .attr("cx", function(d) {
		   		return d[0] * 100 / 900;
		   })
		   .attr("cy", function(d) {
			   	return d[1] * 100 / 1000;
//		   		return d[1];
		   })
		   .attr("r", function(d) {
		   		return 1;
		   });
//		   .attr("r", function(d) {
//		   		return Math.sqrt(h - d[1]);
//		   });

//		svg.selectAll("text")
//		   .data(dataset)
//		   .enter()
//		   .append("text")
//		   .text(function(d) {
//		   		return d[0] + "," + d[1];
//		   })
//		   .attr("x", function(d) {
//		   		return d[0];
//		   })
//		   .attr("y", function(d) {
//		   		return d[1];
//		   })
//		   .attr("font-family", "sans-serif")
//		   .attr("font-size", "11px")
//		   .attr("fill", "red");
	};
	
	return {
		constructor : ScatterPlot,
		
		_init : init,
		
		setDataProvider : setDataProvider,
		
		show : show ,
		
		hide : hide
		
	};
})();
