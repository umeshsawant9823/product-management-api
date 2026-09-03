package org.techhub.service;

public interface AsyncAuditService {
    void logProductActivity(String action, Long productId, String username, String details);
}
