package paxos;

import java.util.*;

public class PaxosMessage {
    ProcessId src;
    String src_name;
}

class LeaderTimeoutMessage extends PaxosMessage {
    BallotNumber lastActiveBallot_number;

    @Override
    public String toString() {
        return "LeaderTimeoutMessage{" +
                ", lastActiveBallot_number=" + lastActiveBallot_number +
                '}';
    }

    LeaderTimeoutMessage(ProcessId src, BallotNumber lastActiveBallot_number) {
        this.src = src;
        this.lastActiveBallot_number = lastActiveBallot_number;
    }

}

class PingMessage extends PaxosMessage {
    @Override
    public String toString() {
        return "PingMessage{" +
                "src=" + src +
                '}';
    }

    PingMessage(ProcessId src) {
        this.src = src;
    }

}

class PongMessage extends PaxosMessage {
    @Override
    public String toString() {
        return "PongMessage{" +
                "src=" + src +
                '}';
    }

    PongMessage(ProcessId src) {
        this.src = src;
    }

}

class P1aMessage extends PaxosMessage {
    BallotNumber ballot_number;

    @Override
    public String toString() {
        return "P1aMessage{" +
                "ballot_number=" + ballot_number +
                '}';
    }

    P1aMessage(ProcessId src, BallotNumber ballot_number) {
        this.src = src;
        this.src_name = src.name;
        this.ballot_number = ballot_number;
    }

}

class P1bMessage extends PaxosMessage {
    BallotNumber ballot_number;
    Set<PValue> accepted;

    P1bMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted) {
        this.src = src;
        this.src_name = src.name;
        this.ballot_number = ballot_number;
        this.accepted = accepted;
    }

    @Override
    public String toString() {
        return "P1bMessage{" +
                "ballot_number=" + ballot_number +
                ", accepted=" + accepted +
                '}';
    }
}

class P2aMessage extends PaxosMessage {
    BallotNumber ballot_number;
    int slot_number;
    Command command;

    P2aMessage(ProcessId src, BallotNumber ballot_number, int slot_number, Command command) {
        this.src = src;
        this.src_name = src.name;
        this.ballot_number = ballot_number;
        this.slot_number = slot_number;
        this.command = command;
    }

    @Override
    public String toString() {
        return "P2aMessage{" +
                "ballot_number=" + ballot_number +
                ", slot_number=" + slot_number +
                ", command=" + command +
                '}';
    }
}

class P2bMessage extends PaxosMessage {
    BallotNumber ballot_number;
    int slot_number;

    P2bMessage(ProcessId src, BallotNumber ballot_number, int slot_number) {
        this.src = src;
        this.src_name = src.name;
        this.ballot_number = ballot_number;
        this.slot_number = slot_number;
    }

    @Override
    public String toString() {
        return "P2bMessage{" +
                "ballot_number=" + ballot_number +
                ", slot_number=" + slot_number +
                '}';
    }
}

class PreemptedMessage extends PaxosMessage {
    BallotNumber ballot_number;

    PreemptedMessage(ProcessId src, BallotNumber ballot_number) {
        this.src = src;
        this.ballot_number = ballot_number;
    }

    @Override
    public String toString() {
        return "PreemptedMessage{" +
                "ballot_number=" + ballot_number +
                '}';
    }
}

class AdoptedMessage extends PaxosMessage {
    BallotNumber ballot_number;
    Set<PValue> accepted;

    AdoptedMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted) {
        this.src = src;
        this.src_name = src.name;
        this.ballot_number = ballot_number;
        this.accepted = accepted;
    }

    @Override
    public String toString() {
        return "AdoptedMessage{" +
                "ballot_number=" + ballot_number +
                ", accepted=" + accepted +
                '}';
    }
}

class DecisionMessage extends PaxosMessage {
    ProcessId src;
    int slot_number;
    Command command;

    public DecisionMessage(ProcessId src, int slot_number, Command command) {
        this.src = src;
        this.src_name = src.name;
        this.slot_number = slot_number;
        this.command = command;
    }

    @Override
    public String toString() {
        return "DecisionMessage{" +
                "src=" + src +
                ", slot_number=" + slot_number +
                ", command=" + command +
                '}';
    }
}

class RequestMessage extends PaxosMessage {
    Command command;

    public RequestMessage(ProcessId src, Command command) {
        this.src = src;
        this.src_name = src.name;
        this.command = command;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "command=" + command +
                '}';
    }
}

class TxMessage extends PaxosMessage {
    Command command;
    public ProcessId delayReplica;
    public int delayReplicaTime;

    public TxMessage(ProcessId src, Command command, ProcessId pid, int delay) {
        this.src = src;
        this.src_name = src.name;
        this.command = command;
        this.delayReplica = pid;
        this.delayReplicaTime = delay;
    }

    @Override
    public String toString() {
        return "TxMessage{" +
                "command=" + command +
                '}';
    }
}

class ResponseMessage extends PaxosMessage {
    Command command;
    Account account;

    public ResponseMessage(ProcessId src, Command command, Account account) {
        this.src = src;
        this.src_name = src.name;
        this.command = command;
        this.account = account;
    }

    @Override
    public String toString() {
        return "Command: " + command + " done. A/c update :" + account;
    }
}

class ProposeMessage extends PaxosMessage {
    int slot_number;
    Command command;

    public ProposeMessage(ProcessId src, int slot_number, Command command) {
        this.src = src;
        this.src_name = src.name;
        this.slot_number = slot_number;
        this.command = command;
    }

    @Override
    public String toString() {
        return "ProposeMessage{" +
                "slot_number=" + slot_number +
                ", command=" + command +
                '}';
    }
}
