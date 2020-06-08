package com.sddm.flowable;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lin_jie
 * @date 2020/6/2  16:59
 **/

@SpringBootTest
public class TestContronller {

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

    @RequestMapping("/index")
    public String po() {
        return "1";
    }


    @RequestMapping("/test")
    public String passAllTask() throws IOException, InterruptedException {
        InputStream is = new FileInputStream("./src/main/resources/process/mutian-new.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("test.bpmn", text)
                .deploy();//完成部署
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("xjbj1");
        System.out.println("==============开始流程==============\n");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============填写询价单=============");
        Map<String, Object> properties = new HashMap<>();
        //properties.put("quotationForm",1);
        taskService.complete(task.getId());
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        UserTask userTask = (UserTask) taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        userTask.getExtensionElements()
        System.out.println("task内容: " + task.toString());
        System.out.println("===============填写报价单(生技)  5s之内完成==============");

        Thread.sleep(10000);

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
        properties.put("status", "reject");
        taskService.complete(task.getId(), properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============供应商修改报价单==============");
        taskService.complete(task.getId());
        System.out.println("task内容: " + task.toString());
        System.out.println("==============审核报价单，维护报价生成/通过============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        properties = new HashMap<>();
        properties.put("status", "pass");
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
        return "1";
    }


    @Test
    public String testXJ() throws IOException, InterruptedException {
        InputStream is = new FileInputStream("./src/main/resources/process/mutian.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("test.bpmn", text)
                .deploy();//完成部署
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("xjbj2");
        System.out.println("==============开始流程==============");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: " + task.toString());
        System.out.println("==============填写询价单=============");
        Map<String, Object> properties = new HashMap<>();
//        properties.put("quotationForm","1");
        properties.put("quotationForm","2");
        taskService.complete(task.getId(),properties);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        UserTask userTask = (UserTask) taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        userTask.getExtensionElements()
        System.out.println("task内容: " + task.toString());
        System.out.println("===============填写报价单(生技)  5s之内完成==============");
        Thread.sleep(10000);
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
        return "1";
    }
}
