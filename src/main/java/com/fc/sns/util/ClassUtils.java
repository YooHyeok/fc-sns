package com.fc.sns.util;

import java.util.Optional;

/**
 * 안전하게 캐스팅 하기 위한 클래스
 * 동적으로 타입을 변환시킨다.
 */
public class ClassUtils {
    public static <T> Optional<T> getSafeCastInstance(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? Optional.of(clazz.cast(o)) : Optional.empty();
    }
}
