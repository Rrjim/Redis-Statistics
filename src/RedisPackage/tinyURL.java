package RedisPackage;

import java.io.*;
import java.util.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

public class tinyURL {

    private static final Jedis jedis = new Jedis("127.0.0.1", 6379);
    public static void logTime(String task) {
        long timestamp = System.currentTimeMillis() / 1000; 
        String logEntry = timestamp + ": " + task; 
        jedis.lpush("TimeLogs", logEntry); 
    }
    
    public static void displayTimeLogs(String task) {
        List<String> logs = jedis.lrange(task, 0, -1);
        if (logs != null && !logs.isEmpty()) {
            System.out.println("Time logs for " + task + ":");
            for (int i = 0; i < logs.size(); i++) {
                long timestamp = Long.parseLong(logs.get(i));
                Date date = new Date(timestamp * 1000); 
                System.out.println((i + 1) + ". " + date);
            }
        } else {
            System.out.println("No time logs found for " + task);
        }
    }
    



    public static void main(String[] args) throws Exception {

        String replyFromUser;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter your username:");
        String username = inFromUser.readLine();

        while (true) {
            System.out.println("(I)nsert an artist | (Q)uery an artist | (S)tatistics | e(X)it");
            replyFromUser = inFromUser.readLine();

            logTime("Insertion");
            logTime("Query");
            logTime("Statistics");
            logTime("Exit");
            logTime("Invalid Action");



            if (replyFromUser.equalsIgnoreCase("I")) {
                System.out.println("Enter artist name:");
                String artistName = inFromUser.readLine();
                if (jedis.exists(artistName)) {
                    String name = jedis.hget(artistName, "name");
                    System.out.println(artistName + " is already recorded by: " + name);
                    displayTimeLogs("Insertion");

 
                } else {
                    jedis.hset(artistName, "name", username);
                    System.out.println(artistName + " entered successfully by " + username);
                    displayTimeLogs("Insertion");

                }

            } else if (replyFromUser.equals("Q")) {
                System.out.println("Enter artist name to query:");
                String queryArtist = inFromUser.readLine();
                if (jedis.exists(queryArtist)) {
                    String name = jedis.hget(queryArtist, "name");
                    long counter = jedis.incr(queryArtist + "_counter");
                    System.out.println("Artist " + queryArtist + " is recorded by: " + name + ". Request count: " + counter);
                    displayTimeLogs("Query");

                } else {
                    System.out.println("Artist " + queryArtist + " not found in the database.");
                    displayTimeLogs("Query");
                }
            } else if (replyFromUser.equals("S")) {
                Map<String, Long> userEntryCounts = new HashMap<>();
                Set<String> artistNames = jedis.keys("*");
                Set<String> hashArtists = new HashSet<>();
                for (String artist : artistNames) {
                    if (jedis.type(artist).equals("hash")) {
                        hashArtists.add(artist);
                    }
                }
                for (String artist : hashArtists) {
                    try {
                        if (jedis.type(artist).equals("hash")) {
                            String user = jedis.hget(artist, "name");
                            if (user != null) {
                                userEntryCounts.put(user, userEntryCounts.getOrDefault(user, 0L) + 1);
                            } else {
                                System.err.println("Error: User is null for artist '" + artist + "'.");
                            }
                        } else {
                            System.err.println("Error: Key '" + artist + "' does not hold a hash data type.");
                        }
                    } catch (JedisDataException e) {
                        System.err.println("Error retrieving data from Redis: " + e.getMessage());
                    }
                }

                if (hashArtists.size() != 0) {
                    System.out.println("Number of entries for each user:");
                    for (String user : userEntryCounts.keySet()) {
                        System.out.println(user + ": " + userEntryCounts.get(user));
                    }
                }

                long totalRequests = 0;
                for (String artist : hashArtists) {
                    String counterKey = artist + "_counter";
                    if (jedis.exists(counterKey)) {
                        totalRequests += Long.parseLong(jedis.get(counterKey));
                    }
                }
                if (hashArtists.size() != 0) {
                    double averageRequests = (double) totalRequests / hashArtists.size();
                    System.out.println("Average number of requests per artist: " + averageRequests);
                    displayTimeLogs("Statistics");

                } else {
                    System.out.println("In order to display statistics, please enter some artist data into the database: ");
                    displayTimeLogs("Statistics");

                }

            } else if (replyFromUser.equals("X")) {
                System.out.println("Goodbye");
                System.exit(1);
                displayTimeLogs("Exit");
            } else {
                System.out.println(replyFromUser + "is not a valid choice, retry");
                displayTimeLogs("Invalid Action");
            }
//            if (replyFromUser.equalsIgnoreCase("I")) {
//            displayTimeLogs("Insertion");
//            } else if (replyFromUser.equalsIgnoreCase("Q")) {
//                displayTimeLogs("Query");
//            } else if (replyFromUser.equalsIgnoreCase("S")) {
//                displayTimeLogs("Statistics");
//            } else {
//                displayTimeLogs("Exit");
//            }

            //displayTimeLogs("Action");


        }
    }
}
