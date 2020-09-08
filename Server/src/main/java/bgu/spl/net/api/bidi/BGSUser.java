package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.NotificationMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BGSUser {

    private String username;
    private String password;
    private boolean LoggedIn;
    private int connectionID; // represents the current connectionID - -1 is value for logged off.
    private AtomicInteger PostsCounter;
    private ConcurrentHashMap<String,BGSUser> followersList;
    private ConcurrentHashMap<String,BGSUser> followingList;
    private ConcurrentLinkedQueue<NotificationMessage> FutureMessageList;


    public BGSUser(String username, String password) {
        LoggedIn = false;
        connectionID = -1; //when registering, creating new BGSUser , not yet logged in only registered.
        this.username = username;
        this.password = password;
        PostsCounter = new AtomicInteger(0);
        followersList = new ConcurrentHashMap<>();
        followingList = new ConcurrentHashMap<>();
        FutureMessageList = new ConcurrentLinkedQueue<>();

    }


    public ConcurrentLinkedQueue<NotificationMessage> getFutureMessageList() {
        return FutureMessageList;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public  ConcurrentHashMap<String,BGSUser> getFollowersList() {
        return followersList;
    }

    public ConcurrentHashMap<String, BGSUser> getFollowingList() {
        return followingList;
    }

    public boolean isLoggedIn() {
        return LoggedIn;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void LogIn(int connectionID){
        LoggedIn = true;
        this.connectionID = connectionID;
    }

    public void LogOut(){
        LoggedIn = false;
        connectionID = -1;
    }


    public AtomicInteger getPostsCounter() {
        return PostsCounter;
    }
}