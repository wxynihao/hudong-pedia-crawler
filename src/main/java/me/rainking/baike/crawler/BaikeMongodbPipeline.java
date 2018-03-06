package me.rainking.baike.crawler;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BaikeMongodbPipeline implements Pipeline {

    private final BaikeRepository baikeRepository;

    @Autowired
    public BaikeMongodbPipeline(BaikeRepository baikeRepository) {
        this.baikeRepository = baikeRepository;
    }

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     * @param task        task
     */
    @Override
    public void process(ResultItems resultItems, Task task) {


        String title = resultItems.get("title");

        Baike baike = new Baike(resultItems.get("category"),
                resultItems.get("summary"),
                title,
                resultItems.get("content"),
                resultItems.get("inforBox")
        );

        if (baikeRepository.countByTitle(title) == 0) {
            baikeRepository.save(baike);
            log.warn("新增词条\t" + title + "。\n");
        }
    }
}
