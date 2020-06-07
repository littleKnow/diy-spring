package hkx.study.gp.spring;

import hkx.study.gp.spring.annotation.*;
import hkx.study.gp.spring.mapping.HandlerMapping;
import hkx.study.gp.spring.service.DemoService1;
import hkx.study.gp.spring.service.impl.DemoService1Impl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HKXDispatcherServlet extends HttpServlet {

    private volatile boolean isInit = false;

    private Map<String, Object> ioc = new ConcurrentHashMap<>();
    private Map<String, HandlerMapping> handleMappingMap = new ConcurrentHashMap<>();

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
        HandlerMapping handlerMapping = handleMappingMap.get(servletPath);
        if (handlerMapping != null) {
            Object handler = handlerMapping.getHandler();
            Method method = handlerMapping.getMethod();
            List<String> paramNames = handlerMapping.getParamNames();

            Object[] values = new Object[paramNames.size()];
            for (int i = 0; i < paramNames.size(); i++) {
                String value = req.getParameter(paramNames.get(i));
                values[i] = value;
            }

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

    public void init() throws ServletException {
        super.init();

        //1.创建ioc中的对象
        try {
            initIoc();
            initAutowired();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //3.反射url对应的方法
        initHandlerMapping();

    }

    private void initAutowired() throws IllegalAccessException {
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry<String, Object> beanEntry : entries) {
            Object bean = beanEntry.getValue();
            Class<?> beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();
            FieldFor:
            for (Field field : fields) {
                if (field.isAnnotationPresent(HKXAutowaired.class)) {
                    field.setAccessible(true);

                    Class<?> type = field.getType();
                    for (Map.Entry<String, Object> beanEntry2 : entries) {
                        Object value = beanEntry2.getValue();
                        if (type.isAssignableFrom(value.getClass())) {
                            field.set(bean, value);
                            continue FieldFor;
                        }
                    }
                }
            }
        }
    }

    private void initIoc() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("hkxapp.properties");
        Properties properties = new Properties();
        properties.load(stream);

        String scanPackage = properties.getProperty("scanPackage").replace(".", "/");
        loadIoc(scanPackage);
    }

    private void loadIoc(String scanPackage) throws Exception {
        File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("/" + scanPackage)).toURI());
        File root = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("/")).toURI());
        if (file.isDirectory()) {
            for (File file1 : Objects.requireNonNull(file.listFiles())) {
                String packageName = file1.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                loadIoc(packageName);
            }
        } else {
            String path = file.getAbsolutePath();
            if (path.endsWith(".class")) {
                String className = path.substring(root.getAbsolutePath().length() + 1);
                className = className.replace("/", ".").replace("\\", ".");
                className = className.substring(0, className.lastIndexOf(".class"));
                Class<?> aClass = Class.forName(className);
                if (isComponent(aClass)) {
                    String name = toLowerFirstCase(aClass.getSimpleName());
                    Object bean = aClass.newInstance();
                    ioc.put(name, bean);
                }
            }
        }
    }

    private boolean isComponent(Class<?> aClass) {
        if (aClass.isInterface() || aClass.isAnnotation() || aClass.isEnum() || aClass.isArray()) {
            return false;
        }
        return isComponentClass(aClass);
    }

    private boolean isComponentClass(Class<?> aClass) {
        if (aClass.isAnnotationPresent(HKXComponent.class)) {
            return true;
        }
        for (Annotation annotation : aClass.getDeclaredAnnotations()) {
            if (isComponentClass(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    private String toLowerFirstCase(String name) {
        char[] chars = name.toCharArray();
        chars[0] = (chars[0] + "").toLowerCase().charAt(0);
        return new String(chars);
    }

    private void initHandlerMapping() {
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry<String, Object> beanEntry : entries) {
            Object bean = beanEntry.getValue();
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

                HandlerMapping handlerMapping = new HandlerMapping();
                handlerMapping.setHandler(bean);
                handlerMapping.setMethod(m);
                handlerMapping.setParamNames(paramNames);
                handleMappingMap.put(url, handlerMapping);
            }
        }
    }
}
