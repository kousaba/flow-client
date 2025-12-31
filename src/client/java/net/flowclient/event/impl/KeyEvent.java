package net.flowclient.event.impl;

import net.flowclient.event.Event;

public class KeyEvent extends Event {
    private final int key;
    public KeyEvent(int key) {this.key = key;}
    public int getKey() {return key;}
}
