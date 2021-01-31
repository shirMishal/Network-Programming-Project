package bgu.spl.net.impl.BGS;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BidiProtocolImpl implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<String> connections;
    private BgsData data;

    public BidiProtocolImpl(BgsData _data) {
        shouldTerminate = false;
        data = _data;
    }

    public void start(int _connectionId, Connections<String> _connections) {
        connectionId = _connectionId;
        connections = _connections;
        shouldTerminate = false;
    }

    public void process(String message) {
        String[] messageArr = message.split(" ");
        //System.out.println(messageArr[0]);
        if (messageArr != null) {
            int m_code = Integer.parseInt(messageArr[0]);
            ;
            switch (m_code) {
                case 1: {
                    processRegister(messageArr);
                    break;
                }
                case 2: {
                    processLogin(messageArr);
                    break;
                }
                case 3: {//logout
                    processLogout(messageArr);
                    break;
                }
                case 4: {
                    processFollow(messageArr);
                    break;
                }
                case 5: {
                    processPost(messageArr);
                    break;
                }
                case 6: {
                    processPrivateMessage(messageArr);
                    break;
                }
                case 7: {//userlist
                    processUserlist(messageArr);
                    break;
                }
                case 8: {
                    processStat(messageArr);
                    break;
                }
            }
        }

    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void processRegister(String[] message) {
        UserData ud = new UserData(message[1], message[2]);
        Boolean succeedRegister = data.Register(message[1],ud);
        if(succeedRegister){
            connections.send(connectionId, "10 1");
        }
        else{
            connections.send(connectionId, "11 1");
        }

    }

    private void processLogin(String[] message) {
        UserData user = data.getUserByUserName(message[1]);//null if user is not register
        if (data.isRegisterByUserName(message[1]) && (!user.isLogIn()) && message[2].equals(user.getPassword())) {
            synchronized (user){
                ConcurrentLinkedQueue<String> unsentmessages = data.logIn(connectionId, user);
                connections.send(connectionId, "10 2");
                while (!unsentmessages.isEmpty()) {
                    String m = unsentmessages.poll();
                    connections.send(connectionId, m);
                }
            }
        } else {
            connections.send(connectionId, "11 2");
        }
    }

    private void processLogout(String[] message) {
        UserData User = data.getUserById(connectionId);//null if user is not logged in
        if (User != null && User.isLogIn()) {
            synchronized (User){
                data.logOut(User);
                connections.send(connectionId, "10 3");
            }
        }
        else {
            connections.send(connectionId, "11 3");
        }

    }

    private void processFollow(String[] message) {//assume not send zero users
            UserData user = data.getLoggedInUserById(connectionId);
            if (user != null) {
                String[] message_input = data.ReturnRegisterUsers(message);
                if (message_input[2].equals("0")) {
                    connections.send(connectionId, "11 4");//no registered users at list
                } else {
                    String[] result = user.FollowUnfollowUpdate(message_input);
                    if (result[0].equals("0")) {//num of success follow
                        connections.send(connectionId, "11 4");
                    }
                    else {
                        //String ack = String.join(" ", result);

                        int users = Integer.parseInt(result[0]);
                        String ack = "10 4 " + users + " ";
                        for (int i = 1; i <= users; i++) {
                            ack += result[i];
                            if (i != users) {
                                ack += " ";
                            }
                            if (message[1].equals("0")) {//follow
                                data.getUserByUserName(result[i]).addFollower();
                            } else if (message[1].equals("1")) {//follow
                                data.getUserByUserName(result[i]).eraseFollower();
                            }
                        }
                        connections.send(connectionId, ack);
                    }
                }
            }
            else {
                connections.send(connectionId, "11 4");
            }

    }

    private void processPost(String[] message) {
            UserData user = data.getLoggedInUserById(connectionId);//null if user is not register
            if (user != null) {                //save content and extra users to sendto
                String content = "";
                List<String> users_in_msg = new LinkedList<>();
                for (int i = 1; i < message.length; i++) {
                    if (message[i].charAt(0) == '@') {
                        users_in_msg.add(message[i].substring(1));
                    }
                    content += message[i];
                    if (i != message.length - 1) {
                        content += " ";
                        //content = content.substring(0, content.length()-1);
                    }
                }
                String senderUserName = user.getUserName();
                data.postAction(connections, senderUserName, users_in_msg, content);
                connections.send(connectionId, "10 5");//send ack
            }
            else {
                connections.send(connectionId, "11 5");
            }

    }

    private void processPrivateMessage(String[] message) {
            UserData user = data.getLoggedInUserById(connectionId);//null if user is not register
            if (user != null) {
                String content = "";
                for (int i = 2; i < message.length; i++) {
                    content += message[i];
                    if (i < message.length - 1)
                        content += " ";
                }
                UserData userToSendPm = data.getUserByUserName(message[1]);
                if(userToSendPm != null) {
                    synchronized (userToSendPm){
                        user.addPrivateMessage(userToSendPm.getUserName(), content);
                        if (userToSendPm.isLogIn()) {
                            connections.send(userToSendPm.getConnectionId(), "9 0 " + user.getUserName() + " " + content);
                        } else {
                            userToSendPm.addUnsentNotification("9 0 " + user.getUserName() + " " + content);
                        }
                        connections.send(connectionId, "10 6");
                    }
                }
                else {
                    connections.send(connectionId, "11 6");
                }
            }
            else {
                connections.send(connectionId, "11 6");
            }

    }


    private void processUserlist(String[] message) {
            UserData user = data.getLoggedInUserById(connectionId);//null if user is not register
            if (user != null) {
                String[] registeredUsers = data.getRegisteredUsersByOrder();
                String userNames = "";
                for (String s : registeredUsers) {
                    userNames += (" " + s);
                }
                String msg = "10 7 " + registeredUsers.length + userNames;
                connections.send(connectionId, msg);
            }
            else {
                connections.send(connectionId, "11 7");
            }
    }

    private void processStat(String[] message) {
        UserData user = data.getLoggedInUserById(connectionId);//null if user is not register
        if (user != null) {
            UserData userStat = data.getUserByUserName(message[1]);
            if (userStat != null) {
                String msg = "10 8 " + userStat.getNumOfPosts() + " " + userStat.getNumOfFollowers() + " " + userStat.getNumOfFollowAfter();
                connections.send(connectionId, msg);//send ack
            }
            else {
                connections.send(connectionId, "11 8");
            }
        }
        else {
            connections.send(connectionId, "11 8");
        }
    }

}
