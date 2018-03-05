package me.rainking.baike.repository;

import me.rainking.baike.model.Baike;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * @Description:
 * @Author: Rain
 * @Date: 2018/3/3 14:20
 */
@SpringBootTest
@RunWith(SpringRunner.class)
//@Slf4j
public class BaikeRepositoryTest {

    /**
     * 需要爬取词条的根分类
     */
    @Value("${nameOfRootCategory}")
    private String nameOfRootCategory;

    @Autowired
    BaikeRepository baikeRepository;

    @Test
    public void saveBaike(){
        Baike baike = new Baike();
        baike.setTitle("baikeTitle");
        baike.setSummary("baikeSummary");
        baike.setCategory("baikeCategory");
        baike = baikeRepository.save(baike);
//        log.info(baike.toString());
    }

    @Test
    public void countByTitleTest(){
        System.out.println(baikeRepository.countByTitle("贺龙"));
    }

    @Test
    public  void encoder() throws Exception{

        String category = URLEncoder.encode(nameOfRootCategory, "utf-8");
        Assert.assertEquals(category,"%E5%86%9B%E4%BA%8B");
    }


}