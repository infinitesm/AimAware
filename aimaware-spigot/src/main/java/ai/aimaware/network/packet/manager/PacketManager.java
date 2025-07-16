package ai.aimaware.network.packet.manager;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import ai.aimaware.network.packet.wrapper.inbound.*;
import ai.aimaware.network.packet.wrapper.outbound.*;
import ai.aimaware.profile.model.Profile;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class PacketManager {
    protected final Profile profile;
    protected final EntityPlayer nmsPlayer;
    protected final ExecutorService executor;
    protected final List<Consumer<WrappedPacket>> listeners = new ArrayList<>();

    private final Map<Class<? extends Packet<?>>, Function<Packet<?>, WrappedPacket>> outboundHandlers = new HashMap<>();

    public PacketManager(Profile profile) {
        this.profile = profile;
        this.nmsPlayer = ((CraftPlayer) profile.getPlayer()).getHandle();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void registerHandlers() {
        // Transaction packets
        register(PacketPlayOutTransaction.class, SPacketTransaction::new);

        // Entity packets
        register(PacketPlayOutEntity.class, SPacketEntity::new);
        register(PacketPlayOutEntityTeleport.class, SPacketEntityTeleport::new);
        register(PacketPlayOutNamedEntitySpawn.class, SPacketSpawnPlayer::new);
        register(PacketPlayOutEntityVelocity.class, SPacketEntityVelocity::new);
        register(PacketPlayOutEntityDestroy.class, SPacketEntityDestroy::new);
        register(PacketPlayOutSpawnEntityLiving.class, SPacketSpawnLivingEntity::new);

        // Player state and position
        register(PacketPlayOutPosition.class, SPacketPosition::new);
        register(PacketPlayOutHeldItemSlot.class, SPacketHeldItemSlot::new);
        register(PacketPlayOutAbilities.class, SPacketAbilities::new);

        // Effects
        register(PacketPlayOutEntityEffect.class, SPacketEntityEffect::new);
        register(PacketPlayOutRemoveEntityEffect.class, SPacketRemoveEntityEffect::new);

        // Misc
        register(PacketPlayOutKeepAlive.class, SPacketKeepAlive::new);
    }


    public void start() {
        executor.execute(() -> new ModifiedPlayerConnection(nmsPlayer.server,
                nmsPlayer.playerConnection.networkManager,
                nmsPlayer,
                this));
    }

    public void sendTransaction(int windowId, short actionNumber, boolean accepted) {
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutTransaction(windowId, actionNumber, accepted));
    }

    public void addListener(Consumer<WrappedPacket> listener) {
        listeners.add(listener);
    }

    private void handle(WrappedPacket packet) {
        listeners.forEach(listener -> listener.accept(packet));
    }

    private static class ModifiedPlayerConnection extends PlayerConnection {

        private final PacketManager packetManager;

        public ModifiedPlayerConnection(MinecraftServer server,
                                        NetworkManager networkManager,
                                        EntityPlayer nmsPlayer,
                                        PacketManager packetManager) {

            super(server, networkManager, nmsPlayer);
            this.packetManager = packetManager;
        }

        @Override
        public void a(PacketPlayInSteerVehicle packet) {
            super.a(packet);
            packetManager.handle(new CPacketInput(packet));
        }

        @Override
        public void a(PacketPlayInBlockDig packet) {
            super.a(packet);
            packetManager.handle(new CPacketBlockDig(packet));
        }

        @Override
        public void a(PacketPlayInBlockPlace packet) {
            super.a(packet);
            packetManager.handle(new CPacketBlockPlace(packet));
        }

        @Override
        public void a(PacketPlayInHeldItemSlot packet) {
            super.a(packet);
            packetManager.handle(new CPacketHeldItemSlot(packet));
        }

        @Override
        public void a(PacketPlayInEntityAction packet) {
            super.a(packet);
            packetManager.handle(new CPacketEntityAction(packet));
        }

        @Override
        public void a(PacketPlayInUseEntity packet) {
            super.a(packet);
            packetManager.handle(new CPacketUseEntity(packet));
        }

        @Override
        public void a(PacketPlayInTransaction packet) {
            super.a(packet);
            packetManager.handle(new CPacketTransaction(packet));
        }

        @Override
        public void a(PacketPlayInKeepAlive packet) {
            super.a(packet);
            packetManager.handle(new CPacketKeepAlive(packet));
        }

        @Override
        public void a(PacketPlayInAbilities packet) {
            super.a(packet);
            packetManager.handle(new CPacketAbilities(packet));
        }

        @Override
        public void a(PacketPlayInChat packet) {
            super.a(packet);
            packetManager.handle(new CPacketChat(packet));
        }

        @Override
        public void a(PacketPlayInFlying packet) {
            super.a(packet);
            packetManager.handle(new CPacketFlying(packet));
        }

        @Override
        public void a(PacketPlayInArmAnimation packet) {
            super.a(packet);
            packetManager.handle(new CPacketAnimation(packet));
        }

        @Override
        public void sendPacket(Packet packet) {
            super.sendPacket(packet);

            Function<Packet<?>, WrappedPacket> handler = packetManager.getOutboundHandlers().get(packet.getClass());

            if (handler != null) {
                packetManager.handle(handler.apply(packet));
            }
        }
    }

    private <T extends Packet<?>> void register(Class<T> clazz, Function<T, WrappedPacket> handler) {
        Function<Packet<?>, WrappedPacket> bridge = packet -> {
            if (clazz.isInstance(packet)) {
                return handler.apply(clazz.cast(packet));
            } else {
                throw new IllegalArgumentException("Handler for " + clazz.getName() + " cannot handle packet of type " + packet.getClass().getName());
            }
        };

        outboundHandlers.put(clazz, bridge);
    }


}
