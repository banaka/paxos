package paxos;

import java.util.ArrayList;
import java.util.List;

class TimeoutClock extends Thread {
    int oldMessageCount;
    int timeout;
    Client client;
    TimeoutClock(int oldMessageCount, int timeout, Client client){
        this.oldMessageCount = oldMessageCount;
        this.timeout = timeout;
        this.client = client;
    }
    public void run() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
        if(client.currentMessage == oldMessageCount) {
            System.err.println("Client Timeout! Resending request to Replicas...");
            client.checkIfMessageCanBeSent();
        }
    }
}

class Messenger extends Thread {
    PaxosMessage message;
    Process dest;
    Client src;

    Messenger(PaxosMessage m, Process d, Client s){
        this.message = m;
        this.dest = d;
        this.src = s;
    }

    public void run() {
        if(((TxMessage)message).delayReplica == null || dest.me.equals(((TxMessage)message).delayReplica)) {
            try {
                Thread.sleep(((TxMessage)message).delayReplicaTime);
            } catch (InterruptedException e) {}
        }
        src.sendMessage(dest.me, new RequestMessage(src.me, new Command(src.me, src.currentMessage, ((TxMessage) message).command.op)));
    }
}
public class Client extends Process {
    List<PaxosMessage> queue = new ArrayList<PaxosMessage>();
    int currentMessage = 0;
    int clientTimeout;

    public Client(Env env, ProcessId me){
        this.env = env;
        this.me = me;
        setLogger();
        loadProp();
        clientTimeout = Integer.parseInt(prop.getProperty("clientTimeout"));
        env.addProc(me, this);
    }

    public void checkIfMessageCanBeSent(){
        if(currentMessage < (queue.size()))  {
            PaxosMessage msg = queue.get(currentMessage);
            new TimeoutClock(currentMessage, clientTimeout, this).start();
            for (Replica r: env.replicas) {
                new Messenger(msg,r,this).start();
            }
        }
    }

    @Override
    void body() {
        logger.log(messageLevel, "Here I am: " + me);
        while (!stop_request()) {
            PaxosMessage msg = getNextMessage();

            if(msg instanceof TxMessage){
                queue.add(msg);
                checkIfMessageCanBeSent();
            } else if(msg instanceof ResponseMessage) {
                if(((ResponseMessage) msg).command.req_id == currentMessage){
                    if(((ResponseMessage) msg).account == null)
                        System.out.println("ERR (Ignored): " + ((ResponseMessage) msg).command);
                    else
                        System.out.println("DONE: "+((ResponseMessage) msg).command+" A/c: "+((ResponseMessage) msg).account);
                    currentMessage++;
                    checkIfMessageCanBeSent();
                }
            }

        }
    }
}
