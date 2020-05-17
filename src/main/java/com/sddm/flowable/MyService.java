package com.sddm.flowable;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    public List<String> schemaIdList = new ArrayList<>();

    @Autowired
    public MyService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    public String startMutianProcess() {
        return runtimeService.startProcessInstanceByKey("mutian-process")
                .getProcessInstanceId();
    }


    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

}
