package hkx.study.gp.spring.controller;

import hkx.study.gp.spring.annotation.HKXAutowaired;
import hkx.study.gp.spring.annotation.HKXController;
import hkx.study.gp.spring.annotation.HKXRequestMapping;
import hkx.study.gp.spring.annotation.HKXRequestParam;
import hkx.study.gp.spring.service.DemoService1;

@HKXController
@HKXRequestMapping("/demo1")
public class DemoController1 {

    @HKXAutowaired
    private DemoService1 demoService1;

    @HKXRequestMapping("hello")
    public String hello(@HKXRequestParam("n") String name,
                        String time) {
        return demoService1.hello(name, time);
    }

}
