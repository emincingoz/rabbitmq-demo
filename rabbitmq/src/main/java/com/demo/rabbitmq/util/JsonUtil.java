package com.demo.rabbitmq.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dongliu.gson.GsonJava8TypeAdapterFactory;

public interface JsonUtil {

    static <T> T retrieveResourceFromJsonText(String data, Class<T> clazz) {
        Gson gson = (new GsonBuilder()).registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory()).create();
        return gson.fromJson(data, clazz);
    }

    static <T> T retrieveResourceFromJsonText(String data, Type type) {
        Gson gson = (new GsonBuilder()).registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory()).create();
        return gson.fromJson(data, type);
    }

    static String resourceToJsonText(Object obj) {
        Gson gson = (new GsonBuilder()).registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory()).create();
        return gson.toJson(obj);
    }
}
