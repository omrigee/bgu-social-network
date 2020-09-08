package bgu.spl.net.api.bidi;


import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    //FIELDS:

    ConcurrentHashMap<Integer , bgu.spl.net.srv.ConnectionHandler> ConnectionsMap ;


    public ConnectionsImpl() {
        ConnectionsMap = new ConcurrentHashMap<>();
    }

    public void addConnection(int connectionID, ConnectionHandler handler){
        this.ConnectionsMap.put(connectionID,handler);
    }


    @Override
    public boolean send(int connectionId, T msg) {

            if (!ConnectionsMap.containsKey(connectionId))
                return false;
         else {
             ConnectionsMap.get(connectionId).send(msg);
             return true;
         }
    }



    @Override
    public void broadcast(T msg) {
       for (Integer connectionID : ConnectionsMap.keySet()){
           send(connectionID,msg);
       }
    }

    @Override
    public void disconnect(int connectionId) {

    }
}
