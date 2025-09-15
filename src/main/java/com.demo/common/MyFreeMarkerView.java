package com.demo.common;

import com.demo.tools.HttpServletUtils;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import org.springframework.web.context.ContextLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MyFreeMarkerView extends org.springframework.web.servlet.view.freemarker.FreeMarkerView {
    @Override
    protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
        //注入全局路径
        String _contextPath = request.getContextPath();
        String __contextPath = HttpServletUtils.getRealContextpath(request);
        model.put("_contextPath", _contextPath);
        model.put("__contextPath", __contextPath);
        model.put("_time", System.currentTimeMillis());

        //注入模块路径
        String[] modules = new String[]{"app", "web", "api", "member", "task"};
        for(String module : modules){
            model.put(("_" + module + "Path"), (_contextPath + "/" + module));
            model.put(("__" + module + "Path"), (__contextPath + "/" + module));
        }

        //注入当前模块名
        String module = HttpServletUtils.getModule(request);
        model.put("_module", module);

        //注入HTTP ParametS
        Map _params = new HashMap();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            _params.put(key, request.getParameter(key));
        }
        model.put("_params", _params);

        super.exposeHelpers(model, request);
    }

}
