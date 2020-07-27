package com.dwj.rpc.common.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具类（基于 Protostuff 实现）
 *
 */
public class SerializationUtil {

    // 这是个缓存， 这个模板是固定的，都需要这样用。
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    // Objenesis 可以绕过java的构造函数进行初始化对象，
    private static Objenesis objenesis = new ObjenesisStd(true); // 根据类进行实例化对象。

    private SerializationUtil() {
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass(); // 获取对象的类。
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            // 反序列化的主要操作。
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // Schema 相当于定义了 序列化后的数据的模板即框架，内部数据的结构，和如何进行解析和翻译。
    // 那这个Schema是什么呢？就是一个组织结构，就好比是数据库中的表、视图等等这样的组织机构，在这里表示的就是序列化对象的结构。
    // 根据序列化对象获取其组织结构Schema
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        // 因为每一个cls都会创建一个泛型组织结构，所以使用一个HashMap作为缓存，可以提高效率。
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            // 如果为空则加入。
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }
}
