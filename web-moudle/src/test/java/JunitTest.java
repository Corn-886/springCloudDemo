import com.example.WebApplication;
import com.example.dao.ProdMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebApplication.class)
@Slf4j
public class JunitTest {
    @Autowired
    ProdMapper prodMapper;

    @Test
    public void testMapper(){
      log.info(""+  prodMapper.selectTest());

    }


}
