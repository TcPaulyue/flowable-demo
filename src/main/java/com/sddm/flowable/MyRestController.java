package com.sddm.flowable;

import com.alibaba.fastjson.JSONObject;
import com.sddm.flowable.domain.Schema;
import com.sddm.flowable.service.MyTaskListener;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyRestController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    @Autowired
    MyTaskListener myTaskListener;

    @Autowired
    MyService myService;

    @Autowired
    public MyRestController(RuntimeService runtimeService
            , TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @PostMapping(value="/process")
    public String startProcessInstance() {
        return runtimeService.startProcessInstanceByKey("formRequest")
                .getProcessInstanceId();
    }
    @GetMapping(value = "/schemaContent/{processInstanceId}")
    public Schema getSchemaContent(@PathVariable String processInstanceId){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return null;
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        String fomilyId = task.getDescription();
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000" + "/api/fomily/{fomilyId}";
        Map<String, Object> params = new HashMap<>();
        params.put("fomilyId", fomilyId);
        ResponseEntity<Schema> response = restTemplate.getForEntity(uri,Schema.class,params);
        return response.getBody();
    }

    @PostMapping(value = "/completeTask/{processInstanceId}")
    public JSONObject completeTask(@PathVariable String processInstanceId,
                                       @RequestBody String query){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return new JSONObject();
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        runtimeService.setVariable(task.getExecutionId(), "queryExp", query);
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000"+"/api/documents/query";
        JSONObject postData = new JSONObject();
        postData.put("query", query);
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,query,JSONObject.class);
        taskService.complete(task.getId());
        return response.getBody();
    }

    @RequestMapping(value="/tasks", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public List<TaskRepresentation> getTasks(@RequestParam String assignee) {
        List<Task> tasks = myService.getTasks(assignee);
        List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
        for (Task task : tasks) {
            dtos.add(new TaskRepresentation(task.getId(), task.getName()));
        }
        return dtos;
    }

    static class TaskRepresentation {

        private String id;
        private String name;

        public TaskRepresentation(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

    }

}