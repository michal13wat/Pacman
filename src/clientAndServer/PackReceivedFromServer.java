package clientAndServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

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
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public ArrayList<T> getObjectsList() {
        return objectsList;
    }

    public ArrayList<Integer> getDeletedList() {
        return deletedList;
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
