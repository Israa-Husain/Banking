package com.ga.ACME;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class TransactionFilter {

    private TransactionFilter(){

    }

    public static List<Transaction> filter(List<Transaction> transactions, FilterType type, LocalDateTime dateTime){
        LocalDateTime start = switch (type){
            case today -> dateTime.toLocalDate().atStartOfDay();
            case yesterday -> dateTime.minusDays(1); //dateTime.toLocalDate().minusDays(1) //ERROR
            case lastWeek -> dateTime.minusWeeks(1);
            case last7Days -> dateTime.minusDays(7);
            case lastMonth -> dateTime.minusMonths(1);
            case last30Days -> dateTime.minusDays(30);
        };
        return transactions.stream().filter(t -> t.getTimestamp().isBefore(dateTime) && t.getTimestamp().isAfter(start)).collect(Collectors.toList());
    }

    public static List<Transaction> custom(List<Transaction> transactions, LocalDateTime start, LocalDateTime end){
        return transactions.stream().filter(t -> t.getTimestamp().isBefore(end) && t.getTimestamp().isAfter(start)).collect(Collectors.toList());
    }

}
