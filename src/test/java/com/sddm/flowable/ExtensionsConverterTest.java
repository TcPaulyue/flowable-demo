package com.sddm.flowable;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionsConverterTest extends AbstractConverterTest {

    @Autowired
    private RepositoryService repositoryService;
    @Override
    protected String getResource() {
        return "extensions.bpmn20.xml";
     //   return "process/AskForVacationProcess.bpmn20.xml";
    }

    @Test
    public void convertXMLToModel() throws Exception {

        // Check that reconverting doesn't duplicate extension elements
        BpmnModel bpmnModel = readXMLFile();
    //    FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement("sid-41AC103F-C991-4438-9B2D-013243854FA9");
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement("theTask");

        List<ExtensionElement> extensionElements1 = flowElement.getExtensionElements().get("test");
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get("outgoings");
        assertThat(extensionElements).hasSize(1);

        // Reconvert to xml and back to bpmn model
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        byte[] xmlBytes = bpmnXMLConverter.convertToXML(bpmnModel);
        bpmnModel = readXMLFile(new ByteArrayInputStream(xmlBytes));

        extensionElements = bpmnModel.getMainProcess().getFlowElement("theTask").getExtensionElements().get("outgoings");
     //   String customName = extensionElements.get(0).getName();
        assertThat(extensionElements).hasSize(1);

        // 发布流程
        System.err.println(xmlBytes);
        String processName = "extensions.bpmn20.xml";
        String s = new String(xmlBytes, StandardCharsets.UTF_8);
        repositoryService.createDeployment().addString(processName, new String(xmlBytes, StandardCharsets.UTF_8))
                .deploy();

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