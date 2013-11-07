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

    @Override
    public int hashCode() {
        int result = client.hashCode();
        result = 31 * result + req_id;
        result = 31 * result + op.hashCode();
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;

        Command command = (Command) o;

        if (req_id != command.req_id) return false;
        if (!client.equals(command.client)) return false;
        if (!op.equals(command.op)) return false;

        return true;
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
