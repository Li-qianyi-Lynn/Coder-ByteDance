package com.coder.mall.user.custom.converter;

import com.coder.mall.user.enums.UserType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserTypeConverter implements Converter<String, UserType> {
    @Override
    public UserType convert(String source) {
        for (UserType userType : UserType.values()) {
            if (userType.getName().equals(source)) {
                return userType.valueOf(source);
            }
        }
        throw new IllegalArgumentException();
    }
}
