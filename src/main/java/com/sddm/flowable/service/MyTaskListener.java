package com.sddm.flowable.service;

import com.sddm.flowable.MyService;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component
public class MyTaskListener implements TaskListener {

    private Expression taskName;

    public Expression getTaskName() {
        return taskName;
    }

    public void setTaskName(Expression taskName) {
        this.taskName = taskName;
    }

    @Override
    public void notify(DelegateTask execution) {
        System.out.println("variables="+ taskName.getValue(execution));
    }
}