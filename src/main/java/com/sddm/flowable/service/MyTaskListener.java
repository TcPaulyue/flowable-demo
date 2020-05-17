package com.sddm.flowable.service;

import com.sddm.flowable.MyService;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component
public class MyTaskListener implements TaskListener {

    private Expression schemaId;

    public Expression getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(Expression schemaId) {
        this.schemaId = schemaId;
    }

    @Override
    public void notify(DelegateTask execution) {
        System.out.println("variables="+ schemaId.getValue(execution));
    }
}