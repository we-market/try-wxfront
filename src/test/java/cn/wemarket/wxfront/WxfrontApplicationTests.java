package cn.wemarket.wxfront;

import cn.wemarket.wxfront.biz.server.WxfrontApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WxfrontApplication.class})
public class WxfrontApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(WxfrontApplicationTests.class);
    @Test
    public void contextLoads() {
        LOGGER.error("这是一条错误信息");
    }


}

