/**
 * @author Mino Togna 
 */

function ScatterPlot(container) {
	
	this.container = container;
	
	this.xVariable = null,
	
	this.yVariable = null,
	
//	console.log(this.container.html());
	this.chartinfo = {
		    chart: {
		    	type: 'scatter',
                zoomType: 'xy',
                borderWidth: 0,
                borderRadius: 0,
                backgroundColor: null	
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

ScatterPlot.prototype.show = function() {
	this.container.fadeIn();
};

ScatterPlot.prototype.hide = function(){
	this.container.hide();
};

ScatterPlot.prototype.setXAxis = function(xAxis) {
	this.xVariable = xAxis;
	this.chartinfo.xAxis.title.text = xAxis;
};

ScatterPlot.prototype.setYAxis = function(yAxis) {
	this.yVariable = yAxis;
	this.chartinfo.yAxis.title.text = yAxis;
};
ScatterPlot.prototype.setAxes = function(xAxis, yAxis) {
	this.setXAxis(xAxis);
	this.setYAxis(yAxis);
};

ScatterPlot.prototype.refresh = function(data) {
	var chartData = [];
	$.each(data,function(i,record){
		var seriesItem = [] ; 
		$.each(this.fields, function(i,field) {
			seriesItem.push(field);
		});
		chartData.push(seriesItem);
	});
	
	this.chartinfo.series[0].data = chartData;
	$(this.container.find(".chart")).highcharts(this.chartinfo);
};
