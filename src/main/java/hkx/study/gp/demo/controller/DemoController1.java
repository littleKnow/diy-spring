package hkx.study.gp.demo.controller;

import hkx.study.gp.spring.framework.annotation.HKXAutowired;
import hkx.study.gp.spring.framework.annotation.HKXController;
import hkx.study.gp.spring.framework.annotation.HKXRequestMapping;
import hkx.study.gp.spring.framework.annotation.HKXRequestParam;
import hkx.study.gp.demo.service.DemoService1;

@HKXController
@HKXRequestMapping("/demo1")
public class DemoController1 {

    @HKXAutowired
    private DemoService1 demoService1;

    @HKXRequestMapping("hello")
    public String hello(@HKXRequestParam("n") String name,
                        @HKXRequestParam("t") String time) {
        return demoService1.hello(name, time);
    }

}
