package com.shuking.rpccore.serializer;

public enum SerializerEnum {
    JDK("jdk", new JdkSerializer()),
    JSON("json", new JsonSerializer()),
    KRYO("kyro", new KryoSerializer()),
    HESSIAN("hessian", new HessianSerializer());

    public final String serializerName;

    public final Serializer serializerClass;

    SerializerEnum(String serializerName, Serializer serializerClass) {
        this.serializerName = serializerName;
        this.serializerClass = serializerClass;
    }

    // 根据传入的key返回相应的序列化器
    public static Serializer getSerializerByKey(String serializerName) {
        for (SerializerEnum serializerEnum : SerializerEnum.values()) {
            if (serializerEnum.serializerName.equals(serializerName)) {
                return serializerEnum.serializerClass;
            }
        }
        // 默认返回jdk
        return JDK.serializerClass;
    }
}
