# Activiti-util
对 Activiti 原生 API 进行封装，更具易用性。

# 安装

通过 Maven 安装依赖：

```xml
<dependency>
    <groupId>cn.icexmoon</groupId>
    <artifactId>activiti-util</artifactId>
    <version>1.0.3</version>
</dependency>
```

本项目基于 Activiti 8.7.0 开发，使用本依赖的项目需要引入 Activiti 8.7.0 以上依赖。比如：

```xml
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>activiti-spring-boot-starter</artifactId>
    <version>8.7.0</version>
</dependency>
```

本项目的 Java 语言级别是 21，JVM 版本建议 >= 21。

# 整合

`ActivitiUtils`依赖于`ProcessEngine`：

```java
@Slf4j
public class ActivitiUtils {
    private final ProcessEngine processEngine;

    public ActivitiUtils(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }
    // ...
}
```

可以利用`ProcessEngines`构建：

```java
ActivitiUtils activitiUtils = new ActivitiUtils(ProcessEngines.getDefaultProcessEngine());
```

如果是 Spring 项目，可以注入`ActivitiUtils`类型的 Bean：

```java
@Configuration
public class ActivitiConfig {
    @Bean
    public ActivitiUtils activitiUtils(ProcessEngine processEngine) {
        return new ActivitiUtils(processEngine);
    }
}
```

# 部署工作流

仅提供一个简单封装：

```java
final String BPMN = "bpmn/candidate.bpmn20.xml";
final String PNG = "bpmn/candidate.png";
String name = "出差申请v4";
activitiUtils.deploy(BPMN, PNG, name);
```

如果有更复杂的需要，比如通过上传文件部署，可以使用 Activiti 原生 API，比如：

```java
// 添加流程定义
Deployment deploy = repositoryService.createDeployment()
    .name(name)
    .addInputStream(bpmn2FileOriginalFilename, bpmnFile.getInputStream())
    .addInputStream(pngFileOriginalFilename, pngFile.getInputStream())
    .deploy();
```

> 完整示例可以看[这里](https://github.com/icexmoon/learn-activiti/blob/main/ch4/oa-service/src/main/java/cn/icexmoon/oaservice/service/impl/ProcessDefinitionServiceImpl.java#L130)。

# 操作工作流

## 启动并自动完成第一个任务

启动并自动完成第一个待处理的任务：

```java
Map<String, Object> vars = new HashMap<>();
vars.put("applier", applyInstance.getUserId().toString());
vars.put("days", applyInstance.getFormData().getExtraData().get("days"));
vars.put("budget", applyInstance.getFormData().getExtraData().get("budget")); 
ProcessInstance processInstance = activitiUtils.startAndNext(applyInstance.getProcessKey(), applyInstance.getId(), vars);
```

这个封装的用途是，如果项目设置了某种 Activiti 任务监听（TaskListener），比如为每个职位审批环节（经理审批等）配置一个，在进入审批环节时（event=create），在监听器内按组织架构规则分配审批人作为用户任务（UserTask）的候选人（Candidate）。这里有个问题，当监听的任务是 BPMN2 工作流中的第一个活动（Activity）时，此时运行时的相关表中并未生成工作流实例，如果访问任务所属工作流实例属性，就会产生一个 Null 指针异常。因此就需要在开始事件（StartEnvent）后添加一个用户任务，可以命名为“创建申请”。该任务不需要指定委托人（Assignee），直接由程序自动完成，以确保之后的任务监听能正常执行。

## 完成指定任务

```java
activitiUtils.completeTaskWithCheck(taskExecutor, lastTask.getId(), vars);
```

审批环节通过时可以调用此 API。

`taskExecutor`应该是有权操作任务的人（委托人或候选人），否则会报错。`vars`会被添加为任务环境变量（非工作流实例变量）。

## 拒绝指定任务

```java
activitiUtils.rejectTask("527516", "Brus", "审批未通过", vars);
```

审批环节未通过时可以调用此 API。

参数`Brus`指拒绝任务的人，必须具备操作任务的权限（委托人或候选人），否则会报错。执行该 API 后会删除任务所属工作流实例，历史记录中相应的任务会有字段表示在该任务环节执行了工作流删除动作。任务的执行状态（已通过/未通过）应当由任务变量（vars）中记录。

# 查询

## 查询指定用户的待审批工作流实例

```java
List<ProcessInstance> processInstances = activitiUtils.listPendingApprovalProcessInstances("Jack");
for (ProcessInstance processInstance : processInstances) {
    activitiUtils.printProcessInstance(processInstance.getId());
}
```

## 查询指定用户审批过的工作流实例

```java
List<HistoricProcessInstance> processInstances = activitiUtils.listHistoricProcessInstances("ZhangSan", null, null);
for (HistoricProcessInstance processInstance : processInstances) {
    activitiUtils.printProcessInstance(processInstance.getId());
}
```

## 获取指定流程定义的最新一个实例

```java
ProcessInstance lastProcessInstance = activitiUtils.getLastProcessInstance("travel_apply");
```

可以用这个封装实现自动化测试。

## 获取一个任务的可执行人

```java
String taskExecutor = activitiUtils.getTaskExecutor(lastTask.getId());
```

获取到的人是任务的委托人或候选人。

## 获取任务的环境变量

```java
Map<String, Object> taskVariables = activitiUtils.getTaskVariables("512508");
```

# 日志

## 打印工作流信息

在日志中打印工作流信息，用于调试。

```java
activitiUtils.printProcessInstance(processInstance.getId());
```

更多未列举的 API 可以直接查看源码。

The End.