package org.techhub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AsyncAuditServiceImpl implements AsyncAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAuditServiceImpl.class);

    @Override
    @Async("taskExecutor")
    public void logProductActivity(String action, Long productId, String username, String details) {
        logger.info("[AUDIT-ASYNC] Timestamp: {}, Action: {}, ProductId: {}, User: {}, Details: {}, Thread: {}",
                LocalDateTime.now(), action, productId, username, details, Thread.currentThread().getName());
    }
}
