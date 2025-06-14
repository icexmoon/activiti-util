package cn.icexmoon.activitiutil;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivitiTests
 * @Description
 * @Author icexmoon@qq.com
 * @Date 2025/6/5 上午11:26
 * @Version 1.0
 */
public class ActivitiTests {
    private final ProcessEngine processEngine;
    private final String PROCESS_DEFINITION_KEY = "candidate";
    private final ActivitiUtils activitiUtils = new ActivitiUtils(ProcessEngines.getDefaultProcessEngine());

    public ActivitiTests() {
        processEngine = ProcessEngines.getDefaultProcessEngine();
    }

    @Test
    public void testBpmnDeploy() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .name("出差申请")
                .addClasspathResource("bpmn/travel.bpmn20.xml")
                .addClasspathResource("bpmn/travel.png")
                .deploy();
        System.out.println(deploy.getId());
    }

    @Test
    public void testListApprovalInstances() {
        List<ProcessInstance> processInstances = activitiUtils.listPendingApprovalProcessInstances("Jack");
        for (ProcessInstance processInstance : processInstances) {
            activitiUtils.printProcessInstance(processInstance.getId());
        }
    }

    @Test
    public void testStart() {
        Map<String, Object> vars = new HashMap<>();
        ProcessInstance processInstance = activitiUtils.startAndNext("travel_apply", vars);
        activitiUtils.printProcessInstance(processInstance.getId());
    }

    @Test
    public void testNext() {
        activitiUtils.nextActivity("497501");
    }

    @Test
    public void testPrintInstance() {
        ProcessInstance lastProcessInstance = activitiUtils.getLastProcessInstance("travel_apply");
        activitiUtils.printProcessInstance(lastProcessInstance.getId());
    }

    @Test
    public void testCompleteTask() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("opinion", "同意");
        ProcessInstance lastProcessInstance = activitiUtils.getLastProcessInstance("travel_apply");
        Task lastTask = activitiUtils.getLastTask(lastProcessInstance.getId());
        String taskExecutor = activitiUtils.getTaskExecutor(lastTask.getId());
        activitiUtils.completeTaskWithCheck(taskExecutor, lastTask.getId(), vars);
        System.out.println("任务[%s]已完成".formatted(lastTask.getId()));
    }

    @Test
    public void testGetVars() {
        Map<String, Object> taskVariables = activitiUtils.getTaskVariables("512508");
        System.out.println(taskVariables);
    }

    @Test
    public void testRejectTask() {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("opinion", "不同意");
            vars.put("status", 1);
            activitiUtils.rejectTask("527516", "Brus", "审批未通过", vars);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListProcessInstances(){
        List<HistoricProcessInstance> processInstances = activitiUtils.listHistoricProcessInstances("ZhangSan", null, null);
        for (HistoricProcessInstance processInstance : processInstances) {
            activitiUtils.printProcessInstance(processInstance.getId());
        }
    }
}
