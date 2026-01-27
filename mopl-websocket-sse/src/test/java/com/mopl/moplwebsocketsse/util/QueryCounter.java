package com.mopl.moplwebsocketsse.util;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

// @Component
public class QueryCounter {

    // private final EntityManagerFactory emf;
    //
    // public QueryCounter(EntityManagerFactory emf) {
    //     this.emf = emf;
    // }
    //
    // public long count(Runnable action) {
    //     Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
    //     stats.setStatisticsEnabled(true);
    //     stats.clear();
    //
    //     action.run();
    //
    //     long prepareCount = stats.getPrepareStatementCount();
    //
    //     System.out.println("\n========== 쿼리 통계 ==========");
    //     System.out.println("PrepareStatement 수: " + prepareCount);
    //     System.out.println("Query 실행 수: " + stats.getQueryExecutionCount());
    //     System.out.println("Entity Load 수: " + stats.getEntityLoadCount());
    //     System.out.println("==============================\n");
    //
    //     return prepareCount;
    // }
}
