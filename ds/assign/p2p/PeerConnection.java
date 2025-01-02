package ds.assign.p2p;

import java.util.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public class PeerConnection{
    String host;
    int port;
    //Lista dos vizinhos diretos
    //Key-> porta
    //valor-> hostname
    private Map<String, Long> vizinhos = new HashMap<>();

    PeerConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.vizinhos = new HashMap<>();
    }

    public synchronized Map<String, Long> getVizinhos() {
        return new HashMap<>(vizinhos);
    }

    /*public Map<String, Long> getVizinhoTimestampMap() {
        return vizinhoTimestamp;
    }*/

    //Adiciona o vizinho
    public synchronized void addVizinho(int port, String host) {
        vizinhos.put(host + ":" + port, System.currentTimeMillis());
    }

    public synchronized void mergeIncomingMap(String incomingMap) {
        // Parse and merge incoming map
        String[] entries = incomingMap.split(",");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 3) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                long timestamp = Long.parseLong(parts[2]);
                vizinhos.put(host + ":" + port, timestamp);
            }
        }
    }


    @Override
    public synchronized String toString() {
        // Convert map to string format
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> entry : vizinhos.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        return sb.toString();
    }

    public synchronized String getVizinhosAsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> entry : vizinhos.entrySet()) {
            sb.append(entry.getValue()).append(":").append(entry.getKey()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        return sb.toString();
    }

    public synchronized void cleanupOldEntries(long thresholdMillis) {
        long currentTime = System.currentTimeMillis();
        vizinhos.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > thresholdMillis);
    }

    public synchronized String chooseRandomVizinho(){
        if (vizinhos.isEmpty()) {
            return null;
        }
        List<String> keys = new ArrayList<>(vizinhos.keySet());
        keys.remove(host + ":" + port); // Remove self from the list
        if (keys.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        return keys.get(rand.nextInt(keys.size()));
    }
}
