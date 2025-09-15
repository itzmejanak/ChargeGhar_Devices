//字面量版： "test{language}".format ( { language : "javascript" } );
//数组版： "test{0}test{1}test{2}".format(11,22,33);
String.prototype.format = function(args) {
    var result = this;
    if (arguments.length > 0) {
        if (arguments.length == 1 && typeof (args) == "object") {
            for (var key in args) {
                if(args[key]!=undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }
        else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i] != undefined) {
                    var reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
}

//'asd'.PadRight(6,'0') = 000asd
String.prototype.padLeft = Number.prototype.padLeft = function (len, charStr) {
    var s = this + '';
    return new Array(len - s.length + 1).join(charStr,  '') + s;
}
String.prototype.padRight = Number.prototype.padRight = function (len, charStr) {
    var s = this + '';
    return s + new Array(len - s.length + 1).join(charStr,  '');
}

//格式化ID显示
String.prototype.formatId = Number.prototype.formatId = function (len) {
    len = len || 5;
    return this.padLeft(len, '0');
}



//对Date的扩展，将 Date 转化为指定格式的String
//月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
//年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
//例子：
//(new Date()).format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
//(new Date()).format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
Date.prototype.format = function (fmt) { //author: meizz
    if(!this.getTime()){
        return '-';
    }
    if(!fmt){
        fmt = "yyyy-MM-dd hh:mm:ss";
    }

    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

//格式化时间段
//传入毫秒
//3000.formatDuring() ==> 3秒
Number.prototype.formatDuring = function() {
    var mss = parseInt(this);
    var days = Math.trunc(mss / (1000 * 60 * 60 * 24));
    var hours = Math.trunc((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    var minutes = Math.trunc((mss % (1000 * 60 * 60)) / (1000 * 60));
    var seconds = Math.trunc((mss % (1000 * 60)) / 1000);
    var str = '';

    // if(showDay){
    //     if(days > 0){
    //         str += (days + '天');
    //     }
    //     if(hours > 0){
    //         str += (hours + '小时');
    //     }
    // }
    // else{
    //     str = ((days * 24 + hours) + '小时');
    // }
    if(hours > 0 || days > 0){
        str = ((days * 24 + hours) + '小时');
    }
    if(minutes > 0){
        str += (minutes + '分钟');
    }
    if(seconds > 0){
        str += (seconds + '秒');
    }
    return str == '' ? '-' : str;
}