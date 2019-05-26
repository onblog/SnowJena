function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURI(r[2]);
    return null;
}
var app = GetQueryString("app");
var id = GetQueryString("id");
var name = GetQueryString("name");
setInterval(function () {
    $.get("/monitor/json?app="+app+"&id="+id+"&name="+name, function (data) {
        my_echarts(data)
    });
}, 3000);
function my_echarts(data) {
    var myChart = echarts.init(document.getElementById('view'));//（1）
    // 指定图表的配置项和数据
    var option = {
        backgroundColor: '#21202D',
        title: {
            subtext: '单位/个',
            textStyle: {
                color: '#fff'
            }
        },
        tooltip: {
            trigger: 'axis'
        },
        toolbox: {
            feature: {
                dataZoom: {
                    yAxisIndex: 'none'
                },
                restore: {},
                saveAsImage: {},
                magicType: {
                    type: []
                }
            }
        },
        legend: {
            textStyle: {
                color: '#fff'
            },
            data: ['QPS', 'PASS']
        },
        xAxis: {
            data: data.map(function (item) {
                return item.time;
            }),
            nameTextStyle: {
                color: '#fff'
            },
            axisLine: {lineStyle: {color: '#8392A5'}},//X轴颜色
        },
        yAxis: {
            scale: true,
            axisLine: {lineStyle: {color: '#8392A5'}},//Y轴颜色
            splitLine: {show: false}
        },
        series: [{
            name: 'QPS',
            type: 'bar',
            data: data.map(function (item) {
                return item.pre;
            })
        }, {
            name: 'PASS',
            type: 'line',
            data: data.map(function (item) {
                return item.after;
            }),
            lineStyle: {
                color: '#009688'
            }
        }],
        dataZoom: [{ //区域缩放
            startValue: data[0].time
        }, {
            type: 'inside'
        }]
    };
    // 使用刚指定的配置项和数据显示图表。
    myChart.setOption(option);
}