package bgu.spl.net.api.bidi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseBGS {

    private ConcurrentHashMap<String,BGSUser> RegisteredUsers; // Map of all registered users. Key is - Username, Value is - BGSUser
    private String registeredUsersString;
    private AtomicInteger registeredCounter;
    private Object registerLock;

    public DatabaseBGS() {
        RegisteredUsers = new ConcurrentHashMap<>();
        registeredUsersString = "";
        registeredCounter = new AtomicInteger(0);
        registerLock = new Object();
    }


    public AtomicInteger getRegisteredCounter() {
        return registeredCounter;
    }

    public String getRegisteredUsersString() {
        return registeredUsersString;
    }

    public void setRegisteredUsersString(String registeredUsersString) {
        this.registeredUsersString = registeredUsersString;
    }

    public ConcurrentHashMap<String, BGSUser> getRegisteredUsersMap() {
        return RegisteredUsers;
    }


    public void register(String username, String password){
        BGSUser newuser = new BGSUser(username,password);
        RegisteredUsers.put(username,newuser);
    }

    public Object getRegisterLock() {
        return registerLock;
    }
}



