package com.sddm.flowable;

import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.util.io.InputStreamSource;

import java.io.InputStream;

public abstract class AbstractConverterTest implements BpmnXMLConstants {

    protected BpmnModel readXMLFile() throws Exception {
        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
        return readXMLFile(xmlStream);
    }

    protected BpmnModel readXMLFile(InputStream inputStream) throws Exception {
        return new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(inputStream), true, false, "UTF-8");
    }

    protected abstract String getResource();

}