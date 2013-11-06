package paxos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Env {
    Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
    static String TX_MSG_SEPARATOR = "\\$";

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
                acceptors[i] = new ProcessId("acceptor" + i);
                Acceptor acc = new Acceptor(this, acceptors[i]);
            }
            for (int i = 0; i < nReplicas; i++) {
                replicas[i] = new ProcessId("replica" + i);
                Replica repl = new Replica(this, replicas[i], leaders);
            }
            for (int i = 0; i < nLeaders; i++) {
                leaders[i] = new ProcessId("leader" + i);
                Leader leader = new Leader(this, leaders[i], acceptors, replicas);
            }

            for (int i = 1; i <= nRequests; i++) {
                ProcessId pid = new ProcessId("client" + i);
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

    public static void main(String[] args) throws Exception {
        Env e = new Env();
        e.run(args);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("$ Enter new Command KILL|SHOW|HELP > ");
            String input = br.readLine();
            e.operateOn(input);
        }
    }

    private void operateOn(String input) {
        String[] arr = input.split(" ");
        String inputCommand = arr[0];
        Commands c = null;
        try {
        c = Commands.valueOf(inputCommand);
        } catch(IllegalArgumentException e) {
            System.err.println("Unknown Command!");
            return;
        }
        switch(c){
            case KILL:
                String pidToKill = arr[1];
                for(ProcessId p : procs.keySet()){
                    if(p.toString().equals(pidToKill)){
                        procs.get(p).stop_request = true;
                        removeProc(p);
                        System.out.println("Killed "+pidToKill);
                        return;
                    }
                }
                System.out.println("Could not find such process...type SHOW for live processes");
                break;
            case SHOW:
                for(ProcessId p : procs.keySet()){
                    System.out.print(p + " | ");
                }
                System.out.println();
                break;
            case HELP:
                for(Commands cc : Commands.values()){
                    System.out.println(cc + " -- " + cc.getDescription());
                }
                break;
            default:
                System.err.println("UnImplemented Command!");
        }
    }
}
