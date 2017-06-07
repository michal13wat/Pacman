package clientAndServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import pacman.IntWrapper;
import pacman.StringWrapper;

/**
 * Created by User on 2017-04-17.
 */
/*
 *  Nazwa klasy jest logiczna jeżeli patrzy się od strony klieta.
 *  Żeby nie toworzyć nowych klas osobno dla servera, użyję tych.
 *  */
public class PackReceivedFromServer<T> implements Serializable {
    private String additionalInfo;
    private ArrayList<T> objectsList = new ArrayList<T>();          // główny element klasy!
    private ArrayList<Integer> deletedList = new ArrayList<>();
    private ArrayList<String> connectedClients = new ArrayList<>();
    private int notConnectedClients = 0;
    private Random randomizer = null;

    // Wartości z wrapperów.
    public int gameScore;
    public int gameLives;
    public int maxPlayers;
    
    // Czy aby na pewno???
    private ArrayList<PackToSendToServer> clientFeedback = new ArrayList<>();
    
    public PackReceivedFromServer(){
    }
    public PackReceivedFromServer(ArrayList<T> objectsList, String additionalInfo,
                                  ArrayList<String> connectedClients, int notConnectedClients){
        this.objectsList = objectsList;
        this.additionalInfo = additionalInfo;
        this.connectedClients = connectedClients;
        this.notConnectedClients = notConnectedClients;
    }
    
    public void clear() {
        objectsList.clear();
        deletedList.clear();
        connectedClients.clear();
        clientFeedback.clear();
        randomizer = null;
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
    
    public void addObject(T obj){
        objectsList.add(obj);
    }
    public void addList(ArrayList<T> list){
        objectsList.addAll(list);
    }

    public void addDeletedList(ArrayList<Integer> list) {
        deletedList.addAll(list);
    }
    
    public void addFeedbacks(ArrayList<PackToSendToServer> list) {
        clientFeedback.addAll(list);
    }
    
    public void addFeedback(PackToSendToServer pack) {
        clientFeedback.add(pack);
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public ArrayList<T> getObjectsList() {
        return objectsList;
    }

    public ArrayList<Integer> getDeletedList() {
        return deletedList;
    }
    
    public ArrayList<PackToSendToServer> getClientFeedback() {
        return clientFeedback;
    }
    
    public Random getRandomizer() {
        return randomizer;
    }
    
    public void addConnectedClient(String name){
        this.connectedClients.add(name);
    }

    public void setConnectedClients(ArrayList<String> connectedClients) {
        this.connectedClients = connectedClients;
    }

    public ArrayList<String> getConnectedClients() {
        return connectedClients;
    }

    public void setNotConnectedClients(int notConnectedClients) {
        this.notConnectedClients = notConnectedClients;
    }

    public int getNotConnectedClients() {
        return notConnectedClients;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
