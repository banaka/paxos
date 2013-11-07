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
    int delay;
    public boolean assign_stop_request = false;
    public Level messageLevel;

    public boolean stop_request() {
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return assign_stop_request;
    }

    abstract void body();

    public void run() {
        body();
        env.removeProc(me);
    }

    Properties loadProp() {
        try {
            prop.load(new FileInputStream("config.properties"));
            if (prop.getProperty(me.name) != null) {
                delay = Integer.parseInt(prop.getProperty(me.name));
            } else {
                delay = Integer.parseInt(prop.getProperty("delay"));
            }
            messageLevel = prop.getProperty("printMessages").equals("TRUE") ? Level.CONFIG : Level.FINER;

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return prop;
    }

    PaxosMessage getNextMessage() {
        return inbox.bdequeue();
    }

    void sendMessage(ProcessId dst, PaxosMessage msg) {
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.logger.log(messageLevel, "Sent Msg" + msg + " to " + dst + " from " + me);
        env.sendMessage(dst, msg);
    }

    void deliver(PaxosMessage msg) {
        inbox.enqueue(msg);
        this.logger.log(messageLevel, me + " Got msg " + msg + " from " + msg.src);
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
            fileHandler.setLevel(Level.FINER);
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
