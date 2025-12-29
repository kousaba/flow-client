package net.flowclient.event;

public abstract class Event {
    private boolean cancelled = false;

    // キャンセル可能なイベントか
    public boolean isCancellable(){
        return false;
    }

    // イベントをキャンセルする
    public void setCancelled(boolean cancelled) {
        if (isCancellable()) {
            this.cancelled = cancelled;
        }
    }

    public boolean isCancelled(){
        return cancelled;
    }
}
