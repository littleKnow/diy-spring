package hkx.study.gp.spring.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@HKXComponent
public @interface HKXController {
}
