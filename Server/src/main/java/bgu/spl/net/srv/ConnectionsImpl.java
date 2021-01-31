package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap < Integer, ConnectionHandler<T> > connectionId_and_Handler;
    private AtomicInteger id;
    private ReentrantReadWriteLock lock;

    public ConnectionsImpl(){
        connectionId_and_Handler = new ConcurrentHashMap<>();
        id = new AtomicInteger(0);
        lock = new ReentrantReadWriteLock();
    }

    public int add (ConnectionHandler<T>  Handler){
        Integer connectionId = id.getAndIncrement();
        lock.writeLock().lock();
        connectionId_and_Handler.put(connectionId,Handler);
        lock.writeLock().unlock();
        return connectionId.intValue();
    }

    public boolean send(int connectionId, T msg){
        ConnectionHandler<T> handler = connectionId_and_Handler.get(connectionId);

        if(handler!= null){
            synchronized (handler){
            handler.send(msg);
            return true;
            }
        }

        return false;
    }

    public void broadcast(T msg){
        lock.readLock().lock();//TODO check on synch
            for (Integer key : connectionId_and_Handler.keySet()){
                ConnectionHandler<T> handler = connectionId_and_Handler.get(key);
                handler.send(msg);
            }
        lock.readLock().unlock();
    }

    public void disconnect(int connectionId){
        lock.writeLock().lock();
        connectionId_and_Handler.remove(connectionId);
        lock.writeLock().unlock();
    }

}
