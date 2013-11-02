package paxos;

public class Command {
	ProcessId client;
	int req_id;
    Operation op;

	public Command(ProcessId client, int req_id, Operation op){
		this.client = client;
		this.req_id = req_id;
		this.op = op;
	}

	public boolean equals(Object o) {
		Command other = (Command) o;
		return req_id == other.req_id && op.equals(other.op);
	}

	public String toString(){
		return "paxos.Command(" + client + ", " + req_id + ", " + op + ")";
	}
}
