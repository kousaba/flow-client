package net.flowclient.event.impl;

import net.flowclient.event.Event;

public class ChatReceiveEvent extends Event {
    public final String message;
    public ChatReceiveEvent(String message){
        this.message = message;
    }
}
