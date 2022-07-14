package com.wallassouza.bibliotecaapi.api.resource.service;

import java.util.List;

public interface EmailService {
    void sendMail(String message, List<String> mailsList);
}
