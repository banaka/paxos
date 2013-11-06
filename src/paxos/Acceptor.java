package paxos;

import java.util.HashSet;
import java.util.Set;

public class Acceptor extends Process {
    BallotNumber ballot_number = null;
	Set<PValue> accepted = new HashSet<PValue>();

	public Acceptor(Env env, ProcessId me){
		this.env = env;
		this.me = me;
        setLogger();
        loadProp();
        env.addProc(me, this);
	}

	public void body(){
        logger.log(messageLevel, "Here I am: " + me);
		while (!stop_request) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof P1aMessage) {
				P1aMessage m = (P1aMessage) msg;

				if (ballot_number == null ||
						ballot_number.compareTo(m.ballot_number) < 0) {
					ballot_number = m.ballot_number;
				}
				sendMessage(m.src, new P1bMessage(me, ballot_number, new HashSet<PValue>(accepted)));
			}
			else if (msg instanceof P2aMessage) {
				P2aMessage m = (P2aMessage) msg;

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
