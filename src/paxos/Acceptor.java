package paxos;

import java.util.HashSet;
import java.util.Set;

public class Acceptor extends Process {
    BallotNumber ballot_number = null;
	Set<PValue> accepted = new HashSet<PValue>();
//    long leaseEndTime;
//    ProcessId leaser;

	public Acceptor(Env env, ProcessId me){
		this.env = env;
		this.me = me;
        setLogger();
        loadProp();
        env.addProc(me, this);
	}

	public void body(){
        logger.log(messageLevel, "Here I am: " + me);
		while (!stop_request()) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof P1aMessage) {
				P1aMessage m = (P1aMessage) msg;
//                long currentTime = System.currentTimeMillis();
//                logger.log(messageLevel, "BN:"+ballot_number+"leaser:"+leaser+"B:"+m+"leaseEnd:"+leaseEndTime+"T="+currentTime);
//                if(ballot_number != null && leaseEndTime > currentTime &&
//                        !m.ballot_number.leader_id.equals(leaser)) {
//                    logger.log(messageLevel, "Already in lease with "+leaser+" for "+leaseEndTime+" Current: "
//                    + currentTime + "Ignoring : " + m.ballot_number);
//                } else
                if (ballot_number == null ||
						ballot_number.compareTo(m.ballot_number) < 0) {
					ballot_number = m.ballot_number;
//                    leaseEndTime = m.leaseEndTime;
//                    leaser = m.ballot_number.leader_id;
				}
				sendMessage(m.src, new P1bMessage(me, ballot_number, new HashSet<PValue>(accepted)));
			}
			else if (msg instanceof P2aMessage) {
				P2aMessage m = (P2aMessage) msg;
//                long currentTime = System.currentTimeMillis();
//                if(ballot_number != null && leaseEndTime > currentTime &&
//                        m.ballot_number.leader_id != leaser) {
//                    logger.log(messageLevel, "Already in lease with "+leaser+" for "+leaseEndTime+" Current: "
//                            + currentTime + "Ignoring : " + m.ballot_number);
//                } else
                if (ballot_number == null ||
						ballot_number.compareTo(m.ballot_number) <= 0) {
					ballot_number = m.ballot_number;
					accepted.add(new PValue(ballot_number, m.slot_number, m.command));
				}
				sendMessage(m.src, new P2bMessage(me, ballot_number, m.slot_number));
			}
		}
	}
}
