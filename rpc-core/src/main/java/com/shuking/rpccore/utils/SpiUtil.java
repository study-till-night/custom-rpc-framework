package com.shuking.rpccore.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import com.shuking.rpccore.serializer.Serializer;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spi加载工具类
 */
@Log4j2
public class SpiUtil {

    // 系统默认spi目录
    private static final String SPI_CUSTOM_PATH = "META-INF/rpc/custom/";
    // 自定义spi目录
    private static final String SPI_SYS_PATH = "META-INF/rpc/system/";
    // spi需要扫描的路径
    private static final List<String> SCAN_DIR_LIST = Arrays.asList(SPI_CUSTOM_PATH, SPI_SYS_PATH);
    // spi需要加载的接口
    private static final List<Class<?>> SCAN_CLASS_LIST = Arrays.asList(Serializer.class);
    // K-- 要加载的接口全类名  V-- key与对应的实现类
    private static Map<String, Map<String, Class<?>>> loaderClassMap = new ConcurrentHashMap<>();
    // 缓存对象实例 K--要加载的接口全类名 v-- 实现类的对象实例
    private static Map<String, Object> instanceCacheMap = new ConcurrentHashMap<>();

    /**
     * 加载
     */
    public static void loadAllInterface() {
        log.info("开始spi所有接口---");
        for (Class<?> classType : SCAN_CLASS_LIST) {
            loadSingle(classType);
        }
        for (Map.Entry<String, Map<String, Class<?>>> stringMapEntry : loaderClassMap.entrySet()) {
            log.info("{},{}", stringMapEntry.getKey(), stringMapEntry.getValue());
        }
    }

    /**
     * 加载单个接口
     *
     * @param classType 接口类型
     */
    public static void loadSingle(Class<?> classType) {
        log.info("开始spi单个接口--{}", classType.toString());

        // 不同键值对应不同实现类
        HashMap<String, Class<?>> kvImplementionMap = new HashMap<>();
        for (String dir : SCAN_DIR_LIST) {
            // 获取所有spi文件
            List<URL> spiFiles = ResourceUtil.getResources(dir + classType.getName());
            for (URL spiFile : spiFiles) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(spiFile.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    // 读取每行配置
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] kvStrArray = line.split("=");
                        log.info("{},{}", kvStrArray[0], kvStrArray[1]);
                        if (kvStrArray.length == 2) {
                            // 将key对应实现类加入map
                            kvImplementionMap.put(kvStrArray[0], Class.forName(kvStrArray[1]));
                        }
                    }

                } catch (IOException | ClassNotFoundException e) {
                    log.error("读取spi配置文件出错:{}", e.getMessage());
                }
            }
            loaderClassMap.put(classType.getName(), kvImplementionMap);
        }
    }

    /**
     * 获取实例
     * @param classType 接口类型
     * @param key   实现类类型
     * @return  实现类实例
     * @param <T>
     */
    public static <T> T getInstance(Class<T> classType, String key) {
        // 根据传入的接口类型得到kv实现类的map
        String classTypeName = classType.getName();
        Map<String, Class<?>> kvImplementionMap = loaderClassMap.get(classTypeName);
        if (kvImplementionMap == null) {
            log.info("{} 尚未进行spi装配", classTypeName);
        }
        try {
            Class<?> implClass = kvImplementionMap.get(key);
            if (implClass == null) {
                log.info("spi中未包含{}对应的实现类", key);
            }

            T instance;
            // 如果缓存中有则直接取缓存 否则加入缓存
            if (instanceCacheMap.containsKey(classTypeName)) {
                instance = (T) instanceCacheMap.get(key);
            }
            else {
                instance = (T) (implClass.getConstructor().newInstance());
                instanceCacheMap.put(classTypeName, instance);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("实例化spi--{}--失败:{}", classTypeName, e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        loadAllInterface();
        System.out.println(getInstance(Serializer.class, "json").toString());
    }
}
