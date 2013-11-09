package paxos;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class Leader extends Process {
    ProcessId[] acceptors;
    ProcessId[] replicas;
    BallotNumber ballot_number;
    BallotNumber lastActiveBallot_number;
    boolean active = false;
    Heartbeat heartbeat;
    FailureDetector failureDetector;
    int failureDetectionTimeout;
    boolean failureDetection;
    Map<Integer, Command> proposals = new HashMap<Integer, Command>();

    public Leader(Env env, ProcessId me, ProcessId[] acceptors,
                  ProcessId[] replicas) {
        this.env = env;
        this.me = me;
        ballot_number = new BallotNumber(0, me);
        this.acceptors = acceptors;
        this.replicas = replicas;
        this.setLogger();
        loadProp();
        heartbeat = new Heartbeat(env, new ProcessId("heartbeat:" + me + ":"), this);
        env.addProc(me, this);
    }

    Properties loadProp() {
        super.loadProp();
        try {
            failureDetection = "TRUE".equalsIgnoreCase(prop.getProperty("failureDetection")) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
        }
        return prop;
    }

    public void body() {
        logger.log(messageLevel, "Here I am: " + me);

        new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                this, acceptors, ballot_number);
        while (!stop_request()) {
            PaxosMessage msg = getNextMessage();

            if (msg instanceof ProposeMessage) {
                ProposeMessage m = (ProposeMessage) msg;
                if (!proposals.containsKey(m.slot_number)) {
                    proposals.put(m.slot_number, m.command);
                    if (active) {
                        new Commander(env,
                                new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
                                this, acceptors, replicas, ballot_number, m.slot_number, m.command);
                    }
                } else {
                    logger.log(messageLevel, "This Slot is already proposed for, therefore NO ACTION");
                }
            } else if (msg instanceof AdoptedMessage) {
                AdoptedMessage m = (AdoptedMessage) msg;

                if (ballot_number.equals(m.ballot_number)) {
                    Map<Integer, BallotNumber> max = new HashMap<Integer, BallotNumber>();
                    for (PValue pv : m.accepted) {
                        BallotNumber bn = max.get(pv.slot_number);
                        if (bn == null || bn.compareTo(pv.ballot_number) < 0) {
                            max.put(pv.slot_number, pv.ballot_number);
                            proposals.put(pv.slot_number, pv.command);
                        }
                    }

                    for (int sn : proposals.keySet()) {
                        new Commander(env,
                                new ProcessId("commander:" + me + ":" + ballot_number + ":" + sn),
                                this, acceptors, replicas, ballot_number, sn, proposals.get(sn));
                    }
                    active = true;
                }
            } else if (msg instanceof PreemptedMessage) {
                if (failureDetection) {
                    PreemptedMessage m = (PreemptedMessage) msg;
                    if (ballot_number.compareTo(m.ballot_number) < 0) {
                        ProcessId activeLeader = m.ballot_number.leader_id;
                        lastActiveBallot_number = m.ballot_number;
                        if(failureDetector == null)
                            failureDetector = new FailureDetector(env, new ProcessId("failureDetector:" + me + ":" + activeLeader), this, lastActiveBallot_number);
                        logger.log(messageLevel, "Created a FailureDetector for " + activeLeader);
                        active = false;
                    }
                } else {
                    PreemptedMessage m = (PreemptedMessage) msg;
                    if (ballot_number.compareTo(m.ballot_number) < 0) {
                        ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
                        new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                                this, acceptors, ballot_number);
                        active = false;
                    }
                }
            } else if (msg instanceof LeaderTimeoutMessage) {
                LeaderTimeoutMessage m = (LeaderTimeoutMessage) msg;
                if (ballot_number.compareTo(m.lastActiveBallot_number) < 0) {
                    ballot_number = new BallotNumber(m.lastActiveBallot_number.round + 1, me);
                    new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                            this, acceptors, ballot_number);
                    active = false;
                }
            } else {
                System.err.println("paxos.Leader: unknown msg type");
            }
        }
    }
}
