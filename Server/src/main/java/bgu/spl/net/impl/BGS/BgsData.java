package bgu.spl.net.impl.BGS;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BgsData {

    //private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<UserData>> connectionId_and_UserData;//LOGGED IN
    private ConcurrentHashMap<Integer, UserData> connectionId_and_UserData;//LOGGED IN
    private ConcurrentHashMap<String, UserData> userName_and_UserData;//REGISTERED
    private ConcurrentLinkedQueue<String> Registered_Users;
    private ReentrantReadWriteLock lock;

    public BgsData() {
        connectionId_and_UserData = new ConcurrentHashMap<>();
        userName_and_UserData = new ConcurrentHashMap<>();
        Registered_Users = new ConcurrentLinkedQueue<>();
        lock = new ReentrantReadWriteLock();

    }

    public boolean isLoggedInById(int ConnectionId) {
        return connectionId_and_UserData.containsKey(ConnectionId);
    }

    public boolean isRegisterByUserName(String userName) {
        return userName_and_UserData.containsKey(userName);
    }

    public boolean Register(String username, UserData ud) { //assume not registered
        UserData u = userName_and_UserData.putIfAbsent(username, ud);
        if (u == null) {
            Registered_Users.add(ud.getUserName());
            return true;
        }
        return false;
    }

    /*public void Register(int ConnectionId, UserData ud) { //assume not registered

        if (connectionId_and_UserData.containsKey(ConnectionId)) {
            synchronized ((connectionId_and_UserData.get(ConnectionId))) {
                connectionId_and_UserData.get(ConnectionId).add(ud);
            }
        } else {
            ConcurrentLinkedQueue<UserData> q = new ConcurrentLinkedQueue<>();
            q.add(ud);
            connectionId_and_UserData.put(ConnectionId, q); //TODO check for synchronize
        }
        userName_and_UserData.put(ud.getUserName(), ud);
        Registered_Users.add(ud.getUserName());
    }
    */

    public ConcurrentLinkedQueue<String> logIn(int _connectionId, UserData user){
        ConcurrentLinkedQueue<String> unsentMessages = user.logIn(_connectionId);
        connectionId_and_UserData.put(_connectionId, user);
        return  unsentMessages;
    }

    public void logOut(UserData user){// assume user login
        connectionId_and_UserData.remove(user.getConnectionId());
        user.logOut();
    }


    public UserData getUserById(int connectionId) {
        return connectionId_and_UserData.get(connectionId);
    }

    public UserData getLoggedInUserById(int connectionId) { //returns loggedIn user else null
            return connectionId_and_UserData.get(connectionId);

    }

    public UserData getUserByUserName(String username) {
        return userName_and_UserData.get(username);
    }


    public ConcurrentHashMap<String, UserData> getUserName_and_UserData() {
        return userName_and_UserData;
    }

    public String[] getRegisteredUsersByOrder() {
        return Registered_Users.toArray(new String[Registered_Users.size()]);//TODO check this method doesnt empty the array
    }
    //public void sendPost(String SenderUserName, String content, LinkedList<String> users_to_send){//TODO? do not check for users unregistered appears at list

    // }


    public String[] ReturnRegisterUsers(String[] message) {//returns message only with registered users
        String[] output = new String[message.length];
        int numOfUsers = Integer.parseInt(message[2]);
        if (numOfUsers > 0) {
            int nextAvailable = 3;
            lock.readLock().lock();
            for (int i = 0; i < message.length; i++) {
                if (i <= 1) {
                    output[i] = message[i];
                } else if (i > 2) {
                    boolean isRegister = userName_and_UserData.containsKey(message[i]);
                    if (!isRegister) {
                        numOfUsers--;
                    } else {
                        output[nextAvailable] = message[i];
                        nextAvailable++;
                    }
                }
            }
            lock.readLock().unlock();
            output[2] = String.valueOf(numOfUsers);
            return output;
        } else {
            return message;
        }
    }

    public void postAction(Connections<String> connections, String senderUserName, List<String> users_in_msg, String content){
        for (String userName : userName_and_UserData.keySet()) {// TODO check if to use readLock
            if (!userName.equals(senderUserName)) {
                if (userName_and_UserData.get(userName).isFollow(senderUserName) || users_in_msg.contains(userName)) {//send to all users follow sender or apears in his exstralist
                    synchronized (userName_and_UserData.get(userName)){
                        if (userName_and_UserData.get(userName).isLogIn()) {
                            connections.send(userName_and_UserData.get(userName).getConnectionId(), "9 1 " + senderUserName + " " + content);
                        }
                        else {
                            userName_and_UserData.get(userName).addUnsentNotification("9 1 " + senderUserName + " " + content);
                        }
                    }
                }
            } else {
                userName_and_UserData.get(senderUserName).addPost(content);//add post to sender data
            }
        }

    }


}
