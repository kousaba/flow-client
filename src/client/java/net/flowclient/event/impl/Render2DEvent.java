package net.flowclient.event.impl;

import net.flowclient.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class Render2DEvent extends Event {
    private final DrawContext context;
    private final RenderTickCounter tickDelta;
    public Render2DEvent(DrawContext context, RenderTickCounter tickDelta){
        this.context = context;
        this.tickDelta = tickDelta;
    }
    public DrawContext getContext() {return context;}
    public RenderTickCounter getTickDelta() {return tickDelta;}
}
