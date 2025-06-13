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
 * @createTime : 2025/5/15 10:59
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description : 出差申请单
 */
@Data
public class TravelForm extends BaseForm {
    // 出差时长（单位：天）
    private int days;

    public TravelForm(String creator, int days) {
        super(creator);
        this.days = days;
    }
}
