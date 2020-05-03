package com.nan.server.controller;

import com.nan.server.param.TestParam;
import com.nan.server.param.TestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nan")
@Slf4j
public class MyController {

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public TestResponse test(TestParam param, HttpServletRequest request) {

        log.info("request: " + request.toString());

        return TestResponse.builder()
                .myName("my" + param.getName())
                .myParam("my" + param.getParam())
                .build();
    }
}
