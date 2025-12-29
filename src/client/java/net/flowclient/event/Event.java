package net.flowclient.event;

public abstract class Event {
    private boolean cancelled = false;

    // イベントをキャンセルする
    public void cancel(){
        this.cancelled = true;
    }

    public boolean isCancelled(){
        return cancelled;
    }
}
