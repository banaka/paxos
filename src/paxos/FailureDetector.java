package paxos;

import java.util.Properties;
import java.util.logging.Level;

public class FailureDetector extends Process {
    ProcessId forLeader;
    ProcessId activeLeader;
    boolean leaderAlive = false;
    int timeout;

    public FailureDetector(Env env, ProcessId me, ProcessId leader, ProcessId checkLeader) {
        this.env = env;
        this.me = me;
        this.forLeader = leader;
        this.activeLeader = checkLeader;
        setLogger();
        loadProp();
        env.addProc(me, this);
    }

    Properties loadProp() {
        super.loadProp();
        try {
            timeout = Integer.parseInt(prop.getProperty("failureDetectionTimeout"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return prop;
    }

    public void body() {
        while (!stop_request()) {
            for (Leader i : env.leaders) {
                if (i.me == activeLeader)
                    sendMessage(i.heartbeat.me, new PingMessage(me));
            }
            try {
                leaderAlive = false;
                Thread.sleep(this.timeout);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());

            }
            PaxosMessage msg = getNextMessage();
            if (msg instanceof PongMessage) {
                PongMessage pong = (PongMessage) msg;
                if (pong.src.equals(activeLeader)) {
                    leaderAlive = true;
                }
            }
            if (leaderAlive) {
                sendMessage(forLeader, new LeaderTimeoutMessage(me, activeLeader));
            }
        }
    }
}
