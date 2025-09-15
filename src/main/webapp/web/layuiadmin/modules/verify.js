/**
 扩展一个verify模块
 **/

layui.use(['form'], function(){
    var form = layui.form;
    form.verify({
        //搜索框-Integer
        searchInteger: function(value, item){
            if(!value){
                return;
            }
            if(!new RegExp("^[0-9]{1,10}$").test(value)){
                return '有效整数必须为1到10位';
            }
            var number = Number(value);
            if(number < 0 || number > 2147483647){
                return '有效整数范围为0到2147483647';
            }
        },
        //搜索框-Integer
        searchLong: function(value, item){
            if(!value){
                return;
            }
            if(!new RegExp("^[0-9]{1,19}$").test(value)){
                return '有效整数必须为1到19位';
            }
            var number = Number(value);
            if(number < 0 || number > 9223372036854775807){
                return '有效整数范围为0到9223372036854775807';
            }
        }
    });
});

layui.define(function(exports){ //提示：模块也可以依赖其它模块，如：layui.define('layer', callback);
    var obj = {
        hello: function(str){
            alert('Hello '+ (str||'mymod'));
        }
    };

    //输出test接口
    exports('verify', obj);
})