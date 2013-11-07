package paxos;

import java.util.HashMap;
import java.util.Map;

public class Leader extends Process {
    ProcessId[] acceptors;
    ProcessId[] replicas;
    BallotNumber ballot_number;
    boolean active = false;
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
        new Heartbeat(env, new ProcessId("heartbeat:" + me + ":"),me);
        env.addProc(me, this);
    }

    public void body() {
        logger.log(messageLevel, "Here I am: " + me);

        new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                me, acceptors, ballot_number);
        while (!stop_request) {
            PaxosMessage msg = getNextMessage();

            if (msg instanceof ProposeMessage) {
                ProposeMessage m = (ProposeMessage) msg;
                if (!proposals.containsKey(m.slot_number)) {
                    proposals.put(m.slot_number, m.command);
                    if (active) {
                        new Commander(env,
                                new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
                                me, acceptors, replicas, ballot_number, m.slot_number, m.command);
                    }
                } else {
                    //TODO : WHat should happen here ? ideally replica should not be sending any msg with slot
                    // no already propsoed for
                    logger.log(messageLevel, "This Slot is already occupied, therefore NO ACTION");
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
                                me, acceptors, replicas, ballot_number, sn, proposals.get(sn));
                    }
                    active = true;
                }
            } else if (msg instanceof PreemptedMessage) {
                PreemptedMessage m = (PreemptedMessage) msg;
                if (ballot_number.compareTo(m.ballot_number) < 0) {
                    //TODO : Add the failure detection
                    ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
                    new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
                            me, acceptors, ballot_number);
                    active = false;
                }
            } else {
                System.err.println("paxos.Leader: unknown msg type");
            }
        }
    }
}
