package com.example.controller;

import com.example.module.Product;
import com.example.service.KillProdService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    UserService userService;
    @Autowired
    KillProdService killProdService;

    @RequestMapping("/queryContent")
    public String queryConten() {
        return userService.queryContent();
    }

    @RequestMapping("/testHys")
    public String testHys() {
        List<String> s = userService.queryHys();
        return s.get(0);
    }

    @RequestMapping("/seckill")
    public Product seckill(@RequestParam(required = false, defaultValue = "") int id) {
        return killProdService.syncProd(id);
    }

    @RequestMapping("/seckillLua")
    public boolean seckillLua(@RequestParam(required = false, defaultValue = "") int id,
                              @RequestParam(required = false, defaultValue = "") String userId) {
        return killProdService.secKillByLock(id, userId);
    }

    @RequestMapping("/seckillQueue")
    public boolean seckillQueue(@RequestParam(required = false, defaultValue = "") int id,
                              @RequestParam(required = false, defaultValue = "") String userId) {
        return killProdService.sockByQueen(id, userId);
    }
}
