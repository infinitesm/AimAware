package ai.visient.profile.tracker.impl;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.Tracker;
import ai.visient.profile.tracker.handler.PacketProcessor;
import ai.visient.packet.wrapper.WrappedPacket;
import ai.visient.packet.wrapper.inbound.CPacketTransaction;
import ai.visient.packet.wrapper.outbound.SPacketTransaction;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PingTracker extends Tracker implements PacketProcessor {

    public PingTracker(Profile profile) {
        super(profile);
    }

    private final Map<Short, Long> transactionMap = new HashMap<>();
    private final Map<Short, Runnable> actionMap = new HashMap<>();
    private long ping;
    private short tick;

    @Override
    public void process(WrappedPacket packet) {
        if (packet instanceof CPacketTransaction) {
            CPacketTransaction wrapper = (CPacketTransaction) packet;
            short id = wrapper.getActionNumber();

            if (transactionMap.containsKey(id)) {
                // Real time ping calculation.
                ping = packet.getTime() - transactionMap.remove(id);
                actionMap.remove(id).run();
            }
        } else if (packet instanceof SPacketTransaction) {
            SPacketTransaction wrapper = (SPacketTransaction) packet;
            transactionMap.put(wrapper.getActionNumber(), wrapper.getTime());
        }
    }

    public void confirm(Runnable runnable) {
        // Sending hundreds of transactions per player per tick? Sounds like a fantastic idea.
        profile.getPacketManager().sendTransaction(0, tick, false);
        actionMap.put(tick, runnable);
        if (--tick == Short.MIN_VALUE) tick = 0;
    }
}
