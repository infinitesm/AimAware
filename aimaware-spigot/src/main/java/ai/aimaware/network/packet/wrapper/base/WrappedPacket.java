package ai.aimaware.network.packet.wrapper.base;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class WrappedPacket {

    @Getter
    private final long time = System.currentTimeMillis();

    private final Map<String, Field> fields = new HashMap<>();
    private final Object instance;

    public WrappedPacket(Object instance, Class<?> klass) {
        this.instance = instance;

        for (Field field : klass.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getField(String name) {
        Field field = fields.get(name);
        if (field == null) {
            System.err.println("Field not found: " + name);
            return null;
        }

        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
