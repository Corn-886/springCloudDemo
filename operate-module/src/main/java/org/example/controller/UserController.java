package org.example.controller;

import org.example.service.DataCloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    DataCloudService dataCloudService;

    @RequestMapping("/queryContent")
    public String queryConten(    @RequestParam(value = "custCode", required = false) String custCode,
                                   @RequestParam(value = "startDate", required = false) Integer startDate,
                                  @RequestParam(value = "endDate", required = false) Integer endDate) {

        return "" + dataCloudService.getYesHoldAmount(custCode, startDate, endDate);
    }

    @RequestMapping("/querylist")
    public String querylist(    @RequestParam(value = "custCode", required = false) String custCode,
                                  @RequestParam(value = "startDate", required = false) Integer startDate,
                                  @RequestParam(value = "endDate", required = false) Integer endDate) {

        return "" + dataCloudService.getCustProdPofit(custCode, startDate, endDate).toString();
    }
}
