package kiyobot.util;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for conveniently getting types by an Object's Class
 */
public enum ClassType {
    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    CHAR(Character.class),
    DOUBLE(Double.class),
    FLOAT(Float.class),
    INT(Integer.class),
    LONG(Long.class),
    SHORT(Short.class),
    STRING(String.class),
    JSON_OBJECT(JsonObject.class),
    JSON_PACKET(JsonPacket.class),
    BAD_TYPE(null);

    private static final Map<Class, ClassType> INSTANCE_BY_CLASS = new HashMap<>();

    static {
        Arrays.asList(ClassType.values())
              .forEach(instance -> INSTANCE_BY_CLASS.put(instance.getMyClass(), instance));
    }

    private final Class classType;

    ClassType(Class classType) {
        this.classType = classType;
    }

    public Class getMyClass() {
        return classType;
    }

    public static ClassType getByClass(Class classType) {
        return INSTANCE_BY_CLASS.getOrDefault(classType, BAD_TYPE);
    }
}
