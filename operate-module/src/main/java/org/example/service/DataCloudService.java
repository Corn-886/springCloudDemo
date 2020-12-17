package org.example.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ResObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Configuration
public class DataCloudService {
    private static AtomicInteger count = new AtomicInteger(0);

    @Value("${datacloud.source}")
    public String source;
    @Value("${datacloud.secret}")
    public String secret;
    @Value("${datacloud.invest_url}")
    public String url;
    @Autowired
    RestTemplate restTemplate;


    private static String md5(String content) {
        return messageDigest(content, "md5");
    }

    private static String messageDigest(String content, String algo) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            md.update(content.getBytes("UTF-8"));
            result = byte2HexStr(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String byte2HexStr(byte[] buf) {
        if (buf == null || buf.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * 获取用户昨天收益
     *
     * @return
     */
    public Map<String, Double> getYesHoldAmount(String custCode, int startDate, int endDate) {
        count.incrementAndGet();
        Map<String, Double> res = new HashMap<>();
        long mills = System.currentTimeMillis();
        long mills2 = System.currentTimeMillis();

        StringBuilder s1 = new StringBuilder("" + startDate);
        s1.insert(4, "-").insert(7, "-");
        StringBuilder s2 = new StringBuilder("" + endDate);
        s2.insert(4, "-").insert(7, "-");
        List<Double> amounts = new ArrayList<>();
        List<Double> profitsum = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source", source);
        jsonObject.put("password", md5(secret + mills));
        jsonObject.put("timeStamp", mills);
        JSONArray condition = new JSONArray();
        condition.add("eq,startRow," + custCode + "_fund_" + s1.toString());
        condition.add("eq,stopRow," + custCode + "_fund_" + s2.toString());
        jsonObject.put("conditions", condition);
        jsonObject.put("pageindex", 1);
        jsonObject.put("pagesize", 100);
        jsonObject.put("asc", "true");

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("source", source);
        jsonObject2.put("password", md5(secret + mills2));
        jsonObject2.put("timeStamp", mills2);
        JSONArray condition2 = new JSONArray();
        condition2.add("eq,startRow," + custCode + "_fiancial_" + s1.toString());
        condition2.add("eq,stopRow," + custCode + "_fiancial_" + s2.toString());
        jsonObject2.put("conditions", condition2);
        jsonObject2.put("pageindex", 1);
        jsonObject2.put("pagesize", 100);
        jsonObject2.put("asc", "true");


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        log.info(jsonObject.toString());
        log.info(jsonObject2.toString());

        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        HttpEntity<String> request2 = new HttpEntity<>(jsonObject2.toString(), headers);

        try {
            count.incrementAndGet();
            ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
            String result = "{\"retcode\":\"0000\",\"retmsg\":\"success\",\"responsetime\":\"2020-12-14 09:05:35\",\"response_body\":[{\"stock:98.873001\":\"{\\\"pre_amt\\\":\\\"0.0\\\",\\\"pre_qty\\\":\\\"0.0\\\",\\\"trade_id\\\":\\\"160100027976\\\",\\\"account_type\\\":\\\"trade\\\",\\\"prd_no\\\":\\\"98.873001\\\",\\\"end_amt\\\":\\\"0.0\\\",\\\"end_qty\\\":\\\"0.0\\\",\\\"cash_flow_qty\\\":\\\"0.0\\\",\\\"cash_flow_amt\\\":\\\"0.0\\\",\\\"positive_amt\\\":\\\"0.0\\\",\\\"profit_amt\\\":\\\"0.0\\\",\\\"profit\\\":\\\"\\\\\\\\N\\\",\\\"profit_amt_sum\\\":\\\"\\\\\\\\N\\\",\\\"profit_his\\\":\\\"\\\\\\\\N\\\",\\\"shr_nav\\\":\\\"1.0\\\",\\\"trd_dt\\\":\\\"20201211\\\",\\\"buy_date\\\":\\\"\\\\\\\\N\\\",\\\"model\\\":\\\"fund\\\",\\\"nav_flag\\\":\\\"3\\\",\\\"buy_date_flag\\\":\\\"1\\\",\\\"colum_1\\\":\\\"\\\",\\\"colum_2\\\":\\\"\\\",\\\"colum_3\\\":\\\"\\\",\\\"busi_date\\\":\\\"2020-12-11\\\"}\"}],\"pageindex\":\"1\",\"pagesize\":\"100\",\"totalrecords\":\"1\",\"totalpages\":\"1\"}";
            log.info(result);
            JSONObject j = JSONObject.parseObject(result);
            JSONArray items = j.getJSONArray("response_body");
            for (int i = 0; i < items.size(); i++) {
                Map<String, String> map = (Map<String, String>) items.get(i);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    ResObject jsonValue = JSONObject.parseObject(entry.getValue(), ResObject.class);
                    if (jsonValue.getProfit_amt() != null) {
                        amounts.add(Double.parseDouble(jsonValue.getProfit_amt()));
                    }
                    if (jsonValue.getProfit_amt_sum() != null) {
                        profitsum.add(Double.parseDouble(jsonValue.getProfit_amt_sum()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            count.incrementAndGet();
            ResponseEntity<String> postForEntity2 = restTemplate.postForEntity(url, request2, String.class);
            String result2 = "{\"retcode\":\"0000\",\"retmsg\":\"success\",\"responsetime\":\"2020-12-14 09:05:35\",\"response_body\":[{\"stock:87.GF1807\":\"{\\\"pre_amt\\\":\\\"824795.65764\\\",\\\"pre_qty\\\":\\\"8199.09\\\",\\\"trade_id\\\":\\\"160100027976\\\",\\\"account_type\\\":\\\"trade\\\",\\\"prd_no\\\":\\\"87.GF1807\\\",\\\"end_amt\\\":\\\"824894.0467200001\\\",\\\"end_qty\\\":\\\"8199.09\\\",\\\"cash_flow_qty\\\":\\\"0.0\\\",\\\"cash_flow_amt\\\":\\\"0.0\\\",\\\"positive_amt\\\":\\\"0.0\\\",\\\"profit_amt\\\":\\\"98.38908000010997\\\",\\\"profit\\\":\\\"1.1928903733760202E-4\\\",\\\"profit_amt_sum\\\":\\\"61222.05792\\\",\\\"profit_his\\\":\\\"0.07853744630298246\\\",\\\"shr_nav\\\":\\\"100.608\\\",\\\"trd_dt\\\":\\\"20201211\\\",\\\"buy_date\\\":\\\"2019-01-01\\\",\\\"model\\\":\\\"fiancial\\\",\\\"nav_flag\\\":\\\"3\\\",\\\"buy_date_flag\\\":\\\"1\\\",\\\"colum_1\\\":\\\"0.078536\\\",\\\"colum_2\\\":\\\"61222.05792\\\",\\\"colum_3\\\":\\\"1\\\",\\\"busi_date\\\":\\\"2020-12-11\\\"}\"}],\"pageindex\":\"1\",\"pagesize\":\"100\",\"totalrecords\":\"1\",\"totalpages\":\"1\"}";
            log.info(result2);
            JSONObject j2 = JSONObject.parseObject(result2);
            JSONArray items2 = j2.getJSONArray("response_body");
            for (int i = 0; i < items2.size(); i++) {
                Map<String, String> map = (Map<String, String>) items2.get(i);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    ResObject jsonValue = JSONObject.parseObject(entry.getValue(), ResObject.class);
                    if (jsonValue.getProfit_amt() != null) {
                        amounts.add(Double.parseDouble(jsonValue.getProfit_amt()));
                    }
                    if (jsonValue.getProfit_amt_sum() != null) {
                        profitsum.add(Double.parseDouble(jsonValue.getProfit_amt_sum()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (!amounts.isEmpty()) {
            //相加结果，空返回null
            res.put("yesAmount", amounts.stream().mapToDouble(Double::doubleValue).sum());
            res.put("prodRetain", profitsum.stream().mapToDouble(Double::doubleValue).sum());
            return res;

        } else {
            return null;
        }
    }

    /**
     * 获取用户产品收益
     *
     * @return
     */
    public Map<String, ResObject> getCustProdPofit(String custCode, int startDate, int endDate) {

        long mills = System.currentTimeMillis();
        long mills2 = System.currentTimeMillis();

        StringBuilder s1 = new StringBuilder("" + startDate);
        s1.insert(4, "-").insert(7, "-");
        StringBuilder s2 = new StringBuilder("" + endDate);
        s2.insert(4, "-").insert(7, "-");
        //存放结果
        Map<String, ResObject> prods = new HashMap();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source", source);
        jsonObject.put("password", md5(secret + mills));
        jsonObject.put("timeStamp", mills);
        JSONArray condition = new JSONArray();
        condition.add("eq,startRow," + custCode + "_fund_" + s1.toString());
        condition.add("eq,stopRow," + custCode + "_fund_" + s2.toString());
        jsonObject.put("conditions", condition);
        jsonObject.put("pageindex", 1);
        jsonObject.put("pagesize", 100);
        jsonObject.put("asc", "true");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);


        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("source", source);
        jsonObject2.put("password", md5(secret + mills2));
        jsonObject2.put("timeStamp", mills2);
        JSONArray condition2 = new JSONArray();
        condition2.add("eq,startRow," + custCode + "_fiancial_" + s1.toString());
        condition2.add("eq,stopRow," + custCode + "_fiancial_" + s2.toString());
        jsonObject2.put("conditions", condition2);
        jsonObject2.put("pageindex", 1);
        jsonObject2.put("pagesize", 100);
        jsonObject2.put("asc", "true");


        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        try {
            count.incrementAndGet();
            ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
            String result = "{\"retcode\":\"0000\",\"retmsg\":\"success\",\"responsetime\":\"2020-12-14 09:05:35\",\"response_body\":[{\"stock:98.873001\":\"{\\\"pre_amt\\\":\\\"0.0\\\",\\\"pre_qty\\\":\\\"0.0\\\",\\\"trade_id\\\":\\\"160100027976\\\",\\\"account_type\\\":\\\"trade\\\",\\\"prd_no\\\":\\\"98.873001\\\",\\\"end_amt\\\":\\\"0.0\\\",\\\"end_qty\\\":\\\"0.0\\\",\\\"cash_flow_qty\\\":\\\"0.0\\\",\\\"cash_flow_amt\\\":\\\"0.0\\\",\\\"positive_amt\\\":\\\"0.0\\\",\\\"profit_amt\\\":\\\"0.0\\\",\\\"profit\\\":\\\"\\\\\\\\N\\\",\\\"profit_amt_sum\\\":\\\"\\\\\\\\N\\\",\\\"profit_his\\\":\\\"\\\\\\\\N\\\",\\\"shr_nav\\\":\\\"1.0\\\",\\\"trd_dt\\\":\\\"20201211\\\",\\\"buy_date\\\":\\\"\\\\\\\\N\\\",\\\"model\\\":\\\"fund\\\",\\\"nav_flag\\\":\\\"3\\\",\\\"buy_date_flag\\\":\\\"1\\\",\\\"colum_1\\\":\\\"\\\",\\\"colum_2\\\":\\\"\\\",\\\"colum_3\\\":\\\"\\\",\\\"busi_date\\\":\\\"2020-12-11\\\"}\"}],\"pageindex\":\"1\",\"pagesize\":\"100\",\"totalrecords\":\"1\",\"totalpages\":\"1\"}";
            log.info("请求一" + request.toString());
            log.info("结果一" + result);
            JSONObject j = JSONObject.parseObject(result);
            JSONArray items = j.getJSONArray("response_body");
            for (int i = 0; i < items.size(); i++) {
                Map<String, String> map = (Map<String, String>) items.get(i);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    ResObject jsonValue = JSONObject.parseObject(entry.getValue(), ResObject.class);
                    prods.put(jsonValue.getPrd_no(), jsonValue);
                }
            }
        } catch (Exception e) {
            log.error("返回信息有误" + e.getMessage());
            e.printStackTrace();
        }

        HttpEntity<String> request2 = new HttpEntity<>(jsonObject2.toString(), headers);
        try {
            count.incrementAndGet();
            ResponseEntity<String> postForEntity2 = restTemplate.postForEntity(url, request2, String.class);
            String result2 = "{\"retcode\":\"0000\",\"retmsg\":\"success\",\"responsetime\":\"2020-12-14 09:05:35\",\"response_body\":[{\"stock:87.GF1807\":\"{\\\"pre_amt\\\":\\\"824795.65764\\\",\\\"pre_qty\\\":\\\"8199.09\\\",\\\"trade_id\\\":\\\"160100027976\\\",\\\"account_type\\\":\\\"trade\\\",\\\"prd_no\\\":\\\"87.GF1807\\\",\\\"end_amt\\\":\\\"824894.0467200001\\\",\\\"end_qty\\\":\\\"8199.09\\\",\\\"cash_flow_qty\\\":\\\"0.0\\\",\\\"cash_flow_amt\\\":\\\"0.0\\\",\\\"positive_amt\\\":\\\"0.0\\\",\\\"profit_amt\\\":\\\"98.38908000010997\\\",\\\"profit\\\":\\\"1.1928903733760202E-4\\\",\\\"profit_amt_sum\\\":\\\"61222.05792\\\",\\\"profit_his\\\":\\\"0.07853744630298246\\\",\\\"shr_nav\\\":\\\"100.608\\\",\\\"trd_dt\\\":\\\"20201211\\\",\\\"buy_date\\\":\\\"2019-01-01\\\",\\\"model\\\":\\\"fiancial\\\",\\\"nav_flag\\\":\\\"3\\\",\\\"buy_date_flag\\\":\\\"1\\\",\\\"colum_1\\\":\\\"0.078536\\\",\\\"colum_2\\\":\\\"61222.05792\\\",\\\"colum_3\\\":\\\"1\\\",\\\"busi_date\\\":\\\"2020-12-11\\\"}\"}],\"pageindex\":\"1\",\"pagesize\":\"100\",\"totalrecords\":\"1\",\"totalpages\":\"1\"}";
            log.info("请求二" + request2.toString());
            log.info("结果二" + result2);
            JSONObject j = JSONObject.parseObject(result2);
            JSONArray items = j.getJSONArray("response_body");
            for (int i = 0; i < items.size(); i++) {
                Map<String, String> map = (Map<String, String>) items.get(i);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    ResObject jsonValue = JSONObject.parseObject(entry.getValue(), ResObject.class);
                    prods.put(jsonValue.getPrd_no(), jsonValue);
                }
            }
        } catch (Exception e) {

            log.error("返回信息有误" + e.getMessage());
            e.printStackTrace();
        }

        return prods;
    }


}
