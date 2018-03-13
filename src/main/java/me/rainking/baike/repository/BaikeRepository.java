package me.rainking.baike.repository;

import me.rainking.baike.model.Baike;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Description:
 * @Author: Rain
 * @Date: 2018/2/27 11:12
 */
public interface BaikeRepository extends MongoRepository<Baike, String> {

    /**
     * 根据词条标题统计数量
     * @param title 词条标题
     * @return 数量
     */
    long countByTitle(String title);

}
