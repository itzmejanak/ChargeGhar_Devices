package com.demo.common;


import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.Writer;

public class FreemarkerExceptionHandler implements TemplateExceptionHandler {
    @Override
    public void handleTemplateException(TemplateException e, Environment environment, Writer writer) throws TemplateException {
        try {
            writer.append("<pre style=\"display: block; position: absolute; z-index: 9999 ; color:#ffffff; background: red;\">");
            writer.append(e.toString());
            writer.append("</pre>");
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
