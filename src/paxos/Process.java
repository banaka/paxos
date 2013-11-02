package paxos;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

public abstract class Process extends Thread {

    Logger logger;
    ProcessId me;
    Queue<PaxosMessage> inbox = new Queue<PaxosMessage>();
    Env env;
    Properties prop = new Properties();


    abstract void body();

    public void run() {
        body();
        env.removeProc(me);
    }

    Properties getProp()throws IOException{
        prop.load(new FileInputStream("config.properties"));
        return prop;
    }

    PaxosMessage getNextMessage() {
        return inbox.bdequeue();
    }

    void sendMessage(ProcessId dst, PaxosMessage msg) {
        env.sendMessage(dst, msg);
        this.logger.log(Level.CONFIG, "Sent Msg" + msg + " to " + dst);
    }

    void deliver(PaxosMessage msg) {
        inbox.enqueue(msg);
        this.logger.log(Level.CONFIG, "Got msg " + msg);
    }

    public void setLogger() {
        logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINER);
        Handler consoleHandler = null;
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            logger.addHandler(consoleHandler);
        }
        consoleHandler.setLevel(Level.CONFIG);

        try {
            FileHandler fileHandler = new FileHandler("log/Log" + me + ".log", true);
            fileHandler.setLevel(Level.CONFIG);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
