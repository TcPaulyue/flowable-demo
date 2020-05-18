package com.sddm.flowable;

import com.alibaba.fastjson.JSONObject;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MyFormService {

    private final TaskService taskService;

    private final RuntimeService runtimeService;

    @Autowired
    public MyFormService(TaskService taskService,RuntimeService runtimeService){
        this.taskService = taskService;
        this.runtimeService = runtimeService;
    }

    public JSONObject getStartFormData(String s) {
        Map<String,Object> map = runtimeService.getVariables(s);
        return JSONObject.parseObject(map.get("document").toString());
    }

    public ProcessInstance submitStartFormData(String s) {
        return null;
    }

    public JSONObject getTaskFormData(String s) {
        Map<String, Object> variablesMap = runtimeService.getVariables(s);
        return JSONObject.parseObject(variablesMap.get("document").toString());
    }

    public void submitTaskFormData(String taskExecutionId, JSONObject documentContent) {
        runtimeService.setVariable(taskExecutionId, "document", documentContent);
    }

    public void saveFormData(String s, Map<String, String> map) {

    }

    public String getStartFormId(String s) {
        return null;
    }

    public String getTaskFormId(String s) {
        return  taskService.createTaskQuery()
                .processInstanceId(s).singleResult().getDescription();
    }
}
