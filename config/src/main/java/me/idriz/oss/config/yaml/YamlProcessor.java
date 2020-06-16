package me.idriz.oss.config.yaml;

import me.idriz.oss.config.Config;
import me.idriz.oss.config.adapter.ConfigAdapter;
import me.idriz.oss.config.annotation.Adapter;
import me.idriz.oss.config.annotation.Section;
import me.idriz.oss.config.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class YamlProcessor {

    private static final Map<Class<?>, List<Field>> FIELDS = new HashMap<>();
    private static final Map<String, Class<?>> CLASS_BY_NAME_MAP = new HashMap<>();

    private static final Class<?>[] PRIMITIVE_TYPES = {

            //General Types
            String.class,

            //Boxed Types
            Integer.class,
            Double.class,
            Float.class,
            Short.class,
            Boolean.class,
            Byte.class,
            Long.class,

            //Unboxed types
            int.class,
            double.class,
            float.class,
            short.class,
            boolean.class,
            byte.class,
            long.class,


    };

    private static boolean isConfigPrimitive(Class<?> type) {
        for (Class<?> clazz : PRIMITIVE_TYPES) {
            if (type.equals(clazz)) return true;
        }
        return false;
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        if (clazz.getDeclaredClasses().length > 0) {
            for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
                if (!declaredClass.isAnnotationPresent(Section.class)) continue;

                if (declaredClass.getDeclaredClasses().length > 0) fields.addAll(getFields(declaredClass));
                for (Field field : declaredClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Value.class)) {
                        fields.add(field);
                    }
                }
            }
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    static <T> void initializeHook(Config config, T object) throws IllegalAccessException {

        List<Field> fields = FIELDS.getOrDefault(
                object.getClass(),
                getFields(object.getClass())
        );

        AtomicBoolean addedAnyFields = new AtomicBoolean(false);

        for (Field field : fields) {

            Object parent = object;
            String key = field.getAnnotation(Value.class).value();

            Class<?> lastDeclaredClass = field.getDeclaringClass();

            while (lastDeclaredClass.isAnnotationPresent(Section.class)) {
                key = lastDeclaredClass.getAnnotation(Section.class).value().concat(".").concat(key);
                lastDeclaredClass = lastDeclaredClass.getDeclaringClass();
            }

            if (!lastDeclaredClass.equals(object.getClass())) {
                try {
                    parent = lastDeclaredClass.getConstructors()[0].newInstance();
                } catch (InstantiationException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Sectioned class MUST have an empty constructor.");
                }
            }
            field.setAccessible(true);

            AtomicReference<Object> value = new AtomicReference<>(config.get(key));
            Object defaultValue = field.get(parent);

            if (isConfigPrimitive(field.getType()) && !field.isAnnotationPresent(Adapter.class)) {
                if (value.get() == null) {
                    if (defaultValue == null) continue;
                    config.set(key, defaultValue);
                    addedAnyFields.set(true);
                    continue;
                }
                field.set(object, value.get());
                continue;
            }
            ConfigAdapter configAdapter = null;
            if (field.isAnnotationPresent(Adapter.class)) {
                Adapter adapter = field.getAnnotation(Adapter.class);
                configAdapter = Config.getAdapterByAdapterClass(adapter.value());
            }

            String finalKey = key;

            if (List.class.isAssignableFrom(field.getType())) {
                Class<?> genericType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (configAdapter == null) {
                    configAdapter = Config.getAdapter(genericType);
                    if (configAdapter == null && !isConfigPrimitive(genericType)) {
                        throw new NullPointerException("Couldn't find config adapter for class " + field.getType().getSimpleName());
                    }
                }

                if (isConfigPrimitive(genericType) && configAdapter == null) {
                    value.set(config.getList(finalKey));
                    if (value.get() == null) {
                        value.set(defaultValue);
                        config.set(finalKey, defaultValue);
                        addedAnyFields.set(true);
                    }
                    field.set(object, value.get());
                    continue;
                }

                ConfigAdapter finalConfigAdapter = configAdapter;

                configAdapter.readList(config, key, list -> {
                    try {
                        if (list == null) {
                            list = defaultValue;
                            finalConfigAdapter.writeList(config, finalKey, (List) defaultValue);
                            addedAnyFields.set(true);
                        }
                        field.set(object, list);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

                continue;
            }

            if (configAdapter == null) {
                configAdapter = Config.getAdapter(field.getType());
                if(configAdapter == null) {
                    throw new NullPointerException("Couldn't find config adapter for class " + field.getType().getSimpleName());
                }
            }

            ConfigAdapter finalConfigAdapter = configAdapter;
            configAdapter.read(config, key, result -> {
                if (result == null) {
                    result = defaultValue;
                    finalConfigAdapter.write(config, finalKey, defaultValue);
                    addedAnyFields.set(true);
                }
                try {
                    field.set(object, result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

        }

        if (addedAnyFields.get()) config.save();
    }


}
