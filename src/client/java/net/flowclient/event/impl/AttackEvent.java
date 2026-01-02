package net.flowclient.event.impl;

import net.flowclient.event.Event;

import javax.swing.text.html.parser.Entity;

public class AttackEvent extends Event {
    private final Entity target;
    public AttackEvent(Entity target){
        this.target = target;
    }
    public Entity getTarget(){
        return target;
    }
}
