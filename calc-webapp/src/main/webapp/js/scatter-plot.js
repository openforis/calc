/**
 * @author Mino Togna 
 */

function ScatterPlot(container) {
	// ui container
	this.container = container;
	// options section
	this.optionsSection = this.container.find('.options');
	this.xOption = this.optionsSection.find('[name=x]');
	this.yOption = this.optionsSection.find('[name=y]');
	this.refreshBtn = this.optionsSection.find('[name=refresh]');
	//chart container
	this.chartContainer = this.container.find(".chart");
	this.chart = null;
	
	this.job = null;
	this.entityId = null;
	this.variables = [];
	
	this.xVariable = null,
	this.yVariable = null,
	this.offset = 0;
//	this.started = false;
	
	//event handlers
	//change x and y variables
	this.xOption.change( $.proxy(
			function(e) {
					e.preventDefault();
					this.xVariable = this.xOption.val();
				} 
			, this ));
	this.yOption.change( $.proxy(
			function(e){
					e.preventDefault();
					this.yVariable = this.yOption.val();
				} 
			, this ));
	//refresh chart button
	this.refreshBtn.click( $.proxy(
			function(e){
				if( this.xVariable == null || this.yVariable == null ){
					UI.Form.showResultMessage( "x and y must be set", false );
				} else {
					this.refresh();
				}
//				console.log(this);
			} 
		, this ));
			
	
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
		        text: ''
		    },
		    xAxis: {
                title: {
                    enabled: true,
                    text: ''
                },
                lineWidth: 1,
                lineColor: "#ecf0f1",
                gridLineColor: "#ecf0f1",
                tickColor: "#ecf0f1",
                labels: {
                    style: {
                       color: '#ecf0f1',
                       font: '11px Trebuchet MS, Verdana, sans-serif'
                    }
                 }
//                startOnTick: true,
//                endOnTick: true,
//                showLastLabel: true
            },
            yAxis: {
                title: {
                    text: ''
                }
            ,
//                lineWidth: 1,
//                lineColor: "#ecf0f1"
            	gridLineColor: "#ecf0f1",
            	labels: {
                    style: {
                       color: '#ecf0f1',
                       font: '11px Trebuchet MS, Verdana, sans-serif'
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
                color: 'rgba(119, 152, 191, .5)',
                data: []
            }]
		};
	
};

ScatterPlot.prototype = (function(){
	var updateJob = function(job) {
		if( this.job.id == job.id ){
			this.job = job;
		}
	};
	
	var setJob = function(job) {
		var $this = this;
		
		$this.job = job;
		//reset instance variables 
		$this.xVariable = null,
		$this.yVariable = null,
		$this.offset = 0;
		
		$this.entityId = $this.job.tasks[0].calculationStep.outputEntityId;
		if(this.chart){
			this.chart.destroy();
		}
		
		WorkspaceManager.loadQuantitativeVariables($this.entityId, function(response) {
			$this.variables = response;
			UI.Form.populateSelect($this.xOption, $this.variables, 'name','name');
			UI.Form.populateSelect($this.yOption, $this.variables, 'name','name');
		});
//		console.log($this);
	};
	
	var refresh = function() {
		$this = this;
		
		if( this.xVariable != null && this.yVariable != null ) {
			
			//start getting data for the job
			$.ajax({
				url:"rest/data/"+$this.entityId+"/query.json",
				dataType:"json",
				data:{offset:$this.offset, fields:[$this.xVariable,this.yVariable].join()},
				
				success: $.proxy( function(response) {
					var $this = this;
//					$this.started = true;
					var data = response;
					
//					console.log('result from server')
//					console.log(data);
					
					// prepare chart data
					var chartData = [];
					$.each(data,function(i,record){
//						console.log(record);
						var seriesItem = [] ;
//						console.log(record)
						$.each(record.fields, function(i,field) {
//							console.log(field);
							seriesItem.push(field);
						});
						chartData.push(seriesItem);
					});
					
					// show chart
					
//					console.log(chartData);
					this.chartinfo.series[0].data = chartData;
					this.chart = new Highcharts.Chart(this.chartinfo);
//					this.chart = this.chartContainer.highcharts(this.chartinfo);
//					console.log( this.chart.redraw() );
					
				} , this)
				
			});	
		}
		
	};
	
	return {
		constructor : ScatterPlot,
		
		setJob : setJob,
		
		updateJob : updateJob,
		
		show : function() {
			if ( this.offset == 0 ) {
				this.refresh();
			}
			this.container.fadeIn();
		},
		
		hide : function(){
			this.container.hide();
		},
		
		refresh : refresh
//		
//		setXAxis : function(xAxis) {
//			this.xVariable = xAxis;
//			this.chartinfo.xAxis.title.text = xAxis;
//		},
//		
//		setYAxis : function(yAxis) {
//			this.yVariable = yAxis;
//			this.chartinfo.yAxis.title.text = yAxis;
//		},
//		
//		setAxes : function(xAxis, yAxis) {
//			this.setXAxis(xAxis);
//			this.setYAxis(yAxis);
//		}

		
	};
})();
