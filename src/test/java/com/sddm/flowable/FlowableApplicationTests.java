package com.sddm.flowable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.sddm.flowable.service.MyTaskListener;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.*;
import org.flowable.engine.FormService;
import org.flowable.engine.form.StartFormData;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.*;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class FlowableApplicationTests {

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
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private MyService myService;

    private static String SPACE = "   ";

    public String formatJson(String json) {
        StringBuffer result = new StringBuffer();

        int length = json.length();
        int number = 0;
        char key = 0;

        // 遍历输入字符串。
        for (int i = 0; i < length; i++) {
            // 1、获取当前字符。
            key = json.charAt(i);

            // 2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                // （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }

                // （2）打印：当前字符。
                result.append(key);

                // （3）前方括号、前花括号，的后面必须换行。打印：换行。
                result.append('\n');

                // （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
                number++;
                result.append(indent(number));

                // （5）进行下一次循环。
                continue;
            }

            // 3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                // （1）后方括号、后花括号，的前面必须换行。打印：换行。
                result.append('\n');

                // （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                number--;
                result.append(indent(number));

                // （3）打印：当前字符。
                result.append(key);

                // （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }

                // （5）继续下一次循环。
                continue;
            }

            // 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            // 5、打印：当前字符。
            result.append(key);
        }

        return result.toString();
    }
    private String indent(int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }
    @Test
    public void testCreateNewFile() throws IOException{
        String name = "AskForLeaveProcess";
        InputStream is = new FileInputStream("./src/main/resources/process/AskForVacationProcess.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        myService.updateBpmnXML(text,name);
    }
    public boolean createJsonFile(String jsonString) {
        // 标记文件生成是否成功
        boolean flag = true;

        // 拼接文件完整路径
        String fullPath = "./src/main/resources/forms/" + "test1.form";
        // 生成json格式文件
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

            if(jsonString.contains("'")){
                //将单引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
                jsonString = jsonString.replaceAll("'", "\\'");
            }
            if(jsonString.contains("\"")){
                //将双引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
                jsonString = jsonString.replaceAll("\"", "\\\"");
            }

            if(jsonString.contains("\r\n")){
                //将回车换行转换一下，因为JSON串中字符串不能出现显式的回车换行
                jsonString = jsonString.replaceAll("\r\n", "\\u000d\\u000a");
            }
            if(jsonString.contains("\n")){
                //将换行转换一下，因为JSON串中字符串不能出现显式的换行
                jsonString = jsonString.replaceAll("\n", "\\u000a");
            }

            // 格式化json字符串
            jsonString = this.formatJson(jsonString);

            // 将格式化后的字符串写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }

        // 返回是否成功的标记
        return flag;
    }

    @Test
    void getFormFromSchema() throws IOException{
        InputStream is = new FileInputStream("./src/main/resources/test.json");
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        System.out.println(jsonTxt);
        JSONObject json = JSON.parseObject(jsonTxt);
        JSONObject properties = json.getJSONObject("properties");
        HashMap<String, LinkedTreeMap> hashMap = new Gson().fromJson(properties.toString(), HashMap.class);

        JSONObject testForm = new JSONObject();
        testForm.put("key",json.getString("description"));
        testForm.put("name", "请假流程");
        JSONArray jsonArray = new JSONArray();
        hashMap.forEach((key,value)->{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",key);
            jsonObject.put("type",value.get("type").toString());
            jsonObject.put("required",true);
            jsonObject.put("name",value.get("description").toString());
            jsonObject.put("placeholder","empty");
            jsonArray.add(jsonObject);
        });
        testForm.put("fields",jsonArray);
        this.createJsonFile(testForm.toString());
        System.out.println(testForm);
    }

    @Test
    void deployProcess() throws IOException{
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> pds = processDefinitionQuery.list();
        System.out.println(pds.size());
        // 遍历集合，查看内容
        for (ProcessDefinition pd : pds) {
            System.out.println("id:" + pd.getId() + ",");
            System.out.println("name:" + pd.getName() + ",");
            System.out.println("deploymentId:" + pd.getDeploymentId() + ",");
            System.out.println("version:" + pd.getVersion());
           // repositoryService.deleteDeployment(pd.getDeploymentId(),true);
            System.out.println("11111111");
        }
        InputStream is = new FileInputStream("./src/main/resources/AskForVacationProcess.bpmn20.xml");
        String text = IOUtils.toString(is, "UTF-8");
        Deployment deployment = processEngine.getRepositoryService()//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .addString("helloworld.bpmn",text)
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//1
        System.out.println("部署key："+deployment.getKey());//1
        System.out.println("部署时间："+deployment.getDeploymentTime());
        System.out.println("Number of process definitions : "
                + repositoryService.createProcessDefinitionQuery().count());
        
    }

    @Test
    void contextLoads() throws IOException {

//        InputStream is = new FileInputStream("./src/main/resources/forms/test.form");
//        String jsonTxt = IOUtils.toString(is, "UTF-8");
//        System.out.println(jsonTxt);
//        FormDeployment formDeployment = formRepositoryService.createDeployment()
//                .name("definition-one").addInputStream("forms/test.Form",is)
//                .deploy();
//        FormDeployment formDeployment = formRepositoryService.createDeployment()
//                .name("definition-one").addString("forms/test.Form",jsonTxt)
//                .deploy();
        FormDeployment formDeployment = formRepositoryService.createDeployment()
                .name("definition-one")
                .addClasspathResource("forms/test1.form")
                .deploy();
        FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery().deploymentId(formDeployment.getId()).singleResult();
        Map<String, Object> properties = new HashMap<>();
        properties.put("startTime", "2019-01-01");
        properties.put("endTime", "2019-02-01");
        properties.put("reason", "回家过年");
        String outCome = "sendToParent";
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey("formRequest")
                .latestVersion().singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pd.getId());
        System.err.println(bpmnModel);
        //       ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey("formRequest")
 //               .latestVersion().singleResult();
 //       StartFormData form = formService.getStartFormData(pd.getId());
//        FormInfo info = formRepositoryService.getFormModelByKey(form.getFormKey());
 //       ProcessInstance processInstance = runtimeService.startProcessInstanceWithForm(pd.getId(),outCome,properties,pd.getName());

        Map<String,Object> map = new HashMap<>();
        map.put("schemaId","5ec1e50924521d128a258adf");
       ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("formRequest",map);
       Map<String,Object> map1 = runtimeService.getVariables(processInstance.getId());
       Map<String,Object> newMap = new HashMap<>();
        newMap.put("document","abc");
        runtimeService.setVariables(processInstance.getId(),newMap);
        map1= runtimeService.getVariables(processInstance.getId());
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        String id = task.getExecutionId();
        List<String> schemaIdList = myService.schemaIdList;
        String descript = task.getDescription();

        runtimeService.setVariable(task.getExecutionId(), "queryExp", "tc");

        Map<String, Object> variablesMap = runtimeService.getVariables(task.getExecutionId());

        String queryExp = variablesMap.get("queryExp").toString();
        System.out.println("task内容: "+ task.toString());
        System.out.println("变量为：" + taskService.getVariable(task.getId(), "personName"));
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
        }
        taskService.completeTaskWithForm(task.getId(),formDefinition.getId(),outCome,properties);
        Map<String, Object> map2 = runtimeService.getVariables(task.getExecutionId());
        FormModel taskFM = taskService.getTaskFormModel(task.getId()).getFormModel();
        rpi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstance.getId())
                .singleResult();
        if(rpi==null){
            HistoricProcessInstance hpi = historyService//
                    .createHistoricProcessInstanceQuery()//
                    .processInstanceId(processInstance.getId())//使用流程实例ID查询
                    .singleResult();
            System.out.println(hpi.getId()+"    "+hpi.getStartTime()+"   "+hpi.getEndTime()+"   "+hpi.getDurationInMillis());
        }

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println("task内容: "+ task.toString());

    }

}
