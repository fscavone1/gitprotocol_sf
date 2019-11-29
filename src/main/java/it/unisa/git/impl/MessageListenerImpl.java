package it.unisa.git.impl;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class MessageListenerImpl implements MessageListener {
    int peerid;

    public MessageListenerImpl(int peerid) {
        this.peerid = peerid;

    }

    public Object parseMessage(Object obj) {
        TextIO txtIO = TextIoFactory.getTextIO();
        TextTerminal terminal = txtIO.getTextTerminal();
        terminal.printf("\n" + peerid + "] (Direct Message Received) " + obj + "\n\n");
        return "success";
    }

}
