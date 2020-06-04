package me.idriz.oss.config.yaml;

import me.idriz.oss.config.Config;
import me.idriz.oss.config.adapter.ConfigAdapter;
import me.idriz.oss.config.annotation.Adapter;
import me.idriz.oss.config.annotation.Section;
import me.idriz.oss.config.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

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

            //Collection Types
            List.class,


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

        boolean addedAnyFields = false;

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

            Object value = config.get(key);
            Object defaultValue = field.get(parent);

            if (isConfigPrimitive(field.getType()) && !field.isAnnotationPresent(Adapter.class)) {
                if (value == null) {
                    if (defaultValue == null) continue;
                    config.set(key, defaultValue);
                    addedAnyFields = true;
                    continue;
                }
                field.set(object, value);
                continue;
            }

            if (List.class.isAssignableFrom(field.getType())) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                String typeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
                try {

                    Class<?> clazz = CLASS_BY_NAME_MAP.getOrDefault(
                            typeName,
                            Class.forName(typeName)
                    );

                    if (isConfigPrimitive(clazz) && !field.isAnnotationPresent(Adapter.class)) {
                        if (value == null) {
                            addedAnyFields = true;
                            config.set(key, defaultValue);
                            continue;
                        }
                        field.set(object, field.getType().cast(config.getList(key)));
                        continue;
                    }

                    ConfigAdapter adapter = getAdapter(field);
                    if (adapter == null)
                        throw new NullPointerException("Couldn't find adapter class for type " + field.getType().getSimpleName());

                    List<String> list = config.getStringList(key);
                    if (list.size() == 0) {
                        config.set(key, ((List<?>) defaultValue)
                                .stream()
                                .map(val -> adapter.toString(config, val))
                                .collect(Collectors.toList())
                        );
                        addedAnyFields = true;
                        continue;
                    }

                    List<?> mapped = adapter.fromStringList(config, key);
                    field.set(object, mapped);

                    continue;

                } catch (ClassNotFoundException e) {
                    continue;
                }
            }

            ConfigAdapter adapter = getAdapter(field);

            if (adapter == null)
                throw new NullPointerException("Couldn't find adapter class for type " + field.getType().getSimpleName());

            value = adapter.fromString(config, key);
            if (value == null) {
                value = defaultValue;
                config.set(key, adapter.toString(config, defaultValue));
                addedAnyFields = true;
                if (value == null) continue;
            } else field.set(object, value);

        }

        if (addedAnyFields) config.save();
    }

    private static ConfigAdapter getAdapter(Field field) {
        ConfigAdapter adapter;
        if (field.isAnnotationPresent(Adapter.class)) {
            Class<?> clazz = field.getAnnotation(Adapter.class).value();
            adapter = Config.getAdapterByAdapterClass(clazz);
            if (adapter == null)
                throw new NullPointerException("Couldn't find custom adapter class by type " + clazz.getSimpleName());
        } else {
            adapter = Config.getAdapter(field.getType());
        }
        return adapter;
    }


}
