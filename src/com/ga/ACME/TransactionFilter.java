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
            case yesterday -> dateTime.toLocalDate().minusDays(1).atStartOfDay();
            case lastWeek -> dateTime.minusWeeks(1);
            case last7Days -> dateTime.minusDays(7);
            case lastMonth -> dateTime.minusMonths(1);
            case last30Days -> dateTime.minusDays(30);
        };

        LocalDateTime end = type == FilterType.yesterday? dateTime.toLocalDate().atStartOfDay(): dateTime;
        if(type==FilterType.yesterday){
            return transactions.stream().filter(t-> !t.getTimestamp().isBefore(start) && t.getTimestamp().isBefore(end)).toList();
        }
        return transactions.stream().filter(t -> !t.getTimestamp().isBefore(start) && !t.getTimestamp().isAfter(end)).toList();
    }

    public static List<Transaction> custom(List<Transaction> transactions, LocalDateTime start, LocalDateTime end){
        return transactions.stream().filter(t -> !t.getTimestamp().isBefore(start) && !t.getTimestamp().isAfter(end)).toList();
    }

}
