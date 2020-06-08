package com.sddm.flowable;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Log4j2
class formTests {
    @Autowired
    private FormService formService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private FormRepositoryService formRepositoryService;

    @Test
    void passAllTask() throws IOException, InterruptedException {
        InputStream is = new FileInputStream("./src/main/resources/process/mutian-new.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("test.bpmn",text)
                .deploy();//完成部署
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("xjbj1");
        System.out.println("==============开始流程==============");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============填写询价单=============");
        Map<String,Object> properties = new HashMap<>();
        //properties.put("quotationForm",1);
        taskService.complete(task.getId());
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        UserTask userTask = (UserTask) taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        userTask.getExtensionElements()
        System.out.println("task内容: "+ task.toString());
        System.out.println("===============填写报价单(生技)  5s之内完成==============");

        Thread.sleep(10000);

        taskService.complete(task.getId());
        List<Task> tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        System.out.println("==============进入子流程==============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============审核报价单，维护报价生成/退回============");
        properties = new HashMap<>();
        properties.put("status","reject");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============供应商修改报价单==============");
        taskService.complete(task.getId());
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============审核报价单，维护报价生成/通过============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        properties = new HashMap<>();
        properties.put("status","pass");
        taskService.complete(task.getId(),properties);
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstance.getId())
                .singleResult();
        if(rpi==null){
            HistoricProcessInstance hpi = historyService//
                    .createHistoricProcessInstanceQuery()//
                    .processInstanceId(processInstance.getId())//使用流程实例ID查询
                    .singleResult();
            System.out.println(hpi.getId()+"    "+hpi.getStartTime()+"   "+hpi.getEndTime()+"   "+hpi.getDurationInMillis());
            System.out.println("==============流程结束==============");
        }
    }

    @Test
    public void TestMutianXjbj() throws IOException, InterruptedException {
        InputStream is = new FileInputStream("./src/main/resources/process/mutianxjbj-new.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("test.bpmn", text)
                .deploy();//完成部署
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("xjbj");
        System.out.println("==============开始流程==============");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============填写询价单=============");
        Map<String, Object> properties = new HashMap<>();
        properties.put("quotationForm","1");
//        properties.put("quotationForm","2");
        taskService.complete(task.getId(),properties);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        UserTask userTask = (UserTask) taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        userTask.getExtensionElements()
        System.out.println("task内容: " + task.toString());
        System.out.println("===============填写报价单(生技)  5s之内完成==============");
        //Thread.sleep(10000);
        taskService.complete(task.getId());
        List<Task> tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        System.out.println("==============进入子流程==============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============审核报价单，维护报价生成/退回============");
        properties = new HashMap<>();
        properties.put("status", "12");
        taskService.complete(task.getId(), properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============供应商修改报价单==============");
        taskService.complete(task.getId());
        System.out.println("task内容: " + task.toString());
        System.out.println("==============审核报价单，维护报价生成/通过============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        properties = new HashMap<>();
        properties.put("status", "05");
        taskService.complete(task.getId(), properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============主任审核/通过============");
        properties = new HashMap<>();
        properties.put("status", "06");
        taskService.complete(task.getId(), properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============科长审核/通过============");
        properties = new HashMap<>();
        properties.put("status", "07");
        taskService.complete(task.getId(), properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============高级顾问审核/通过============");
        properties = new HashMap<>();
        properties.put("status", "08");
        taskService.complete(task.getId(), properties);

        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstance.getId())
                .singleResult();
        if (rpi == null) {
            HistoricProcessInstance hpi = historyService//
                    .createHistoricProcessInstanceQuery()//
                    .processInstanceId(processInstance.getId())//使用流程实例ID查询
                    .singleResult();
            System.out.println(hpi.getId() + "    " + hpi.getStartTime() + "   " + hpi.getEndTime() + "   " + hpi.getDurationInMillis());
            System.out.println("==============流程结束==============");
        }
    }

    @Test
    public void testXJ() throws IOException, InterruptedException {
        InputStream is = new FileInputStream("./src/main/resources/process/mutian.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("test.bpmn", text)
                .deploy();//完成部署
        Map<String, Object> variables = new HashMap<>();
        System.out.println("===============4个公司=============");
        List<String> assigneeList= Arrays.asList("EnterpriseA","EnterpriseB","EnterpriseC","EnterpriseD");
        assigneeList.forEach(assignee-> System.out.print(assignee+" / "));
        System.out.println(" ");
        variables.put("assigneeList", assigneeList);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("xjbj2",variables);
        System.out.println("==============开始流程==============");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============填写询价单 选择社外=============");
        Map<String, Object> properties = new HashMap<>();
//        properties.put("quotationForm","1");
        properties.put("quotationForm","2");
        taskService.complete(task.getId(),properties);
        System.out.println("task内容: " + task.toString());
        System.out.println("==============进入供应商填写报价单子流程==========");
        System.out.println("===============供应商填写报价单(生技)  5s之内完成  4个实例==============");
    //    Thread.sleep(10000);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        tasks.forEach(task1 -> {
            System.out.println(task1.toString());
            taskService.complete(task1.getId());
        });

        ProcessInstance rpi1 = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstance.getId())
                .singleResult();
    //    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
 //       taskService.complete(task.getId());

        System.out.println("===============进入报价汇总审批子流程 4个实例==============");
        List<Task> tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        System.out.println("================审核报价单，维护报价生成/退回==============");
        tasks1.forEach(task1 -> {
            Map<String,Object> property = new HashMap<>();
            property.put("status", "12");
            taskService.complete(task1.getId(), property);
        });
        System.out.println("==============供应商修改报价单==============");
        tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        tasks1.forEach(task1 -> taskService.complete(task1.getId()));

        System.out.println("==============审核报价单，维护报价生成/通过============");
        tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        tasks1.forEach(task1 -> {
            Map<String,Object> property = new HashMap<>();
            property.put("status", "05");
            taskService.complete(task1.getId(),property);
        });
        System.out.println("==============主任审核/通过============");
        tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        tasks1.forEach(task1 -> {
            Map<String,Object> property = new HashMap<>();
            property.put("status", "06");
            taskService.complete(task1.getId(),property);
        });
        System.out.println("==============科长审核/通过============");
        tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        tasks1.forEach(task1 -> {
            Map<String,Object> property = new HashMap<>();
            property.put("status", "07");
            taskService.complete(task1.getId(),property);
        });
        System.out.println("==============高级顾问审核/通过============");
        tasks1 = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        tasks1.forEach(task1 -> {
            Map<String,Object> property = new HashMap<>();
            property.put("status", "08");
            taskService.complete(task1.getId(),property);
        });
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstance.getId())
                .singleResult();
        if (rpi == null) {
            HistoricProcessInstance hpi = historyService//
                    .createHistoricProcessInstanceQuery()//
                    .processInstanceId(processInstance.getId())//使用流程实例ID查询
                    .singleResult();
            System.out.println(hpi.getId() + "    " + hpi.getStartTime() + "   " + hpi.getEndTime() + "   " + hpi.getDurationInMillis());
            System.out.println("==============流程结束==============");
        }
    }
}
