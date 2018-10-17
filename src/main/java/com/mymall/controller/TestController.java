package com.mymall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping("testResponseBody.do")
    @ResponseBody
    public Map testResponseBody(Integer testPara){
//        User user = new User();
//        user.setId(10);
//        user.setUsername("ling");
//        user.setPassword("111");
//        user.setAnswer("test");
        Map map = new HashMap();
        map.put("key1",testPara);
        map.put("key2","value2");

        return map;
    }

//    @RequestMapping("testRequestBody.do")
//    @ResponseBody
//    public TestModel testRequestBody(@RequestBody TestModel testModel){
//        return testModel;
//    }
}
