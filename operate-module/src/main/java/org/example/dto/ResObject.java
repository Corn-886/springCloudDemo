package org.example.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.mockito.internal.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Data
public class ResObject {
    //昨日盈亏
    private String profit_amt;
    // 1/0 表示20190101之前/后买入
    private String buy_date_flag;
    // 产品编号
    private String prd_no;
    //累计收益率
    private String profit_his;
    //累计盈亏
    private String profit_amt_sum;

    public String getPrd_no() {
        if (StringUtils.isNotEmpty(this.prd_no)) {
            List<String> p = Arrays.asList(prd_no.split("\\."));
            return p.get(1);
        }
        return prd_no;
    }
    //累计收益率
    public String getProfit_his() {
        //不是数字类型返回空
        if(StringUtils.isBlank(profit_his)||"\\N".equals(profit_his)){
            return null;
        }
        if (StringUtils.isNotEmpty(this.buy_date_flag)) {
            // 19年前收益率不展示
            if ("1".equals(buy_date_flag)) {
                return null;
            } else {
                BigDecimal bigDecimal = new BigDecimal(profit_his).setScale(4, RoundingMode.HALF_UP);
                return bigDecimal.toString();
            }
        }
        return profit_his;
    }

    //昨日盈亏
    public String getProfit_amt() {
        //不是数字类型返回空
        if(StringUtils.isBlank(profit_amt)||"\\N".equals(profit_amt)){
            return null;
        }
        if (this.profit_amt != null) {
            BigDecimal bigDecimal = new BigDecimal(profit_amt).setScale(4, RoundingMode.HALF_UP);
            return bigDecimal.toString();
        }
        return profit_amt;
    }

    public String getProfit_amt_sum() {
        if(StringUtils.isBlank(profit_amt_sum)||"\\N".equals(profit_amt_sum)){
            return null;
        }
        if (this.profit_amt_sum != null) {
            BigDecimal bigDecimal = new BigDecimal(profit_amt_sum).setScale(4, RoundingMode.HALF_UP);
            return bigDecimal.toString();
        }

        return profit_amt_sum;
    }

}
