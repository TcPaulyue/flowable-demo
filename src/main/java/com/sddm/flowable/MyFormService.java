package com.sddm.flowable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sddm.flowable.domain.Schema;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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

    JSONObject submitStartFormData(String processInstanceId, String query) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000"+"/api/documents/query";
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,query,JSONObject.class);

        Map<String,Object> map = new HashMap<>();
        map.put("document",response.getBody());
        runtimeService.setVariables(processInstanceId,map);
        return response.getBody();
    }

    String getStartFormId(String s) {
        return runtimeService.getVariables(s).get("schemaId").toString();
    }

    JSONObject getTaskFormData(String s) {
        Map<String, Object> variablesMap = runtimeService.getVariables(s);
        return JSONObject.parseObject(variablesMap.get("document").toString());
    }

    JSONObject submitTaskFormData(String taskExecutionId, String query) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000"+"/api/documents/query";
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,query,JSONObject.class);
        runtimeService.setVariable(taskExecutionId, "document", response.getBody());
        return response.getBody();
    }

    public void saveFormData(String s, Map<String, String> map) {
        runtimeService.setVariables(s,map);
    }

    Schema getFormDefinition(String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000" + "/api/fomily/{fomilyId}";
        Map<String, Object> params = new HashMap<>();
        params.put("fomilyId", id);
        ResponseEntity<Schema> response = restTemplate.getForEntity(uri,Schema.class,params);
        return response.getBody();
    }

    String getTaskFormId(String s) {
        return  taskService.createTaskQuery()
                .processInstanceId(s).singleResult().getDescription();
    }
}
