package com.sddm.flowable;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.constants.ModelDataJsonConstants;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@SpringBootTest
public class FlowableCustomExtensionTest {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void getBpmnModel() {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey("formRequest")
                .latestVersion().singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pd.getId());
        Process process = bpmnModel.getProcesses().get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask u = (UserTask) flowElement;
                List outgoingFlows = u.getOutgoingFlows();
                System.err.println("outgoingFlows:" + outgoingFlows);
            }
            System.err.println(flowElement.getId() + "--->>>>"
                    + flowElement.getName());

        }
    }
    @Test
    void createBpmnModel(){
        BpmnModel bpmnModel = new BpmnModel();
        SequenceFlow flow1 = new SequenceFlow();
        flow1.setId("flow1");
        flow1.setName("开始节点->分享牛1");
        flow1.setSourceRef("start1");
        flow1.setTargetRef("userTask1");
        // 分享牛1->分享牛2
        SequenceFlow flow2 = new SequenceFlow();
        flow2.setId("flow2");
        flow2.setName("分享牛1->分享牛2");
        flow2.setSourceRef("userTask1");
        flow2.setTargetRef("userTask2");

        // 分享牛1->分享牛2
        SequenceFlow flow3 = new SequenceFlow();
        flow3.setId("flow3");
        flow3.setName("分享牛2->结束节点");
        flow3.setSourceRef("userTask2");
        flow3.setTargetRef("endEvent");

        StartEvent start = new StartEvent();
        start.setName("开始节点");
        start.setId("start1");
        start.setOutgoingFlows(Arrays.asList(flow1));
        // 分享牛1
        UserTask userTask1 = new UserTask();
        userTask1.setName("分享牛1");
        userTask1.setId("userTask1");
        userTask1.setIncomingFlows(Arrays.asList(flow1));
        userTask1.setOutgoingFlows(Arrays.asList(flow2));
        // 分享牛2
        org.flowable.bpmn.model.UserTask userTask2 = new UserTask();
        userTask2.setName("分享牛2");
        userTask2.setId("userTask2");
        userTask2.setIncomingFlows(Arrays.asList(flow2));
        userTask2.setOutgoingFlows(Arrays.asList(flow3));

        // 结束节点
        org.flowable.bpmn.model.EndEvent endEvent = new org.flowable.bpmn.model.EndEvent();
        endEvent.setName("结束节点");
        endEvent.setId("endEvent");
        endEvent.setIncomingFlows(Arrays.asList(flow3));
        Process process = new Process();
        // 将所有的FlowElement添加到process中
        process.addFlowElement(start);
        process.addFlowElement(flow1);
        process.addFlowElement(userTask1);
        process.addFlowElement(flow2);
        process.addFlowElement(userTask2);
        process.addFlowElement(flow3);
        process.addFlowElement(endEvent);
        bpmnModel.addProcess(process);
        ProcessValidator processValidator=new ProcessValidatorFactory().createDefaultProcessValidator();
        List<ValidationError> validationErrorList=processValidator.validate(bpmnModel);
        if (validationErrorList.size()>0){
            throw new RuntimeException("流程有误，请检查后重试");
        }
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey("formRequest")
                .latestVersion().singleResult();
        BpmnModel bpmnModel1 = repositoryService.getBpmnModel(pd.getId());
        System.err.println(bpmnModel);
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("test.bpmn20.xml",bpmnModel1)
                .deploy();
        System.err.println(deployment);


    }

    @Test
    void deployFlowModel() throws IOException, XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader isr = null;
        XMLStreamReader xtr = null;
        InputStream is = new FileInputStream("./src/main/resources/process/AskForVacationProcess.bpmn20.xml");
        isr = new InputStreamReader(is, "utf-8");
        xtr = xif.createXMLStreamReader(isr);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        ObjectNode modelNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        System.err.println(modelNode);
        // 通过act_de_model中存放的Modeler内容来部署
        Model modelData = repositoryService.newModel();
        modelData.setKey("test");
        modelData.setName("AskForVacationProcess");
        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "AskForVacationProcess");
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "just a test code");
        modelData.setMetaInfo(modelObjectNode.toString());
        //updateProcessKey(modelNode);
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        Model modelData1 = repositoryService.getModel(modelData.getId());
        ObjectNode modelNode1 = null;
        try {
            // 获取模型
            byte[] bytes = repositoryService.getModelEditorSource(modelData1.getId());
            if (null == bytes) {
                System.err.println("模型数据为空，请先设计流程并成功保存，再进行发布。");
            }
            modelNode1 = (ObjectNode) new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode1);
            if (model.getProcesses().size() == 0) {
                System.err.println("数据模型不符要求，请至少设计一条主线流程。");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            // 发布流程
            System.err.println(bpmnBytes);
            String processName = modelData1.getName() + ".bpmn";
            String s = new String(bpmnBytes, StandardCharsets.UTF_8);
            repositoryService.createDeployment().name(modelData1.getName()).addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8))
                    .deploy();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }
}
