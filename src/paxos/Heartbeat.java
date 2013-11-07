package paxos;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class Heartbeat extends Process {
    ProcessId forLeader;
    Set<ProcessId> upSet ;

    public Heartbeat(Env env, ProcessId me, ProcessId leader) {
        this.env = env;
        this.me = me;
        this.forLeader = leader;
        this.upSet = new HashSet<ProcessId>();
        setLogger();
        loadProp();
        env.addProc(me, this);
    }

    public void body() {
        while (true) {
            for (Leader i : env.leaders) {
                sendMessage(i.me, new PingMessage(me));

            }
            PaxosMessage msg = getNextMessage();
            if (msg instanceof PingMessage) {
                PingMessage ping = (PingMessage) msg;
                upSet.add(ping.src);
                sendMessage(ping.src, new PingMessage(me));
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());

            }
        }

    }
}
