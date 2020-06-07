package hkx.study.gp.spring.service.impl;

import hkx.study.gp.spring.annotation.HKXService;
import hkx.study.gp.spring.service.DemoService1;

@HKXService
public class DemoService1Impl implements DemoService1 {

    @Override
    public String hello(String name, String time) {
        return "hello," + name + "。<br/>" + time + "好";
    }
}
