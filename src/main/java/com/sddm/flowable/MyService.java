package com.sddm.flowable;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MyService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    @Autowired
   ProcessEngine processEngine;

    public List<String> schemaIdList = new ArrayList<>();

    @Autowired
    public MyService(RuntimeService runtimeService, TaskService taskService
                ,HistoryService historyService,RepositoryService repositoryService
                ) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService=repositoryService;
    }

    public String startMutianProcess() {
        return runtimeService.startProcessInstanceByKey("mutian-process")
                .getProcessInstanceId();
    }

    void updateDeployedProcess(String processName, String process){
        List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
        // 遍历集合，查看内容
        for (ProcessDefinition pd : pds) {
            if(pd.getName().equals(processName)){
                System.out.println("delete old processDefinition "+processName);
                repositoryService.deleteDeployment(pd.getDeploymentId());
            }
        }
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString(processName+".bpmn",process)
                .deploy();//完成部署
        System.out.println("Number of process definitions : "
                + repositoryService.createProcessDefinitionQuery().count());
    }


    public InputStream getDiagram(String processInstanceId) {
        //获得流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = StringUtils.EMPTY;
        if (processInstance == null) {
            //查询已经结束的流程实例
            HistoricProcessInstance processInstanceHistory =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
            if (processInstanceHistory == null)
                return null;
            else
                processDefinitionId = processInstanceHistory.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        //使用宋体
        String fontName = "宋体";
        //获取BPMN模型对象
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        //获取流程实例当前的节点，需要高亮显示
        List<String> currentActs = Collections.EMPTY_LIST;
        if (processInstance != null)
            currentActs = runtimeService.getActiveActivityIds(processInstance.getId());

        return processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(model,"png",currentActs,new ArrayList<String>()
                ,fontName,fontName,fontName,null,1.0,true);
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    List<String> getProcessDefinitions() throws IOException {
        System.out.println("get deployed Processes.");
        List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
        List<String> processDefinitions = new ArrayList<>();
        for(ProcessDefinition processDefinition:pds){
            processDefinitions.add(processDefinition.getName());
        }
        return processDefinitions;
//        System.out.println("Number of process definitions : "
//                + repositoryService.createProcessDefinitionQuery().count());
//        ArrayList<String> files = new ArrayList<String>();
//        File file = ResourceUtils.getFile("classpath:process");
//        File[] tempList = file.listFiles();
//        for (int i = 0; i < tempList.length; i++) {
//            if (tempList[i].isFile()) {
//                String s = tempList[i].getName();
//                int k = s.indexOf('.');
////                System.out.println(tempList[i].getName());
//                files.add(tempList[i].getName().substring(0,k));
//            }
//            if (tempList[i].isDirectory()) {
//                return null;
//            }
//        }
//        return files;
    }


    String getBpmnXML(String bpmnName) throws IOException {
        String bpmn = "process/" +bpmnName+".bpmn20.xml";
        Resource resource = new ClassPathResource(bpmn);
        File file = resource.getFile();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String bpmn1="";
        String s=new String();
        while((s=br.readLine())!=null){
            bpmn1+=s;
        }
        br.close();
        return bpmn1;
    }

    public boolean updateBpmnXML(String xml,String processName) {
        // 标记文件生成是否成功
        boolean flag = true;

        // 拼接文件完整路径
        String fullPath = "/Users/congtang/Desktop/sddm-backend/flowable/src/main/resources/process/" + processName+ ".bpmn.xml";

        try {
            // 保证创建一个新文件
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();

            // 将格式化后的字符串写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(xml);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
}
