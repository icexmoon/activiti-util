package cn.icexmoon.activitiutil.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : 魔芋红茶
 * @version : 1.0
 * @Project : activiti-maven-demo
 * @Package : cn.icexmoon.demo.listener
 * @ClassName : .java
 * @createTime : 2025/5/15 12:41
 * @Email : icexmoon@qq.com
 * @Website : https://icexmoon.cn
 * @Description : 打印任务实例的局部变量
 */
public class LocalVariablesPrintListener implements TaskListener {
    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    @Override
    public void notify(DelegateTask delegateTask) {
        // 打印当前任务实例的局部变量
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> variablesLocal = taskService.getVariablesLocal(delegateTask.getId());
        System.out.println("===========" + delegateTask.getName() + "==========");
        variablesLocal.forEach((key, val) -> {
            System.out.print("key:" + key + ", ");
            System.out.println("val:" + val);
        });
        System.out.println("===========End===========");
    }
}
