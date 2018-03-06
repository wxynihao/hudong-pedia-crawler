package me.rainking.baike.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * @Description: 最终需要的百科数据（如不使用lombok插件，需添加getter/setter方法）
 * @Author: Rain
 * @Date: 2018/2/27 10:02
 */
@Data
public class Baike {

    /**
     * id
     */
    @Id
    private String id;

    /**
     * category 开放目录
     */
    private String category;

    /**
     * summary 摘要
     */
    private String summary;

    /**
     * title 词条标题
     */
    private String title;

    /**
     * content 内容
     */
    private Map<String, String> content;

    /**
     * inforBox 信息框内容
     */
    private Map<String, String> inforBox;

    public Baike() {
    }

    public Baike(String category, String summary, String title, Map<String, String> content, Map<String, String> inforBox) {
        this.category = category;
        this.summary = summary;
        this.title = title;
        this.content = content;
        this.inforBox = inforBox;
    }

    @Override
    public String toString() {
        return "Baike{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", summary='" + summary + '\'' +
                ", title='" + title + '\'' +
                ", content=" + content +
                ", inforBox=" + inforBox +
                '}';
    }
}
