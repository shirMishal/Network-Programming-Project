package bgu.spl.net.impl.BGS;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UserData {
    //TODO add read write locks
    private String userName;
    private String password;
    private int connectionId;
    private AtomicBoolean logIn;
    private ConcurrentHashMap< String, Integer> followAfter; //private List<String> followAfter;//no mean for integer
    private List<String> posts;
    private HashMap<String,String> privateMessages;
    private AtomicInteger numOfPosts;
    private AtomicInteger numOfFollowers;
    private AtomicInteger numOfFollowAfter;
    private ConcurrentLinkedQueue<String> unsentNotifications;// notifications will send as soon as user will login
                                                                //"0 postusername content"
    public UserData(String _userName,String _password){
        userName = _userName;
        password = _password;
        connectionId = -1;
        logIn = new AtomicBoolean(false);
        followAfter = new ConcurrentHashMap<>();
        posts = new LinkedList<>();
        privateMessages = new HashMap<>();
        numOfFollowAfter = new AtomicInteger(0);
        numOfFollowers = new AtomicInteger(0);
        numOfPosts = new AtomicInteger(0);
        unsentNotifications = new ConcurrentLinkedQueue<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public boolean isLogIn() {
        return logIn.get();
    }

    public boolean isFollow(String otherUser){
        return followAfter.containsKey(otherUser);
    }

    public ConcurrentHashMap<String, Integer> getFollowAfter() {
        return followAfter;
    }

    public List<String> getPosts() {
        return posts;
    }

    public HashMap<String,String> getPrivateMessages() {
        return privateMessages;
    }

    public int getNumOfPosts() {
        return numOfPosts.get();
    }

    public int getNumOfFollowers() {
        return numOfFollowers.get();
    }

    public int getNumOfFollowAfter() {
        return numOfFollowAfter.get();
    }

    public ConcurrentLinkedQueue<String> getUnsentNotifications() {
        return unsentNotifications;
    }

    //login change boolean and send back notifications that should be sent to client
    public ConcurrentLinkedQueue<String> logIn(int _connectionId){
        logIn.getAndSet(true);
        connectionId = _connectionId;
        return  unsentNotifications;
    }

    public void logOut(){
        logIn.getAndSet(false);
        connectionId = -1;
        unsentNotifications.clear();
    }

    public void addPost(String post){
        numOfPosts.incrementAndGet();
        posts.add(post);
    }

    public void addUnsentNotification(String Notification){

        unsentNotifications.add(Notification);
    }

    public void addPrivateMessage(String ToUser,String pm){
        privateMessages.put(ToUser, pm);
    }

    public void addFollower(){
        numOfFollowers.incrementAndGet();
    }
    public void eraseFollower(){
        numOfFollowers.decrementAndGet();
    }

    public String[] FollowUnfollowUpdate(String[] userList){//TODO check on case numOfUsers=0 assum all users registered
        //userList[1] contains follow or unfollow code
        //userList[2] contains numOfUsers
        if (userList!= null && userList[1].equals("0")){
            return FollowUpdate(userList);
        }
        else if (userList!= null && userList[1].equals("1")){
            return UnfollowUpdate(userList);
        }
        return null;//we Assume we doesnt get here
    }

// follow/unfollow returns [ num of success, userlist]
    private String[] FollowUpdate(String[] userList) {
        String[] output = new String[userList.length];
        int SuccessCounter = 0;
        int numOfUsers = Integer.parseInt(userList[2]);
        if (numOfUsers!= 0){
            for (int i = 3; i< numOfUsers +3;i++) {
                if (! followAfter.containsKey(userList[i])){
                    followAfter.put(userList[i],numOfFollowAfter.get()+1);
                    SuccessCounter++;
                    numOfFollowAfter.incrementAndGet();
                    output[SuccessCounter]= userList[i];
                }
            }
        }
        String success =  String.valueOf(SuccessCounter);
        output[0] = success;
        return output;
    }

    private String[] UnfollowUpdate(String[] userList) {
        String[] output = new String[userList.length];
        int SuccessCounter = 0;
        int numOfUsers = Integer.parseInt(userList[2]);
        if (numOfUsers!= 0){
            for (int i = 3; i< numOfUsers +3;i++) {
                if (followAfter.containsKey(userList[i])){
                    followAfter.remove(userList[i]);
                    SuccessCounter++;
                    numOfFollowAfter.decrementAndGet();
                    output[SuccessCounter]= userList[i];
                }
            }
        }
        String success =  String.valueOf(SuccessCounter);
        output[0] = success;
        return output;

    }
}
