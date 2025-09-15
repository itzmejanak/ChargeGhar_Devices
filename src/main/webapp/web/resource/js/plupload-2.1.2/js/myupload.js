(function($){
    $.myupload = function(options){

        options = options || {};

        options = {
            suffixs: options.suffixs || '*',
            buttonId: options.buttonId || 'browse_button',
            maxFileSize: options.maxFileSize || '100MB',
            serverUrl: options.serverUrl || '',
            success: options.success || function (file) {}
        }

        var accessid = ''
        var accesskey = ''
        var host = ''
        var policyBase64 = ''
        var signature = ''
        var callbackbody = ''
        var filename = ''
        var key = ''
        var expire = 0
        var g_object_name = ''
        var g_object_name_type = ''
        var now = timestamp = Date.parse(new Date()) / 1000;

        //文件MD5名字
        var fileName = '';
        var md5 = '';
        var suffix = '';
        var fileExist = false;

        function send_request()
        {
            var xmlhttp = null;
            if (window.XMLHttpRequest)
            {
                xmlhttp=new XMLHttpRequest();
            }
            else if (window.ActiveXObject)
            {
                xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
            }

            if (xmlhttp!=null)
            {
                //MD5秒传判断
                var url = options.serverUrl;
                url += ('&md5=' + md5 + '&suffix=' + suffix);

                xmlhttp.open( "GET", url, false );
                xmlhttp.send( null );
                console.log('xmlhttp',xmlhttp);

                if(xmlhttp.status != 200){
                    layer.closeAll('dialog');
                    layer.alert(xmlhttp.responseText, {icon: 5});
                    throw xmlhttp.responseText;
                }

                return xmlhttp.responseText
            }
            else
            {
                alert("Your browser does not support XMLHTTP.");
            }
        };

        function get_signature()
        {
            body = send_request();

            var obj = eval ("(" + body + ")");
            obj = obj.data;
            host = obj['host']
            policyBase64 = obj['policy']
            accessid = obj['accessid']
            signature = obj['signature']
            expire = parseInt(obj['expire'])
            callbackbody = obj['callback']
            key = obj['dir']
            fileName = obj['fileName']
            fileExist = eval(obj['fileExist'].toLowerCase())

            console.log('body',obj);
            return true;
        };

        function get_suffix(filename) {
            pos = filename.lastIndexOf('.')
            suffix = ''
            if (pos != -1) {
                suffix = filename.substring(pos)
            }
            return suffix;
        }

        function get_uploaded_object_name(filename)
        {
            return g_object_name;
        }

        function set_upload_param(up, filename, ret)
        {
            if (ret == false)
            {
                ret = get_signature()
            }

            //判断秒传
            if(fileExist){
                var file = up.files[up.files.length - 1];
                file.fileName = fileName;
                file.host = host;
                file.url = host + '/' + fileName;
                file.md5 = md5;
                options.success(file);
                layer.closeAll('dialog');
                return;
            }

            g_object_name = key;
            if (filename != '') {
                suffix = get_suffix(filename);
                //g_object_name += fileUID + suffix;
                g_object_name = fileName;
            }
            new_multipart_params = {
                'key' : g_object_name,
                'policy': policyBase64,
                'OSSAccessKeyId': accessid,
                'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
                'callback' : callbackbody,
                'signature': signature,
            };
            up.setOption({
                'url': host,
                'multipart_params': new_multipart_params
            });
            up.start();
        }

        var uploader = new plupload.Uploader({
            runtimes : 'html5,flash,silverlight,html4',
            browse_button : options.buttonId,
            multi_selection: false,
            //container: document.getElementById('container'),
            flash_swf_url : '/admin/plugins/plupload-2.1.2/js/Moxie.swf',
            silverlight_xap_url : '/admin/plugins/plupload-2.1.2/js/Moxie.xap',
            url : 'http://oss.aliyuncs.com',
            filters: {
                mime_types : [ //只允许上传图片和zip,rar文件
                    { title : "文件类型", extensions : options.suffixs}
                ],
                max_file_size : options.maxFileSize, //最大只能上传10mb的文件
                prevent_duplicates : false //不允许选取重复文件
            },
            init: {
                PostInit: function() {},

                FilesAdded: function(up, files) {
                    layer.msg('正在检查文件..', {
                        icon: 16,shade: 0.3,time:0
                    });

                    //原生HTML5 File API
                    var file = files[0].getNative();

                    //计算MD5
                    $.md5(file, function (value) {
                        md5 = value;
                        suffix = get_suffix(files[0].name);

                        console.log('上传文件名：' + (md5 + suffix));
                        set_upload_param(uploader, '', false);
                        console.log('判断秒传：' + fileExist);
                    });
                },
                BeforeUpload: function(up, file) {
                    set_upload_param(up, file.name, true);
                },

                UploadProgress: function(up, file) {
                    var msg = '<i class="layui-layer-ico layui-layer-ico16"></i>';
                    msg += ('文件上传中，' + file.percent + '%');
                    $('.layui-layer-content').html(msg);
                },
                FileUploaded: function(up, file, info) {
                    console.log('BeforeUpload_up', up);
                    console.log('BeforeUpload_file', file);
                    if (info.status == 200)
                    {
                        file.fileName = get_uploaded_object_name(file.name);
                        file.host = host;
                        file.url = file.host + '/' + file.fileName;
                        file.md5 = md5;
                        options.success(file);
                    }
                    else
                    {
                        layer.alert(info.response, {icon: 5});
                    }

                    layer.closeAll('dialog');
                    expire = 0;
                },
                Error: function(up, err) {
                    layer.closeAll('dialog');

                    var msg = 'Error xml:' + err.response;
                    if (err.code == -600) {
                        msg = ('文件大小超过' + options.maxFileSize + '限制');
                    }
                    else if (err.code == -601) {
                        msg = '文件格式不正确：' + err.file.name;
                        console.log(err);
                    }
                    else if (err.code == -602) {
                        msg = '这个文件已经上传过一遍了';
                    }

                    layer.alert(msg, {icon: 5});
                }
            }
        });
        uploader.init();
    }

    $.md5 = function(file, callBack){
        var fileReader = new FileReader(),
            blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice,
            chunkSize = 2097152,
            // read in chunks of 2MB
            chunks = Math.ceil(file.size / chunkSize),
            currentChunk = 0,
            spark = new SparkMD5();

        fileReader.onload = function(e) {
            spark.appendBinary(e.target.result); // append binary string
            currentChunk++;

            if (currentChunk < chunks) {
                loadNext();
            }
            else {
                callBack(spark.end());
            }
        };

        function loadNext() {
            var start = currentChunk * chunkSize,
                end = start + chunkSize >= file.size ? file.size : start + chunkSize;

            fileReader.readAsBinaryString(blobSlice.call(file, start, end));
        };

        loadNext();
    }

})(jQuery);