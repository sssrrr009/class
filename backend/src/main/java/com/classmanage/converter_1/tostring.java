package com.classmanage.converter_1;

import com.alibaba.excel.converters.Converter;
import com.classmanage.security.Role;

public class tostring implements Converter<String, Role>{

    @Override
    public Role convert(String source) {

        Role[] values = Role.values();
        for (Role value : values) {
            if (value.toString().equals(source)) {
                return value;
            }
        }
        throw new IllegalArgumentException("code:"+source+"非法");


    }

}
