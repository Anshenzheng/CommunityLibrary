package com.community.library.service;

import com.community.library.entity.Statistics;
import com.community.library.repository.StatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    
    @Autowired
    private StatisticsRepository statisticsRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalReaders", userService.getReaderCount());
        stats.put("totalBooks", bookService.getTotalBooksCount());
        stats.put("availableBooks", bookService.getAvailableBooksCount());
        stats.put("borrowedBooks", borrowService.getBorrowedCount());
        stats.put("pendingRequests", borrowService.getPendingCount());
        stats.put("overdueCount", borrowService.getOverdueCount());
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        
        stats.put("weeklyBorrows", borrowService.getBorrowsBetween(weekStart, today));
        stats.put("weeklyReturns", borrowService.getReturnsBetween(weekStart, today));
        
        return stats;
    }
    
    public List<Statistics> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        return statisticsRepository.findByStatDateBetweenOrderByStatDate(startDate, endDate);
    }
    
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void generateDailyStatistics() {
        LocalDate today = LocalDate.now();
        
        if (statisticsRepository.findByStatDate(today).isPresent()) {
            return;
        }
        
        Statistics stats = new Statistics();
        stats.setStatDate(today);
        stats.setTotalUsers((int) userService.getTotalUsers());
        stats.setTotalBooks((int) bookService.getTotalBooksCount());
        stats.setTotalBorrowed((int) borrowService.getBorrowsBetween(today.minusDays(1), today));
        stats.setTotalReturned((int) borrowService.getReturnsBetween(today.minusDays(1), today));
        stats.setTotalOverdue((int) borrowService.getOverdueCount());
        
        statisticsRepository.save(stats);
    }
}
