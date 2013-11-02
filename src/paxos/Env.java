package paxos;

import java.io.FileInputStream;
import java.util.*;

public class Env {
    Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
    String TX_MSG_SEPARATOR = "\\$";

    synchronized void sendMessage(ProcessId dst, PaxosMessage msg) {
        Process p = procs.get(dst);
        if (p != null) {
            p.deliver(msg);
        }
    }

    synchronized void addProc(ProcessId pid, Process proc) {
        procs.put(pid, proc);
        proc.start();
    }

    synchronized void removeProc(ProcessId pid) {
        procs.remove(pid);
    }

    void run(String[] args) {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("config.properties"));
            int nAcceptors = Integer.parseInt(prop.getProperty("nAcceptors"));
            int nReplicas = Integer.parseInt(prop.getProperty("nReplicas"));
            int nLeaders = Integer.parseInt(prop.getProperty("nLeaders"));
            int nRequests = Integer.parseInt(prop.getProperty("nRequests"));

            ProcessId[] acceptors = new ProcessId[nAcceptors];
            ProcessId[] replicas = new ProcessId[nReplicas];
            ProcessId[] leaders = new ProcessId[nLeaders];

            for (int i = 0; i < nAcceptors; i++) {
                acceptors[i] = new ProcessId("acceptor:" + i);
                Acceptor acc = new Acceptor(this, acceptors[i]);
            }
            for (int i = 0; i < nReplicas; i++) {
                replicas[i] = new ProcessId("replica:" + i);
                Replica repl = new Replica(this, replicas[i], leaders);
            }
            for (int i = 0; i < nLeaders; i++) {
                leaders[i] = new ProcessId("leader:" + i);
                Leader leader = new Leader(this, leaders[i], acceptors, replicas);
            }

            for (int i = 1; i <= nRequests; i++) {
                ProcessId pid = new ProcessId("client:" + i);
                String[] opTxt=prop.getProperty("Operation"+i).split(TX_MSG_SEPARATOR,2);
                Operation op = new Operation(opTxt[0],opTxt[1]);
                for (int r = 0; r < nReplicas; r++) {
                    sendMessage(replicas[r], new RequestMessage(pid, new Command(pid, 0, op)));
                }
            }
        } catch (Exception e) {
            System.out.println("Error while reading the properties file for the Operation");
        }
    }

    public static void main(String[] args) {
        new Env().run(args);
    }
}
