package org.daisleyharrison.security.samples.spring.microservices.shared.utilities;

import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumConverter<T extends Enum<T>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumConverter.class);
    private Class<T> enumType;

    public EnumConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    public T valueOf(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        for (Field field : enumType.getFields()) {
            if (field.isEnumConstant()) {
                if (text.equals(field.getName())) {
                    return Enum.valueOf(enumType, text);
                }
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null) {
                    if (text.equals(jsonProperty.value())) {
                        return Enum.valueOf(enumType, field.getName());
                    }
                }
            }
        }
        LOGGER.warn("Failed to convert \"{}\" to enum {} value", text, enumType.getName());
        return null;
    }
}
