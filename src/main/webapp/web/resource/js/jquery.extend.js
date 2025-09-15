/**
 * jquery扩展类
 * Created by zhujinbin
 * 2016-04-26
 */

/**
 * 将插件封装在一个闭包里面，防止外部代码污染  冲突
 */
(function($){

    $.toJSON = function(obj){
        return JSON.stringify(obj);
    }

    $.evalJSON = function (str) {
        return $.isBlank(str) ? "" :  JSON.parse(str);
    };

    $.test = function(obj){
        alert(JSON.stringify(obj,null,2));
        console.log('test', obj);
    };


    //关闭窗口 刷新父级表格
    //$.closeWindow()          关闭窗口
    //$.closeWindow('page')    关闭窗口，并刷新父级页面
    //$.closeWindow('table')   关闭窗口，并异步刷新父级表格
    $.closeWindow = function (moudle) {
        var hasParent = (parent.location != self.location);
        //关闭主页标签
        if(parent.window.isIndexPage){
            var topLayui = parent === self ? layui : top.layui;
            topLayui.admin.closeThisTabs();
            return;
        }

        //刷新父级页面
        if(hasParent && moudle){
            $.parentReload(moudle);
        }

        //关闭当前窗口
        if (hasParent) {
            parent.window.layer.closeAll('iframe');
        } else {
            window.close();
        }
    };


    //刷新父级页面
    //$.parentReload('page')    新父级页面
    //$.parentReload('table')   异步刷新父级表格
    $.parentReload = function (moudle) {
        var hasParent = (parent.location != self.location);
        if(hasParent && moudle){
            //重新加载父级页面
            if(moudle == 'page'){
                parent.location.reload();
            }
            //ajax刷新父级表格
            else if(moudle == 'table' && parent.layui && parent.layui.table){
                if(!parent.vue1 || !parent.vue1.table1){
                    alert('无法刷新表格，父级未定义vue1或table1');
                    console.error('无法刷新表格，未定义vue1或table1');
                    return;
                }
                parent.vue1.table1.reload();
            }
        }
    };

    //打开标签页
    $.openTab = function(url, title) {
        var topLayui = parent === self ? layui : top.layui;
        if (top.location != self.location) {
            topLayui.index.openTabsPage(url, title);
        } else {
            window.open(url);
        }
    }

    //打开当前窗口
    $.openFullFrame = function(url, title){
        var index = layer.open({
            area: ['100%', '100%'],
            type: 2,
            content: url,
            title: title || '页面信息',
            //closeBtn: 0,
            anim: -1,
            isOutAnim:false,
        });
        console.log('openFullFrame', url);
    };

    /**
     * <input name="a" value="111">,<input name="b" value="222">
     * $("#from").serializeObject();
     */
    $.fn.serializeObject = function() {
        var disableds = this.find(':disabled');
        disableds.attr('disabled',false);

        var o = {};
        var a = this.serializeArray();
        $.each(a, function() {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });

        disableds.attr('disabled',true);
        return o;
    };

    /**
     * 获取所有地址栏URL参数
     * @returns result = {a:value1,b:value2}
     */
    $.getUrlParams = function(_url){
        var url = _url || location.search;
        var theRequest = new Object();
        var index = url.indexOf("?");
        if (index != -1) {
            var str = url.substr(index + 1);
            strs = str.split("&");
            for(var i = 0; i < strs.length; i ++) {
                theRequest[strs[i].split("=")[0]] = decodeURI(strs[i].split("=")[1]);
            }
        }
        return theRequest;
    };

    //判断空字符串
    $.isBlank = function (obj) {
        return(!obj || $.trim(obj) === "");
    };

    //重写jquery的ajax方法
    var _ajax=$.ajax;
    $.ajax=function(opt) {
        var fn = {
            error: function (XMLHttpRequest, textStatus, errorThrown) {},
            success: function (data, textStatus) {},
            beforeSend: function (callbackContext, jqXHR, s) {},
            complete: function (status, statusText, responses, heads) {}
        }
        if (opt.error) {
            fn.error = opt.error;
        }
        if (opt.success) {
            fn.success = opt.success;
        }
        if (opt.beforeSend) {
            fn.beforeSend = opt.beforeSend;
        }
        if (opt.complete) {
            fn.complete = opt.complete;
        }
        // console.log(opt);
        // console.log(opt.errorMsg || true);
        // return;
        //
        // opt.loading = opt.loading || false;
        // opt.errorMsg = opt.errorMsg || true;
        //
        // console.log(opt);
        // return;


        //扩展增强处理
        var _opt = $.extend(opt, {
            beforeSend: function (callbackContext, jqXHR, s) {
                if(typeof(layer) != 'undefined'){
                    if(opt.data && typeof(opt.data.page) != 'undefined' && typeof(opt.data.limit) != 'undefined'){
                        layer.load(2);
                    }

                    if(opt.loading){
                        layer.load(2, {shade: [0.20,'#000']});
                    }
                }
                fn.beforeSend(callbackContext, jqXHR, s);
            },
            complete: function (status, statusText, responses, heads) {
                if(typeof(layer) != 'undefined'){
                    layer.closeAll('loading');
                }
                fn.complete(status,statusText,responses,heads);
            },
            isJsonString: function(str) {
                try {
                    if (typeof JSON.parse(str) == "object") {
                        return true;
                    }
                } catch (e) {
                }
                return false;
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                try {
                    var msg = XMLHttpRequest.responseText;
                    if(!XMLHttpRequest.readyState){
                        msg = '未连接到互联网，请检查网络连接';
                    }
                    else if(this.isJsonString(XMLHttpRequest.responseText)){
                        var data = JSON.parse(XMLHttpRequest.responseText);
                        msg = data.msg || data.data;
                    }

                    console.log('ajax', msg);
                    if (opt.errorMsg !== false) {
                        layer.alert(msg, {icon: -1}, function (index) {
                            if(XMLHttpRequest.status == 460){
                                top.location.reload();
                            }
                            layer.close(index);
                        });
                    }
                }
                catch (e) {
                    console.error('ajax, catch error',e);
                    layer.alert(XMLHttpRequest.responseText, {icon: -1});
                }
                fn.error(XMLHttpRequest, textStatus, errorThrown);
            },
            success: function (data, textStatus) {
                //格式化 layui table分页数据
                if(opt.data && typeof(opt.data.page) != 'undefined' && typeof(opt.data.limit) != 'undefined'){
                    data.count = data.data.count;
                    data.data = data.data.data || data.data;
                    data.code = 0;
                    data.msg = 'ok';
                }
                fn.success(data,textStatus);
            }
        });
        _ajax(_opt);
    }
})($);