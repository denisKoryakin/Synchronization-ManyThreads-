package com.company;

import java.util.*;
import java.util.concurrent.*;

public class Main {

    //  ключи - процент повторения R от общего маршрута, значение - количество повторений данного процента за все время
    //  Самое частое количество повторений 61 (встретилось 9 раз)
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    public static final int wayLength = 100;
    public static final int threads = 1000;

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Callable<Integer> myCallable = () -> {
            String way = generateRoute("RLRFR", wayLength);
            char[] arr = way.toCharArray();
            int result = 0;
            for (char c : arr) {
                if (c == 'R') {
                    result++;
                }
            }
            return (result * 100) / arr.length;
        };

//      Создали список из 1000 задач
        List<Callable<Integer>> callableList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            callableList.add(myCallable);
        }
//      Создали лист из 1000 будущих решений
        List<Future<Integer>> futureList;

//      создали пул из 1000 потоков
        final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

//      заполняем лист futureList решениями всех задач из списка callableList
        futureList = threadPool.invokeAll(callableList);

        if (threadPool.isTerminated()) threadPool.shutdown();

//      заполняем мапу
        for (Future<Integer> integerFuture : futureList) {
            if (sizeToFreq.containsKey(integerFuture.get())) {
                sizeToFreq.replace(integerFuture.get(), sizeToFreq.get(integerFuture.get()) + 1);
            } else {
                sizeToFreq.put(integerFuture.get(), 1);
            }
        }
        printResult(sizeToFreq);
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static void printResult(Map<Integer, Integer> map) {
        int maxValue = Collections.max(map.values());
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            if (entry.getValue() == maxValue) {
                System.out.println("Самое частое количество повторений " + entry.getKey() + " встречается " + maxValue + " раз");
            }
        }
        System.out.println("Другие размеры: ");
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            System.out.println("(" + entry + " раз)");
        }
    }
}
