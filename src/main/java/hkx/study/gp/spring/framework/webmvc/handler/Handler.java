package hkx.study.gp.spring.framework.webmvc.handler;

import java.lang.reflect.Method;
import java.util.List;

public class Handler {

    private Object handler;
    private Method method;
    private List<String> paramNames;

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }
}
