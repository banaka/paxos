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
		return client == other.client && req_id == other.req_id && op.equals(other.op);
	}

	public String toString(){
		return "paxos.Command(" + client + ", " + req_id + ", " + op + ")";
	}
}

enum Commands {
    KILL("KILL <pid> // Kills the pid thread"),
    SHOW("Shows all live pids"),
    HELP("Shows this stuff again");

    private String description;
    Commands(String description){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }
}
