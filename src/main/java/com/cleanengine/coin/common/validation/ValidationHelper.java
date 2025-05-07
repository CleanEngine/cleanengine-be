package com.cleanengine.coin.common.validation;

import java.util.Arrays;
import java.util.List;

public final class ValidationHelper {
    private ValidationHelper(){}
    public static String extractFieldNameFromPropertyPathStr(String propertyPath){
        List<String> chunks = Arrays.asList(propertyPath.split("\\."));
        int lastIndex = chunks.size()-1;
        return lastIndex >=0? chunks.get(lastIndex) : "None";
    }
}
