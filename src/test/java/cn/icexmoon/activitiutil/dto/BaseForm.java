package cn.icexmoon.activitiutil.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : 魔芋红茶
 * @version : 1.0
 * @Project : activiti-maven-demo
 * @Package : cn.icexmoon.demo.dto
 * @ClassName : .java
 * @createTime : 2025/5/15 10:56
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description : 表单抽象基类
 */
@Data
public abstract class BaseForm implements Serializable {
    private String creator; //表单发起人

    public BaseForm(String creator) {
        this.creator = creator;
    }
}
