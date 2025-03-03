package com.coder.mall.user.custom.converter;

import com.coder.mall.user.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new Converter<String, T>() {
            @Override
            public T convert(String code) {
                T[] enumConstants = targetType.getEnumConstants();
                for (T eumConstant : enumConstants) {
                    if (eumConstant.getCode().toString().equals(code)) {
                        return eumConstant;
                    }
                }
                throw new IllegalArgumentException();
            }
        };
    }
}
