package cn.icexmoon.activitiutil.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : 魔芋红茶
 * @version : 1.0
 * @Project : activiti-maven-demo
 * @Package : cn.icexmoon.demo.listener
 * @ClassName : .java
 * @createTime : 2025/5/15 20:14
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description : 如果任务没有指定候选人，自动获取候选人
 */
public class ReCandidateListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        // 任务已经有委托人，不进行处理
        if (delegateTask.getAssignee() != null) {
            return;
        }
        // 如果指定委托人，不进行处理
        if (candidates != null && !candidates.isEmpty()) {
            return;
        }
        // 根据任务名称匹配候选人
        Set<String> targetCandidates = getCandidates(getProcessCreator(delegateTask.getProcessInstanceId()), delegateTask.getName());
        if (targetCandidates == null || targetCandidates.isEmpty()) {
            // 没有匹配到候选人，说明流程描述文件有误
            // TODO 记录错误
            return;
        }
        for (String targetCandidate : targetCandidates) {
            // 添加候选人
            delegateTask.addCandidateUser(targetCandidate);
        }
    }

    private String getProcessCreator(String processInstanceId) {
        // TODO 实现根据进程实例id获取申请人id
        return "ZhangSan";
    }

    private Set<String> getCandidates(String processCreator, String taskName) {
        // TODO 实现根据任务名称和流程申请人确定审批候选人
        return Set.of("Jack", "Brus");
    }
}
