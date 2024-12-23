package ds.assign.p2p;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class PeerConnection{

    String host;
    int port;

    PeerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //Lista dos vizinhos diretos
    //Key-> porta
    //valor-> hostname
    private final Map<Integer, String> vizinhos = new HashMap<>();

    //key-> hostname-hostport
    //valor-> timestamp em UTC segundos(que o próprio peer registou em ultimo)

    private final Map<String, Long> vizinhoTimestamp = new HashMap<>();

    public Map<Integer, String> getVizinhos() {
        return vizinhos;
    }

    public Map<String, Long> getVizinhoTimestampMap() {
        return vizinhoTimestamp;
    }

    //Adiciona o vizinho
    public void addVizinho(int port, String address) {
        vizinhos.put(port, address);
        vizinhoTimestamp.put(address + "-" + port, Instant.now().getEpochSecond());
    }

    public synchronized void mergeIncomingMap(String incomingMap) {
        // Parse and merge incoming map
        String[] entries = incomingMap.split(",");
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                long timestamp = Long.parseLong(keyValue[1]);
                vizinhoTimestamp.put(key, Math.max(vizinhoTimestamp.getOrDefault(key, 0L), timestamp));
            }
        }
    }


    @Override
    public synchronized String toString() {
        // Convert map to string format
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> entry : vizinhoTimestamp.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        return sb.toString();
    }

    public synchronized void cleanupOldEntries(long thresholdSeconds) {
        long currentTime = Instant.now().getEpochSecond();
        vizinhoTimestamp.entrySet().removeIf(entry -> currentTime - entry.getValue() > thresholdSeconds);
    }

    public String chooseRandomVizinho(){
        List<Integer> ports = new ArrayList<>(vizinhos.keySet());

        for (int i = 0; i < ports.size(); i++) {
            if(ports.get(i) == port){
                ports.remove(i);
            }   
        }

        Random rand = new Random();

        int randomInt = rand.nextInt(ports.size());
        int randomPort = ports.get(randomInt);


        String address = vizinhos.get(randomPort);
        return address + ":" + randomPort;
    }
}
