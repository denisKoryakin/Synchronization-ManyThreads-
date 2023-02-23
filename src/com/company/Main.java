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

//        создаем поток для мониторинга состояния мапы
        Thread monitor = new Thread(() -> {
            int iterationCount = 0;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                    System.out.println("На этапе " + iterationCount);
                    printEndResult(sizeToFreq);
                    iterationCount++;
                }
            }
        });
        monitor.start();

//        создаем коллекцию из 1000 потоков для поиска 1000 маршрутов
        List<Thread> tasksTreads = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasksTreads.add(new Thread(() -> {
                String way = generateRoute("RLRFR", wayLength);
                char[] arr = way.toCharArray();
                int result = 0;
                for (char c : arr) {
                    if (c == 'R') {
                        result++;
                    }
                }
                result = (result * 100) / arr.length;
//                записываем значение в мапу
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(result)) {
                        sizeToFreq.put(result, sizeToFreq.get(result) + 1);
                    } else {
                        sizeToFreq.put(result, 1);
                    }
                    sizeToFreq.notify();
                }
            }));
//            Заставляем заснуть поток для отработки мониторинга мапы другим потоком
            tasksTreads.get(tasksTreads.size() - 1).start();
            Thread.sleep(1);
        }

//        дожидаемся завершения работы всех потоков по решению задач
        for (Thread thread : tasksTreads) {
            thread.join();
        }
        monitor.interrupt();
        printOtherResults(sizeToFreq);
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static void printEndResult(Map<Integer, Integer> map) {
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            if (entry.getValue().equals(Collections.max(map.values()))) {
                System.out.println("Самое частое количество повторений " + entry.getKey() + " встречается " + Collections.max(map.values()) + " раз");
                break;
            }
        }
    }

    public static void printOtherResults(Map<Integer, Integer> map) throws InterruptedException {
//        ввремя необходимое для отрабатывания параллельных потоков
        Thread.sleep(5);
        System.out.println("Другие размеры: ");
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            if (!entry.getValue().equals(Collections.max(map.values()))) {
                System.out.println("(" + entry + " раз)");
            }
        }
    }
}
