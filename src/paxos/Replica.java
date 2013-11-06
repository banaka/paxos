package paxos;

import java.lang.*;
import java.util.*;
import java.util.logging.Level;

public class Replica extends Process {
    ProcessId[] leaders;
    int slot_num = 1;
    List<Account> accountList;
    Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
    Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();

    public Replica(Env env, ProcessId me, ProcessId[] leaders) {
        this.env = env;
        this.me = me;
        this.leaders = leaders;
        setLogger();
        loadProp();
        env.addProc(me, this);
    }

    Properties loadProp() {
        super.loadProp();
        String accountListStr = prop.getProperty("AccountsList");
        accountList = new ArrayList<Account>();
        String[] strArr = accountListStr.split(",");
        for (int i = 0; i < strArr.length; i++) {
            accountList.add(new Account(strArr[i], i));
        }
        return prop;
    }

    void propose(Command c) {
        if (!decisions.containsValue(c)) {
            for (int s = 1; ; s++) {
                if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
                    proposals.put(s, c);
                    for (ProcessId ldr : leaders) {
                        sendMessage(ldr, new ProposeMessage(me, s, c));
                    }
                    break;
                }
            }
        }
    }

    void perform(Command c) {
        //TODO:CHECK WHAT IS HAPPENING HERE.. SAME CMD SEEMS TO BE EXECUTING MULTIPLE TIMES..
        for (int s = 1; s < slot_num; s++) {
            if (c.equals(decisions.get(s))) {
                //TODO : The replica should be executing a given cmd only Once If a cmd has been executed
                //It should not be executed again.
                slot_num++;
                return;
            } else {
                //Act on the decisions uptil slot_no
                String[] operationArgs = c.op.operationArgs.split(Env.TX_MSG_SEPARATOR);
                try {
                    Account account = accountList.get(Integer.parseInt(operationArgs[0]));
                    int amt = Integer.parseInt(operationArgs[1]);
                    String output = "";
                    switch (c.op.opType) {
                        case Deposit:
                            account.debit(amt);
                            output = account.toString();
                            break;
                        case Inquiry:
                            account.getBalance();
                            output = account.toString();
                            break;
                        case Transfer:
                            Account toAccount = accountList.get(Integer.parseInt(operationArgs[2]));
                            account.transfer(toAccount, amt);
                            output = account.toString() + toAccount.toString();
                            break;
                        case Withdraw:
                            account.credit(amt);
                            output = account.toString();
                            break;
                        default:
                            output = "INVALID OPERATION TYPE";
                            break;
                    }

                } catch (Exception e) {
                  logger.log(Level.SEVERE,"Error in the input msg ");
                    //TODO : send error msg to the client
                }
                //TODO: Send msg to client with output

            }
        }
        logger.log(Level.CONFIG, "" + me + ": perform " + c);

        for (Account acc : accountList) {
            logger.log(Level.CONFIG, acc.toString());
        }
        slot_num++;
        //Need to send the response to client here
    }

    public void body() {
        logger.log(Level.CONFIG, "Here I am: " + me);
        for (; ; ) {
            PaxosMessage msg = getNextMessage();

            if (msg instanceof RequestMessage) {
                RequestMessage m = (RequestMessage) msg;
                propose(m.command);
            } else if (msg instanceof DecisionMessage) {
                DecisionMessage m = (DecisionMessage) msg;
                decisions.put(m.slot_number, m.command);
                for (; ; ) {
                    Command c = decisions.get(slot_num);
                    if (c == null) {
                        break;
                    }
                    Command c2 = proposals.get(slot_num);
                    if (c2 != null && !c2.equals(c)) {
                        propose(c2);
                    }
                    perform(c);
                }
            } else {
                logger.log(Level.SEVERE, "paxos.Replica: unknown msg type");
            }
        }
    }
}
