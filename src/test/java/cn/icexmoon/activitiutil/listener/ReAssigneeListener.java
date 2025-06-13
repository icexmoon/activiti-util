package cn.icexmoon.activitiutil.listener;

import cn.icexmoon.activitiutil.dto.BaseForm;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : 魔芋红茶
 * @version : 1.0
 * @Project : activiti-maven-demo
 * @Package : cn.icexmoon.demo.listener
 * @ClassName : .java
 * @createTime : 2025/5/14 20:44
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description : 重新定义委托人的监听器
 */
public class ReAssigneeListener implements TaskListener {
    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    @Override
    public void notify(DelegateTask delegateTask) {
        if ("create".equals(delegateTask.getEventName())) {
            // 只在任务实例创建后，指定委托人之前生效
            // 根据用户任务名称的不同进行不同处理
            String assignee = null;
            String userId = getApplyUserId(delegateTask.getExecutionId());
            switch (delegateTask.getName()) {
                case "经理审批":
                    assignee = getAssigneeUserId(userId, "manager");
                    break;
                case "高级经理审批":
                    assignee = getAssigneeUserId(userId, "highManager");
                    break;
                case "财务审批":
                    assignee = getAssigneeUserId(userId, "finance");
                    break;
                default:
                    // 不做任何处理
            }
            if (assignee != null) {
                TaskService taskService = processEngine.getTaskService();
                taskService.claim(delegateTask.getId(), assignee);
            }
        }
    }

    private String getAssigneeUserId(String applyUserId, String assigneeRole) {
        // TODO 根据申请人用户 id 和当前审批环节职级获取审批人 id
        // 这里使用假数据
        switch (assigneeRole) {
            case "manager":
                return "Jack";
            case "highManager":
                return "Lili";
            case "finance":
                return "James";
        }
        return null;
    }

    private String getApplyUserId(String executionId) {
        // 从运行时获取绑定的 self 变量作为流程发起人返回
        RuntimeService runtimeService = processEngine.getRuntimeService();
        String self = (String) runtimeService.getVariable(executionId, "self");
        if (self == null) {
            // 尝试通过表单信息获取流程发起人
            BaseForm form = (BaseForm) runtimeService.getVariable(executionId, "form");
            if (form == null){
                throw new RuntimeException(String.format("执行id(%s)缺少变量self", executionId));
            }
            return form.getCreator();
        }
        return self;
    }
}
