package paxos;

public class Heartbeat extends Process {
    ProcessId forLeader;

    public Heartbeat(Env env, ProcessId me, ProcessId leader) {
        this.env = env;
        this.me = me;
        this.forLeader = leader;
        setLogger();
        loadProp();
        env.addProc(me, this);
    }

    public void body() {
        while (!stop_request()) {
            PaxosMessage msg = getNextMessage();
            if (msg instanceof PingMessage) {
                PingMessage ping = (PingMessage) msg;
                sendMessage(ping.src, new PongMessage(me));
            }
        }
    }
}
