package cn.icexmoon.activitiutil.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
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
 * @createTime : 2025/5/15 13:07
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description :
 */
public class SetLocalVariablesListener implements TaskListener {
    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    @Override
    public void notify(DelegateTask delegateTask) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.setVariableLocal(delegateTask.getExecutionId(), "local_msg", "hello");
    }
}
