package hkx.study.gp.spring.framework.webmvc.servlet;

import hkx.study.gp.spring.framework.annotation.*;
import hkx.study.gp.spring.framework.context.HKXApplicationContext;
import hkx.study.gp.spring.framework.webmvc.handler.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/**
 * 委派模式
 * 负责对请求的派发
 */
public class HKXDispatcherServlet extends HttpServlet {

    private volatile boolean isInit = false;

    private Map<String, Handler> handleMappingMap = new HashMap<>();

    private HKXApplicationContext hkxApplicationContext = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (doInit()) {
            try {
                doDispatcher(req, resp);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("content-type", "text/html;charset=UTF-8");

        String servletPath = req.getServletPath();

        Handler handlerMapping = handleMappingMap.get(servletPath);
        if (handlerMapping != null) {
            Object handler = handlerMapping.getHandler();
            Method method = handlerMapping.getMethod();
            List<String> paramNames = handlerMapping.getParamNames();

            Object[] values = new Object[paramNames.size()];
            for (int i = 0; i < paramNames.size(); i++) {
                String value = req.getParameter(paramNames.get(i));
                values[i] = value;
            }

            // Object responseContext = handlerMappingHandler.invoke(servletPath, values);
            Object responseContext = method.invoke(handler, values);
            resp.getWriter().append(String.valueOf(responseContext));
        } else if ("/".equals(servletPath)) {
            resp.getWriter().append("index page");
        } else {
            resp.getWriter().append("404 not found!");
        }
    }

    private boolean doInit() throws ServletException {
        if (isInit) {
            return isInit;
        }
        synchronized (this) {
            if (isInit) {
                return isInit;
            }
            init();
            isInit = true;
        }
        return isInit;
    }

    public void init(ServletConfig config) throws ServletException {
        // 初始化IOC容器
        hkxApplicationContext = new HKXApplicationContext(config.getInitParameter("configLocation"));

        initHandlerMapping();
    }

    private void initHandlerMapping() {
        if (this.hkxApplicationContext.getBeanDefinitionCount() == 0) {
            return;
        }

        String[] BeanDefinitionNames = this.hkxApplicationContext.getBeanDefinitionNames();
        for (String beanName : BeanDefinitionNames) {
            Object bean = this.hkxApplicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            if (!beanClass.isAnnotationPresent(HKXRequestMapping.class)) {
                continue;
            }

            String classUrl = beanClass.getAnnotation(HKXRequestMapping.class).value();
            Method[] declaredMethods = beanClass.getDeclaredMethods();
            for (Method m : declaredMethods) {
                List<String> paramNames = new ArrayList<>();
                if (!m.isAnnotationPresent(HKXRequestMapping.class)) {
                    continue;
                }
                String methodUrl = m.getAnnotation(HKXRequestMapping.class).value();
                String url = ("/" + classUrl + "/" + methodUrl).replaceAll("/+", "/");

                Parameter[] parameters = m.getParameters();
                for (Parameter param : parameters) {
                    String paramName = null;
                    if (param.isAnnotationPresent(HKXRequestParam.class)) {
                        paramName = param.getAnnotation(HKXRequestParam.class).value();
                    } else {
                        paramName = param.getName();
                    }
                    paramNames.add(paramName);
                }

                Handler handler = new Handler();
                handler.setHandler(bean);
                handler.setMethod(m);
                handler.setParamNames(paramNames);
                handleMappingMap.put(url, handler);
            }
        }
    }
}
