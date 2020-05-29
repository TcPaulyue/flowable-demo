package com.sddm.flowable;

import lombok.extern.log4j.Log4j2;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
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
    void passAllTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("mutian-process");
        System.out.println("==============开始流程==============");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============选择社外(N)==============");
        Map<String,Object> properties = new HashMap<>();
        properties.put("property","社外(N)");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        UserTask userTask = (UserTask) taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        userTask.getExtensionElements()
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============finish生成社外报价单==============");
        taskService.complete(task.getId());
        System.out.println("==============finish发送邮件==============");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============finish供应商接收报价单==============");
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============没有超时=============");
        properties = new HashMap<>();
        properties.put("property","否");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============finish提交报价单==============");
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============通过报价汇总==============");
        properties = new HashMap<>();
        properties.put("property","通过");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============finish作成提交审核==============");
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============主任审核通过==============");
        properties = new HashMap<>();
        properties.put("property","通过");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============科长审核通过==============");
        properties = new HashMap<>();
        properties.put("property","通过");
        taskService.complete(task.getId(),properties);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());
        System.out.println("==============高级顾问审核通过==============");
        properties = new HashMap<>();
        properties.put("property","通过");
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
}
