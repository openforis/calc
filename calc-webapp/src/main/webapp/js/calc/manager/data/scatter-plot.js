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
			$this.chart = new Highcharts.Chart($this.chartinfo);
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
			this.dataProvider.data( $this.offset , null , null , vars, function(response) {
				$.proxy(addPoints, $this)(response);

				//get next data if not all data have been loaded
				if( $this.offset < $this.totalItems ){
					$.proxy(getNextData, $this)();
				}
			});
		}
	};
	
	// add points to the chart
	var addPoints = function(data){
		var $this = this;
		
		// prepare chart data
		$.each(data,function(i,record){
			var seriesItem = [ record.fields[$this.xVariable] , record.fields[$this.yVariable] ] ;
//			var shift = ($this.chart.series.data) ? $this.chart.series.data.length > 20 : 0;
			$this.chart.series[0].addPoint(seriesItem, false);
			
			if($this.chart.series[0].data.length % 400 == 0) {
				$this.chart.redraw();
			}
		});
		// redraw chart
		$this.chart.redraw();
		// update offset
		$this.offset = $this.chart.series[0].data.length;
		// update progress
		$this.progressBar.update($this.offset , $this.totalItems);
		
	};
	
	return {
		constructor : ScatterPlot,
		
		_init : init,
		
		setDataProvider : setDataProvider,
		
		show : show ,
		
		hide : hide
		
	};
})();
