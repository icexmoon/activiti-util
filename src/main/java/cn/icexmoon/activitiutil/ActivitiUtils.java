package cn.icexmoon.activitiutil;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ActivitiUtils
 * @Description
 * @Author icexmoon@qq.com
 * @Date 2025/6/5 上午11:16
 * @Version 1.0
 */
@Slf4j
public class ActivitiUtils {
    private final ProcessEngine processEngine;

    public ActivitiUtils(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    /**
     * 获取指定进程实例的最近一条待审批任务
     *
     * @param processInstanceId 进程实例id
     * @return 待完成任务
     */
    public Task getLastTask(String processInstanceId) {
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .desc()
                .list();
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取最新的一个激活的流程实例
     *
     * @param processDefinitionKey 流程定义key
     * @return 流程实例
     */
    public ProcessInstance getLastProcessInstance(String processDefinitionKey) {
        List<ProcessInstance> processInstances = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .active()
                .processDefinitionKey(processDefinitionKey)
                .orderByProcessInstanceId().desc()
                .list();
        if (processInstances == null || processInstances.isEmpty()) {
            return null;
        }
        return processInstances.get(0);
    }

    /**
     * 部署出差申请
     *
     * @param bpmn bpmn文件路径
     * @param png  png 文件路径
     * @param name 部署名称
     */
    public void deploy(String bpmn, String png, String name) {
        processEngine.getRepositoryService().createDeployment()
                .addClasspathResource(bpmn)
                .addClasspathResource(png)
                .name(name)
                .deploy();
    }

    /**
     * 启动一个流程实例
     *
     * @param processDefinitionKey 流程定义 key
     * @param businessKey          业务 key
     * @return 已启动的流程实例
     */
    public ProcessInstance start(final String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    public ProcessInstance start(final String processDefinitionKey, Long businessKey, Map<String, Object> variables) {
        return this.start(processDefinitionKey, businessKey.toString(), variables);
    }

    /**
     * 启动一个流程实例，并且自动完成第一个任务
     *
     * @param processDefinitionKey 流程定义key
     * @param variables            流程变量
     * @return 流程实例
     */
    public ProcessInstance startAndNext(final String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey,
                businessKey,
                variables);
        Task lastTask = getLastTask(instance.getId());
        processEngine.getTaskService().complete(lastTask.getId());
        log.info("流程实例[%s]已启动".formatted(instance.getId()));
        return instance;
    }

    public ProcessInstance startAndNext(final String processDefinitionKey, Long businessKey, Map<String, Object> variables) {
        return startAndNext(processDefinitionKey, businessKey.toString(), variables);
    }

    /**
     * 启动一个流程实例，并且自动完成第一个任务
     *
     * @param processDefinitionKey 流程定义key
     * @param variables            流程变量
     * @return 流程实例
     */
    public ProcessInstance startAndNext(final String processDefinitionKey, Map<String, Object> variables) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey,
                variables);
        Task lastTask = getLastTask(instance.getId());
        processEngine.getTaskService().complete(lastTask.getId());
        log.info("流程实例[%s]已启动".formatted(instance.getId()));
        return instance;
    }

    /**
     * 启动一个流程实例，并且自动完成第一个任务
     *
     * @param processDefinitionKey 流程定义key
     * @return 流程实例
     */
    public ProcessInstance startAndNext(final String processDefinitionKey) {
        return startAndNext(processDefinitionKey, null);
    }

    /**
     * 返回指定任务实例的候选人列表
     *
     * @param taskId 任务实例id
     * @return 候选人列表
     */
    public List<String> listCandidates(String taskId) {
        TaskService taskService = processEngine.getTaskService();
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);
        List<String> candidates = new ArrayList<>();
        for (IdentityLink identityLink : identityLinksForTask) {
            if ("candidate".equals(identityLink.getType())) {
                if (identityLink.getUserId() != null) {
                    candidates.add(identityLink.getUserId());
                }
            }
        }
        return candidates;
    }

    /**
     * 列出指定用户可以完成的任务（包括个人任务和作为候选人的任务）
     *
     * @param userId               指定用户id
     * @param processDefinitionKey 流程定义key
     * @return 任务列表
     */
    public List<Task> listCompletableTask(String userId, String processDefinitionKey) {
        // 获取个人任务
        TaskService taskService = processEngine.getTaskService();
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .processDefinitionKey(processDefinitionKey)
                .list();
    }

    /**
     * 列出指定用户可以完成的任务（包括个人任务和作为候选人的任务）
     *
     * @param userId 指定用户id
     * @return 任务列表
     */
    public List<Task> listCompletableTask(String userId) {
        // 获取个人任务
        TaskService taskService = processEngine.getTaskService();
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .list();
    }

    /**
     * 列出需要指定用户审批的流程实例
     *
     * @param userId 指定用户id
     * @return 流程实例列表
     */
    public List<ProcessInstance> listPendingApprovalProcessInstances(String userId) {
        List<Task> tasks = this.listCompletableTask(userId);
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> processInstanceIds = tasks.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        if (processInstanceIds.isEmpty()) {
            return Collections.emptyList();
        }
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .orderByProcessInstanceId().desc()
                .list();
        return processInstances;
    }

    /**
     * 完成任务（会检查指定用户是否有权限完成该任务）
     *
     * @param userId 指定用户id
     * @param taskId 任务id
     */
    public void completeTaskWithCheck(String userId, String taskId) {
        this.completeTaskWithCheck(userId, taskId, null);
    }

    /**
     * 完成任务（会检查指定用户是否有权限完成该任务）
     *
     * @param userId    指定用户id
     * @param taskId    任务id
     * @param variables 变量（变量范围是局部，不是整个工作流实例）
     */
    public void completeTaskWithCheck(String userId, String taskId, Map<String, Object> variables) {
        if (userId == null) {
            throw new RuntimeException("必须指定一个用户id");
        }
        // 检查指定用户是否是任务的委托人
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        // 检查用户是否有权审批该任务
        if (!canApprovalTask(userId, taskId)){
            throw new RuntimeException("用户[%s]无权审批任务[%s]".formatted(userId, taskId));
        }
        // 如果指定用户不是任务的委托人，先获取任务
        if (!userId.equals(task.getAssignee())) {
            taskService.claim(taskId, userId);
        }
        // 完成任务
        taskService.complete(taskId, variables, true);
    }

    /**
     * 检查用户是否有权审批指定任务
     * @param userId 用户id
     * @param taskId 任务id
     * @return 是否有权限
     */
    public boolean canApprovalTask(String userId, String taskId) {
        // 检查指定用户是否是任务的委托人
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException(String.format("任务（%s）不存在！", taskId));
        }
        if (!userId.equals(task.getAssignee())) {
            // 指定用户不是任务的委托人
            // 检查指定用户是否是任务的候选人
            List<String> candidates = listCandidates(taskId);
            if (candidates == null || candidates.isEmpty() || !candidates.contains(userId)) {
                // 指定用户不是任务的候选人
                return false;
            }
        }
        return true;
    }

    /**
     * 获取任务的变量
     *
     * @param taskId 任务id
     * @return 任务关联的变量
     */
    public Map<String, Object> getTaskVariables(String taskId) {
        // 从历史记录中获取任务的变量
        HistoryService historyService = processEngine.getHistoryService();
        Map<String, Object> variablesLocal = new HashMap<>();
        List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                .taskId(taskId)
                .list();
        for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
            variablesLocal.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
        }
        return variablesLocal;
    }


    /**
     * 获取任务实例的一个执行人（委托人或候选人）
     *
     * @param taskId 任务实例id
     * @return 用户id
     */
    public String getTaskExecutor(String taskId) {
        // 检查任务有没有委托人，如果有，直接返回
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException(String.format("不存在ID为(%s)的任务实例", taskId));
        }
        if (task.getAssignee() != null) {
            return task.getAssignee();
        }
        // 获取一个候选人并返回
        List<String> candidates = listCandidates(taskId);
        if (candidates == null || candidates.isEmpty()) {
            // 既没有委托人也没有候选人
            return null;
        }
        return candidates.get(0);
    }

    /**
     * 推动流程实例到下个活动
     *
     * @param processInstanceId 流程实例id
     */
    public void nextActivity(String processInstanceId) {
        Task lastTask = this.getLastTask(processInstanceId);
        if (lastTask == null) {
            return;
        }
        String executor = this.getTaskExecutor(lastTask.getId());
        this.completeTaskWithCheck(executor, lastTask.getId());
    }

    /**
     * 返回当前进程实例的任务列表
     *
     * @param processInstanceId 进程实例id
     * @return 任务列表
     */
    public List<Task> listCurrentTasks(String processInstanceId) {
        return processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
    }

    /**
     * 打印进程实例的当前任务
     *
     * @param processInstanceId 进程实例id
     */
    public void printCurrentTasks(String processInstanceId) {
        List<Task> tasks = listCurrentTasks(processInstanceId);
        for (Task task : tasks) {
            log.info(String.format("任务ID(%s)，任务名称(%s)，委托人(%s)", task.getId(), task.getName(), task.getAssignee()));
        }
    }

    public void printProcessInstance(String processInstanceId) {
        log.info(String.format("============进程实例（%s）============", processInstanceId));
        this.printHistoryTasks(processInstanceId);
        this.printCurrentTasks(processInstanceId);
        log.info("=====================================");
    }

    /**
     * 打印进程实例的历史任务
     *
     * @param processInstanceId 进程实例id
     */
    public void printHistoryTasks(String processInstanceId) {
        List<HistoricTaskInstance> taskInstances = this.listHistoryTasks(processInstanceId);
        for (HistoricTaskInstance taskInstance : taskInstances) {
            log.info(String.format("任务ID(%s)，任务名称(%s)，审批人(%s)", taskInstance.getId(), taskInstance.getName(), taskInstance.getAssignee()));
        }
    }

    /**
     * 获取进程实例的历史任务列表
     *
     * @param processInstanceId 进程实例id
     * @return 历史任务列表
     */
    public List<HistoricTaskInstance> listHistoryTasks(String processInstanceId) {
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime().asc()
                .list();
        return taskInstances;
    }

    /**
     * 是否当前任务
     *
     * @param processInstanceId 进程实例id
     * @param taskName          任务名称
     * @return 是否当前任务
     */
    public boolean isCurrentTask(String processInstanceId, String taskName) {
        Task currentTask = getCurrentTask(processInstanceId, taskName);
        return currentTask != null;
    }

    /**
     * 获取进程实例的一个当前任务
     *
     * @param processInstanceId 进程实例id
     * @param taskName          任务名称
     * @return
     */
    public Task getCurrentTask(String processInstanceId, String taskName) {
        if (taskName == null) {
            return null;
        }
        List<Task> tasks = listCurrentTasks(processInstanceId);
        for (Task task : tasks) {
            if (taskName.equals(task.getName())) {
                return task;
            }
        }
        return null;
    }

    /**
     * 完成进程实例的指定任务
     *
     * @param processInstanceId 进程实例id
     * @param taskName          任务名称
     */
    public void completeTask(String processInstanceId, String taskName) {
        // 检查任务是否为当前任务
        if (!isCurrentTask(processInstanceId, taskName)) {
            throw new RuntimeException("任务（%s）不是进程实例（%s）的当前任务！".formatted(taskName, processInstanceId));
        }
        Task currentTask = getCurrentTask(processInstanceId, taskName);
        if (currentTask == null) {
            throw new RuntimeException("进程实例没有找到与任务名称匹配的当前任务");
        }
        processEngine.getTaskService().complete(currentTask.getId());
    }

    /**
     * 拒绝任务（删除任务所属工作流实例）
     *
     * @param taskId    任务id
     * @param reason    原因
     * @param userId    拒绝任务的人
     * @param variables 环境变量
     */
    public void rejectTask(String taskId, String userId, String reason, Map<String, Object> variables) {
        // 检查用户是否有权审批该任务
        if (!canApprovalTask(userId, taskId)){
            throw new RuntimeException("用户[%s]无权审批任务[%s]".formatted(userId, taskId));
        }
        TaskService taskService = processEngine.getTaskService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.setVariablesLocal(taskId, variables);
        taskService.claim(taskId, userId);
        // 如果流程还没有结束，删除
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        if (processInstance != null) {
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), reason);
        }
    }
}
