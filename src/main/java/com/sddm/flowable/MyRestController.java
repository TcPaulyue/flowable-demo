package com.sddm.flowable;

import com.alibaba.fastjson.JSONObject;
import com.sddm.flowable.domain.Schema;
import com.sddm.flowable.service.MyTaskListener;
import org.apache.commons.io.IOUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyRestController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final MyFormService myFormService;
    private final MyService myService;

    @Autowired
    public MyRestController(RuntimeService runtimeService
            , TaskService taskService
            , MyFormService myFormService
            , MyService myService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.myFormService = myFormService;
        this.myService = myService;
    }

    @GetMapping("/processDefinitions")
    public List<String> getProcessDefinitions() throws IOException{
        return this.myService.getProcessDefinitions();
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

    @PostMapping(value="/submitForm/{processInstanceId}")
    public JSONObject submitForm(@PathVariable String processInstanceId,
                                        @RequestBody String query){
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8000"+"/api/documents/query";
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,query,JSONObject.class);
        myFormService.submitTaskFormData(task.getExecutionId(),response.getBody());
        return response.getBody();
    }

    @GetMapping(value="getForm/{taskId}")
    public JSONObject getForm(@PathVariable String taskId){
        return myFormService.getTaskFormData(taskId);
    }

    @PostMapping(value = "/completeTask/{processInstanceId}")
    public String completeTask(@PathVariable String processInstanceId){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return "error";
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        taskService.complete(task.getId());
        return task.getExecutionId();
    }

    @PostMapping(value = "/completeTaskWithVariables/{processInstanceId}")
    public String completeTaskWithVariables(@PathVariable String processInstanceId,
                                   @RequestParam String variables){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return "error.";
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Map<String,Object> map = new HashMap<>();
        map.put("property",variables);
        taskService.complete(task.getId(),map);
        return task.getExecutionId();
    }

    @GetMapping(value="/{id}/image" , produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImage(@PathVariable String id)  throws IOException {
        try{
            InputStream in = myService.getDiagram(id);
            if(in == null)
                return null;
            return IOUtils.toByteArray(in);
        }catch (IOException ex){
            System.out.println("查看流程图失败");
            return null;
        }
    }
    @GetMapping(value="/image")
    public String getXML(@RequestParam(value = "processDefinition", defaultValue = "null") String processDefinition)throws IOException{
        System.out.println("查看"+processDefinition+"的流程图");
        return myService.getBpmnXML(processDefinition);
    }
}