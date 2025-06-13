package cn.icexmoon.activitiutil;

import cn.icexmoon.activitiutil.dto.TravelForm;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CandidateTests
 * @Description
 * @Author icexmoon@qq.com
 * @Date 2025/6/5 上午11:56
 * @Version 1.0
 */
public class CandidateTests {
    private final String PROCESS_DEFINITION_KEY = "candidate";
    private final ActivitiUtils activitiUtils = new ActivitiUtils(ProcessEngines.getDefaultProcessEngine());

    /**
     * 部署出差申请
     */
    @Test
    public void deploy() {
        final String BPMN = "bpmn/candidate.bpmn20.xml";
        final String PNG = "bpmn/candidate.png";
        String name = "出差申请v4";
        activitiUtils.deploy(BPMN, PNG, name);
    }

    /**
     * 启动一个出差申请
     */
    @Test
    public void startTravelApply() {
        TravelForm travelForm = new TravelForm("ZhangSan", 6);
        Map<String, Object> variables = new HashMap<>();
        variables.put("form", travelForm);
        ProcessInstance processInstance = activitiUtils.startAndNext(PROCESS_DEFINITION_KEY, variables);
        activitiUtils.printCurrentTasks(processInstance.getId());
    }
}
