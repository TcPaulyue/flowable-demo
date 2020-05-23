package com.sddm.flowable;

import org.apache.commons.io.IOUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication(proxyBeanMethods = false)
public class FlowableApplication {


    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class, args);
    }
    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService) {

        return strings -> {
            InputStream is = new FileInputStream("./src/main/resources/process/form.bpmn20.xml");
            String text = IOUtils.toString(is, "UTF-8");
            Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                    .createDeployment()//创建部署对象
                    .addString("formRequest.bpmn",text)
                    .deploy();//完成部署
            is = new FileInputStream("./src/main/resources/process/articleReview.bpmn20.xml");
            text = IOUtils.toString(is, "UTF-8");
            deployment = repositoryService//获取流程定义和部署对象相关的Service
                    .createDeployment()//创建部署对象
                    .addString("articleReview.bpmn",text)
                    .deploy();//完成部署
            System.out.println("Number of process definitions : "
                    + repositoryService.createProcessDefinitionQuery().count());
            List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
            // 遍历集合，查看内容
            for (ProcessDefinition pd : pds) {
                System.out.println("id:" + pd.getId() + ",");
                System.out.println("name:" + pd.getName() + ",");
                System.out.println("deploymentId:" + pd.getDeploymentId() + ",");
                System.out.println("version:" + pd.getVersion());
            //    repositoryService.deleteDeployment(pd.getDeploymentId(),true);
            }
            System.out.println("Number of process definitions : "
                    + repositoryService.createProcessDefinitionQuery().count());
        };
    }
}
