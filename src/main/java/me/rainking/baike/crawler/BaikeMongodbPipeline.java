package me.rainking.baike.crawler;

import me.rainking.baike.model.Baike;
import me.rainking.baike.repository.BaikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @Description: 爬虫的存储逻辑
 * @Author: Rain
 * @Date: 2018/3/4 0:54
 */
@Component("baikeMongodbPipeline")
public class BaikeMongodbPipeline implements Pipeline {

    @Autowired
    private BaikeRepository baikeRepository;

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     * @param task        task
     */
    @Override
    public void process(ResultItems resultItems, Task task) {


        Baike baike = new Baike(resultItems.get("category"),
                resultItems.get("summary"),
                resultItems.get("title"),
                resultItems.get("content"),
                resultItems.get("inforBox")
        );

        baikeRepository.save(baike);

    }
}
