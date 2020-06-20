package hkx.study.gp.demo.service.impl;

import hkx.study.gp.spring.framework.annotation.HKXService;
import hkx.study.gp.demo.service.DemoService1;

@HKXService
public class DemoService1Impl implements DemoService1 {

    @Override
    public String hello(String name, String time) {
        return "hello," + name + "。<br/>" + time + "好";
    }
}
