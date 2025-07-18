package ai.aimaware.profile.tracker.impl.transaction;

import ai.aimaware.profile.model.Profile;
import ai.aimaware.profile.tracker.base.Tracker;
import ai.aimaware.profile.tracker.handler.PacketHandler;
import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import ai.aimaware.network.packet.wrapper.inbound.CPacketTransaction;
import ai.aimaware.network.packet.wrapper.outbound.SPacketTransaction;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TransactionTracker extends Tracker implements PacketHandler {

    // Transactional data holding maps
    private final Map<Short, Long> transactionTimestamps = new HashMap<>();
    private final Map<Short, Runnable> pendingActions = new HashMap<>();

    private long ping;
    private short currentTransactionId;

    public TransactionTracker(Profile profile) {
        super(profile);
    }

    /**
     * Packet router for transaction tracker
     * @param packet NMS packet wrapper
     */
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

    /**
     * Calculate ping and execute any queued transaction-related actions
     * @param wrapper NMS packet wrapper
     */
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

    /**
     * Increment the transaction ID.
     * TODO: You usually want to limit to a range to avoid clashing with other plugins.
     */
    private void incrementTransactionId() {
        currentTransactionId++;
        if (currentTransactionId == Short.MIN_VALUE) {
            currentTransactionId = 0;
        }
    }
}
