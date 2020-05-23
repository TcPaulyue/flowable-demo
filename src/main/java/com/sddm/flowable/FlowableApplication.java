package com.sddm.flowable;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(proxyBeanMethods = false)
public class FlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class, args);
    }
    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
                                  final RuntimeService runtimeService,
                                  final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery().count());
                List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
                System.out.println(pds.size());
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
            }
        };
    }
}
