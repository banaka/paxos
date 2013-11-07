package paxos;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

public class Heartbeat extends Process {
    ProcessId forLeader;
    Set<ProcessId> upSet;
    int pingDelay;

    public Heartbeat(Env env, ProcessId me, ProcessId leader) {
        this.env = env;
        this.me = me;
        this.forLeader = leader;
        this.upSet = new HashSet<ProcessId>();
        setLogger();
        loadProp();
        env.addProc(me, this);
    }

    Properties loadProp() {
        super.loadProp();
        try {
            pingDelay = Integer.parseInt(prop.getProperty("heartBeatDelay"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return prop;
    }

    public void body() {
//        while (true) {
//            try {
//                Thread.sleep(this.pingDelay);
//            } catch (Exception e) {
//                logger.log(Level.SEVERE, e.getMessage());
//
//            }
//            for (Leader i : env.leaders) {
//                if (i.me != this.forLeader)
//                    sendMessage(i.heartbeat.me, new PingMessage(me));
//            }
//            PaxosMessage msg = getNextMessage();
//            if (msg instanceof PingMessage) {
//                PingMessage ping = (PingMessage) msg;
//                upSet.add(ping.src);
//                sendMessage(ping.src, new PingMessage(me));
//            }
//        }

    }
}
