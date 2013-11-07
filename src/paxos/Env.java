package paxos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Env {
    Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
    static final String TX_MSG_SEPARATOR = "\\$";
    static final String CLIENT_MSG_SEPARATOR = ":";
    static final String BODY_MSG_SEPERATOR = " ";
    ProcessId pid = new ProcessId("Main");
    public static List<Replica> replicas;
    public static List<Leader> leaders;
    Map<String, Client> clnts;

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
//            int nRequests = Integer.parseInt(prop.getProperty("nRequests"));

            ProcessId[] accepts = new ProcessId[nAcceptors];
            ProcessId[] repls = new ProcessId[nReplicas];
            ProcessId[] leads = new ProcessId[nLeaders];

            clnts = new HashMap<String, Client>();

            for (int i = 0; i < nAcceptors; i++) {
                accepts[i] = new ProcessId("acceptor" + i);
                Acceptor acc = new Acceptor(this, accepts[i]);
            }

            replicas = new ArrayList<Replica>();
            for (int i = 0; i < nReplicas; i++) {
                repls[i] = new ProcessId("replica" + i);
                replicas.add(new Replica(this, repls[i], leads));
            }

            leaders = new ArrayList<Leader>();
            for (int i = 0; i < nLeaders; i++) {
                leads[i] = new ProcessId("leader" + i);
                leaders.add(new Leader(this, leads[i], accepts, repls));
            }

//            for (int i = 1; i <= nRequests; i++) {
//                ProcessId pid = new ProcessId("client" + i);
//                String[] opTxt = prop.getProperty("Operation" + i).split(TX_MSG_SEPARATOR, 2);
//                Operation op = new Operation(opTxt[0], opTxt[1]);
//                for (int r = 0; r < nReplicas; r++) {
//                    sendMessage(repls[r], new RequestMessage(pid, new Command(pid, 0, op)));
//                }
//            }
        } catch (Exception e) {
            System.out.println("Error while reading the properties file for the Operation");
        }
    }

    public static void main(String[] args) throws Exception {
        Env e = new Env();
        e.run(args);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("$ Enter new Command KILL|SHOW|TX|HELP > ");
            String input = br.readLine();
            e.operateOn(input);
        }
    }

    private void operateOn(String input) {
        String[] arr = input.split(BODY_MSG_SEPERATOR,2);
        String inputCommand = arr[0];
        Commands c = null;
        try {
            c = Commands.valueOf(inputCommand);
        } catch (IllegalArgumentException e) {
            if(!("".equals(inputCommand)))
                System.err.println("Unknown Command! "+inputCommand);
            return;
        }
        switch (c) {
            case KILL:
                String pidToKill = arr[1];
                for(ProcessId p : procs.keySet()){
                    if(p.toString().equals(pidToKill)){
                        procs.get(p).assign_stop_request = true;
                        removeProc(p);
                        System.out.println("Killed " + pidToKill);
                        return;
                    }
                }
                System.out.println("Could not find such process...type SHOW for live processes");
                break;
            case SHOW:
                for (ProcessId p : procs.keySet()) {
                    System.out.print(p + " | ");
                }
                System.out.println();
                break;
            case HELP:
                for (Commands cc : Commands.values()) {
                    System.out.println(cc + " -- " + cc.getDescription());
                }
                break;
            case TX:
                String[] bodySplit = arr[1].split(BODY_MSG_SEPERATOR,2); //[0]=1:Withdraw$0$20 [1]=replica1:3000
                ProcessId delayReplica=null;
                int delayReplicaTime=0;
                if(bodySplit.length > 1){
                    String[] optionalSplit = bodySplit[1].split(CLIENT_MSG_SEPARATOR);
                    for(Replica r: replicas){
                        if(r.me.toString().equals(optionalSplit[0]))
                            delayReplica = r.me;
                    }
                    delayReplicaTime = Integer.parseInt(optionalSplit[1]);
                }
                String clientArr[] = bodySplit[0].split(CLIENT_MSG_SEPARATOR);
                String clientName = "client"+clientArr[0];
                if(!(clnts.containsKey(clientName)))
                    clnts.put(clientName, new Client(this, new ProcessId(clientName)));
                Client client = clnts.get(clientName);
                String[] opTxt = clientArr[1].split(TX_MSG_SEPARATOR, 2);
                Operation op = new Operation(opTxt[0], opTxt[1]);
                sendMessage(client.me, new TxMessage(pid, new Command(pid, 0, op),delayReplica,delayReplicaTime));
                break;
            default:
                System.err.println("UnImplemented Command!");
        }
    }
}
