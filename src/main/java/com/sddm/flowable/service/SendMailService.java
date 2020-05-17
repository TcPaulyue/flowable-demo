package com.sddm.flowable.service;

import lombok.extern.log4j.Log4j2;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Log4j2
public class SendMailService implements JavaDelegate {
    public void execute(DelegateExecution execution) {
        log.info("Sending mail to shengji.");
    }
}