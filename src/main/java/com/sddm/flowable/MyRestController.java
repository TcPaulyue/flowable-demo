package com.sddm.flowable;

import com.alibaba.fastjson.JSONObject;
import com.sddm.flowable.domain.Schema;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.util.io.InputStreamSource;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
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

    @PostMapping(value="/processDefinitions/{processDefinitionName}")
    public String updateProcessDefinition(@PathVariable String processDefinitionName
           ,@RequestBody String xml ) {
        myService.updateDeployedProcess(processDefinitionName,xml);
        return "success";
    }

    @PostMapping(value="/processInstances/{processDefinitionName}")
    public String startProcessInstance(@PathVariable String processDefinitionName) {
        return runtimeService.startProcessInstanceByKey(processDefinitionName)
                .getProcessInstanceId();
    }

    @GetMapping(value = "/taskSchemaDefinition")
    public Schema getTaskSchemaDefinition(
            @RequestParam(value="processInstanceId")  String processInstanceId){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return null;
        }
        String fomilyId = myFormService.getTaskFormId(processInstanceId);
        return myFormService.getFormDefinition(fomilyId);
    }

    @GetMapping(value = "/ProcessInstanceSchemaDefinition")
    public Schema getProcessInstanceSchemaDefinition(
            @RequestParam(value="processInstanceId")  String processInstanceId){
        ProcessInstance rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        if(rpi == null){
            return null;
        }
        String fomilyId = myFormService.getStartFormId(processInstanceId);
        return myFormService.getFormDefinition(fomilyId);
    }

    @PostMapping(value="/submitTaskForm")
    public JSONObject submitTaskForm(
            @RequestParam(value="processInstanceId") String processInstanceId,
            @RequestBody String query){
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        return myFormService.submitTaskFormData(task.getExecutionId(),query);
    }

    @GetMapping(value="/task")
    public JSONObject getCurrentTask(
            @RequestParam(value="processInstanceId") String processInstanceId) throws Exception {
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        String taskId = task.getTaskDefinitionKey();
        String taskName = task.getName();
        BpmnModel bpmnModel = readXMLFile();
        //    FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement("sid-41AC103F-C991-4438-9B2D-013243854FA9");
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(taskId);
        Map<String,List<ExtensionElement>> extensions = flowElement.getExtensionElements();
  //      List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get("test");
        JSONObject taskInfo = new JSONObject();
        taskInfo.put("taskId",taskId);
        taskInfo.put("taskName",taskName);
        taskInfo.put("taskExtensions",extensions);
        return taskInfo;
    }
    private BpmnModel readXMLFile() throws Exception {
        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
        return readXMLFile(xmlStream);
    }
    private BpmnModel readXMLFile(InputStream inputStream) throws Exception {
        return new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(inputStream), true, false, "UTF-8");
    }
    private String getResource() {
        //return "extensions.bpmn20.xml";
      //  return myService.bpmnDefinitions.get("AskForVacationProcess");
        return "process/AskForVacationProcess.bpmn20.xml";
    }

    @PostMapping(value="/submitProcessInstanceForm")
    public JSONObject submitProcessInstanceForm(
            @RequestParam(value="processInstanceId") String processInstanceId,
            @RequestBody String query){
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        return myFormService.submitStartFormData(task.getExecutionId(),query);
    }

    @GetMapping(value="getTaskFormData")
    public JSONObject getTaskFormData(@RequestParam(value="taskId") String taskId){
        return myFormService.getTaskFormData(taskId);
    }

    @GetMapping(value="getProcessInstanceFormData")
    public JSONObject getProcessInstanceFormDate(@RequestParam(value="processInstanceId") String processInsatnceId){
        return myFormService.getStartFormData(processInsatnceId);
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
        return myService.getUpdatedBpmnXml(processDefinition);
    }
}