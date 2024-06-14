package RedisPackage;

import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.Scanner;

public class TimeLogs {

    private static final Jedis jedis = new Jedis("127.0.0.1", 6379);


    public static void logTime(String task) {
        long timestamp = System.currentTimeMillis() / 1000; // Convert to seconds
        jedis.lpush(task, String.valueOf(timestamp));
    }

    public static void displayLogs(String task) {
        List<String> logs = jedis.lrange(task, 0, -1);
        if (logs != null && !logs.isEmpty()) {
            System.out.println("Logs for task '" + task + "':");
            for (int i = 0; i < logs.size(); i++) {
                System.out.println((i + 1) + ". " + new java.util.Date(Long.parseLong(logs.get(i)) * 1000));
            }
        } else {
            System.out.println("No logs found for task '" + task + "'.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Log time for a task");
            System.out.println("2. Display time logs for a task");
            System.out.println("3. Exit");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter task name: ");
                    String task = scanner.nextLine();
                    logTime(task);
                    System.out.println("Time logged successfully.");
                    break;
                case "2":
                    System.out.print("Enter task name: ");
                    task = scanner.nextLine();
                    displayLogs(task);
                    break;
                case "3":
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
