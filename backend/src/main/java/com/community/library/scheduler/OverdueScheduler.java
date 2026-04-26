package com.community.library.scheduler;

import com.community.library.service.BorrowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(OverdueScheduler.class);
    
    @Autowired
    private BorrowService borrowService;
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAndMarkOverdue() {
        logger.info("开始执行逾期检查定时任务...");
        try {
            borrowService.markOverdueRecords();
            logger.info("逾期检查定时任务执行完成");
        } catch (Exception e) {
            logger.error("逾期检查定时任务执行失败: {}", e.getMessage(), e);
        }
    }
}
