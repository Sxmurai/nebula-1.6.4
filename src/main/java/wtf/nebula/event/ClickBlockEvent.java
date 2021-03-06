package wtf.nebula.event;

import me.bush.eventbus.event.Event;

public class ClickBlockEvent extends Event {
    private final int x, y, z, facing;

    public ClickBlockEvent(int x, int y, int z, int facing) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getFacing() {
        return facing;
    }

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
