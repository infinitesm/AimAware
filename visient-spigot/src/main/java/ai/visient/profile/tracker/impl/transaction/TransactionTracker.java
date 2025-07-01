package ai.visient.profile.tracker.impl.transaction;

import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.handler.PacketHandler;
import ai.visient.network.packet.wrapper.base.WrappedPacket;
import ai.visient.network.packet.wrapper.inbound.CPacketTransaction;
import ai.visient.network.packet.wrapper.outbound.SPacketTransaction;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TransactionTracker extends Tracker implements PacketHandler {

    private final Map<Short, Long> transactionTimestamps = new HashMap<>();
    private final Map<Short, Runnable> pendingActions = new HashMap<>();

    private long ping;
    private short currentTransactionId;

    public TransactionTracker(Profile profile) {
        super(profile);
    }

    @Override
    public void process(WrappedPacket packet) {
        if (packet instanceof CPacketTransaction) {
            CPacketTransaction wrapper = (CPacketTransaction) packet;
            handleClientTransaction(wrapper);
        }
        else if (packet instanceof SPacketTransaction) {
            SPacketTransaction wrapper = (SPacketTransaction) packet;
            handleServerTransaction(wrapper);
        }
    }

    private void handleClientTransaction(CPacketTransaction wrapper) {
        short id = wrapper.getActionNumber();

        if (transactionTimestamps.containsKey(id)) {
            long sendTime = transactionTimestamps.remove(id);
            ping = wrapper.getTime() - sendTime;

            Runnable action = pendingActions.remove(id);
            if (action != null) {
                action.run();
            }
        }
    }

    private void handleServerTransaction(SPacketTransaction wrapper) {
        transactionTimestamps.put(wrapper.getActionNumber(), wrapper.getTime());
    }

    /**
     * Queues an action to run once the transaction is confirmed by the client.
     *
     * @param action Runnable task to execute after confirmation.
     */
    public void confirm(Runnable action) {
        profile.getPacketManager().sendTransaction(
                /* windowId */ 0,
                currentTransactionId,
                false
        );
        pendingActions.put(currentTransactionId, action);

        incrementTransactionId();
    }

    private void incrementTransactionId() {
        currentTransactionId++;
        if (currentTransactionId == Short.MIN_VALUE) {
            currentTransactionId = 0;
        }
    }
}
