package hkx.study.gp.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@HKXComponent
public @interface HKXController {
}
