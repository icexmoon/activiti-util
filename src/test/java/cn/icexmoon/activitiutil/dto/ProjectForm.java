package cn.icexmoon.activitiutil.dto;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : 魔芋红茶
 * @version : 1.0
 * @Project : activiti-maven-demo
 * @Package : cn.icexmoon.demo.dto
 * @ClassName : .java
 * @createTime : 2025/5/16 12:12
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description :
 */
@Data
public class ProjectForm extends BaseForm{
    private String name; // 项目名称
    private Integer amount; // 项目预算
    public ProjectForm(String creator) {
        super(creator);
    }
}
