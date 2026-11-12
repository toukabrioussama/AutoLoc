package tn.esprit.autoloc.autolacapi.service.location.legacy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmtpClient {

    private final String host;
    private final int port;

    public SmtpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String destinataire, String message) {
        log.info("[SMTP {}:{}] email à {} : {}", host, port, destinataire, message);
    }
}