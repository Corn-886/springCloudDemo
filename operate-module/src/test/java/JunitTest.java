import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.OperateApplication;
import org.example.dto.ProdHisExcelDto;
import org.example.service.DataCloudService;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OperateApplication.class)
@Slf4j
public class JunitTest {
    @Autowired
    DataCloudService dataCloudService;

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testData() {
        String url = "http://wmuat.gf.com.cn/api/agg/api/tx/service/eval/prod/fund/detail/qryNetWorthPage?fundId=8195566&page=1&pageSize=5";
        String result = restTemplate.getForEntity(url, String.class).getBody();
        List<ProdHisExcelDto> p = new ArrayList<>();
        if (StringUtils.isNotBlank(result)) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            p = jsonObject.getJSONArray("data").toJavaList(ProdHisExcelDto.class);
        }
        System.out.println(p);
    }


}
