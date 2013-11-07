package paxos;

import java.util.ArrayList;
import java.util.List;

public class Client extends Process {
    List<PaxosMessage> queue = new ArrayList<PaxosMessage>();
    int currentMessage = 0;
    int currentRcvdMessage = 0;

    public Client(Env env, ProcessId me){
        this.env = env;
        this.me = me;
        setLogger();
        env.addProc(me, this);
    }

    public void checkIfMessageCanBeSent(){
        if(currentMessage < (queue.size()))  {
            PaxosMessage msg = queue.get(currentMessage);
            for (Replica r: env.replicas) {
                sendMessage(r.me, new RequestMessage(me, new Command(me, currentMessage, ((TxMessage) msg).command.op)));
            }
        }
    }

    @Override
    void body() {
        logger.log(messageLevel, "Here I am: " + me);
        while (!stop_request()) {
            PaxosMessage msg = getNextMessage();

            //Add to Queue
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
