package com.coder.framework.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class JsonUtils {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** SpringMVC 中自带一个 ObjectMapper，直接读取配置好的 ObjectMapper */
    public static void init(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    /**
     * 将对象转为 Json 字符串
     * @param obj 传入对象
     * @return Json 字符串
     */
    @SneakyThrows
    public static String toJsonString(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param jsonStr
     * @param clazz
     * @return
     * @param <T>
     */
    @SneakyThrows
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }

        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }
}
