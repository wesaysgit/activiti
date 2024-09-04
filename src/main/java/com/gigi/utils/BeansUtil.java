package com.gigi.utils;

import com.alibaba.fastjson2.JSON;
import com.gigi.pojo.PetDTO;
import com.gigi.pojo.User;
import com.gigi.pojo.UserDTO;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class BeansUtil {

    public static void main(String[] args) {
        PetDTO cat = PetDTO.builder().id(1).name("cat").build();
        PetDTO dog = PetDTO.builder().id(2).name("dog").build();

        User lili = User.builder().id(1L).name("lili").petDTOS(Arrays.asList(cat, dog)).build();
//        User apatow = User.builder().id(2L).name("apatow").build();
//        List<User> users = Arrays.asList(lili, apatow);
//        List<UserDTO> userDTOS = BeansUtil.convert(users, it -> UserDTO.builder().id(it.getId()).name(it.getName()).build());
//        System.out.println(userDTOS);
//        List<UserDTO> dtoList = BeansUtil.filter(userDTOS, it -> it.getId() > 1);
//        System.out.println(dtoList);

        UserDTO userDTO = UserDTO.builder().build();
        UserDTO target = copyFields(lili, userDTO);
        log.info("target >>> {}", JSON.toJSONString(target));
    }

    public static <T, R> R copyFields(T source, R target) {
        try {
            Field[] sourceFields = source.getClass().getDeclaredFields();
            Field[] targetFields = target.getClass().getDeclaredFields();

            Map<String, Field> sourceFMap = new HashMap<>();
            for (Field sourceField : sourceFields) {
                sourceField.setAccessible(true);
                sourceFMap.put(sourceField.getName(), sourceField);
            }
            for (Field targetField : targetFields) {
                String targetFieldName = targetField.getName();
                Field sourceField = sourceFMap.get(targetFieldName);
                if (sourceField == null) {
                    targetField.setAccessible(true);
                    targetField.set(target, null);
                    continue;
                }
                targetField.setAccessible(true);
                Object sourceV = sourceField.get(source);
                if (sourceV instanceof List<?> sourceList) {
                    List<Object> targetList = sourceList.stream().map(BeansUtil::copyChildBean).toList();
                    targetField.set(target, targetList);
                } else {
                    targetField.set(target, sourceV);
                }
            }
        } catch (Exception e) {
            log.error("copyFields error...", e);
        }
        return target;
    }

    /**
     * 对象类型一致
     * @param source
     * @return
     * @param <T>
     */
    private static <T, R> R copyChildBean(T source) {
        try {
            @SuppressWarnings("unchecked")
            R target = (R) source.getClass().getDeclaredConstructor().newInstance();
            copyFields(source, target);
            return target;
        } catch (Exception e) {
            log.error("copyChildBean error...", e);
        }
        return null;
    }

    public static <T, R> List<R> convert(List<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

}
