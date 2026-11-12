package tn.esprit.autoloc.autolacapi.service.location.legacy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsGateway {

    private final String url;

    public SmsGateway(String url) {
        this.url = url;
    }

    public void send(String telephone, String message) {
        log.info("[SMS {}] vers {} : {}", url, telephone, message);
    }
}