package com.clertonleal.proto;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Proto {

    private static final String TAG = Proto.class.getName();

    private static final Configuration CONFIGURATION = new Configuration();

    private Proto() {}

    /**
     * @param cursor Cursor in position to be serialized
     * @param clazz Class of object to be serialized
     * @param <T> Generic type of object to be serialized
     * @param closeCursor Boolean to check if cursors should be closed
     * @return Serialized object or null when a instantiation error occurs
     */
    public static <T> T object(Cursor cursor, Class<T> clazz, boolean closeCursor) {
        T instance;

        try {
            instance = serializeCursor(cursor, clazz);

            if (closeCursor) {
                cursor.close();
            }

            return instance;
        } catch (InstantiationException e) {
            Log.e(TAG, "Error to instantiate a object of " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "The default constructor of " + clazz.getName() + " is not visible", e);
        }

        return null;
    }

    /**
     * @param cursor Cursor in position to be serialized
     * @param clazz Class of object to be serialized
     * @param <T> Generic type of object to be serialized
     * @return Serialized object or null when a instantiation error occurs
     */
    public static <T> T object(Cursor cursor, Class<T> clazz) {
        T instance;

        try {
            instance = serializeCursor(cursor, clazz);

            if (configuration().isClosingCursor()) {
                cursor.close();
            }

            return instance;
        } catch (InstantiationException e) {
            Log.e(TAG, "Error to instantiate a object of " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "The default constructor of " + clazz.getName() + " is not visible", e);
        }
        return null;
    }

    /**
     * @param cursor Cursor in position to be serialized
     * @param clazz Class of object to be serialized
     * @param <T> Generic type of object to be serialized
     * @param closeCursor Boolean to check if cursors should be closed
     * @return List with the serialized objects that were correctly instantiated
     */
    public static <T> List<T> list(Cursor cursor, Class<T> clazz, boolean closeCursor) {
        final List<T> list =  new ArrayList<>();

        if (cursor.moveToFirst()) {
            List<Field> fields = getFields(clazz);
            HashMap<String, Integer> columnsMap = cursorMapColumns(cursor, fields);
            do {
                T instance = null;

                try {
                    instance = serializeCursor(cursor, clazz, fields, columnsMap);
                } catch (InstantiationException e) {
                    Log.e(TAG, "Error to instantiate a object of " + clazz.getName(), e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "The default constructor of " + clazz.getName() + " is not visible", e);
                }

                if (instance != null) {
                    list.add(instance);
                }
            } while (cursor.moveToNext());
        }
        if (closeCursor) {
            cursor.close();
        }

        return list;
    }

    /**
     * @param cursor Cursor in position to be serialized
     * @param clazz Class of object to be serialized
     * @param <T> Generic type of object to be serialized
     * @return List with the serialized objects that were correctly instantiated
     */
    public static <T> List<T> list(Cursor cursor, Class<T> clazz) {
        return list(cursor, clazz, configuration().isClosingCursor());
    }

    /**
     * @return The configuration object of Proto
     */
    public static Configuration configuration() {
        return CONFIGURATION;
    }

    private static <T> T serializeCursor(Cursor cursor, Class<T> clazz)
            throws InstantiationException, IllegalAccessException {
        return serializeCursor(cursor, clazz, getFields(clazz), null);
    }

    private static <T> T serializeCursor(Cursor cursor, Class<T> clazz, List<Field> fields,
                                         HashMap<String, Integer> columnsMap)
            throws InstantiationException, IllegalAccessException {
        final T instance = clazz.newInstance();
        if (columnsMap == null) {
            columnsMap = cursorMapColumns(cursor, fields);
        }
        for(Field field : fields) {
            setFieldInObject(instance, field, cursor, columnsMap);
        }

        return instance;
    }

    private static HashMap<String, Integer> cursorMapColumns(Cursor cursor, List<Field> fields) {
        HashMap<String, Integer> columnMap = new HashMap<>();
        for (Field field: fields) {
            final DatabaseField annotation = field.getAnnotation(DatabaseField.class);
            if (annotation == null) {
                continue;
            }
            final String columnName = annotation.columnName();
            final int fieldIndex = cursor.getColumnIndex(columnName);
            columnMap.put(columnName, fieldIndex);
        }
        return columnMap;
    }

    private static void setFieldInObject(Object o, Field field, Cursor cursor,
                                         HashMap<String, Integer> columnMap) {
        final DatabaseField annotation = field.getAnnotation(DatabaseField.class);
        if (annotation == null) {
            return;
        }

        final String columnName = annotation.columnName();
        final Integer fieldIndex = columnMap.get(columnName);
        if (fieldIndex == null) {
            Log.e(TAG, "Don't find column to to field: " + field.getName());
            return;
        }

        Object result = null;
        final Class clazz = field.getType();

        if (clazz == Integer.class || clazz == Integer.TYPE) {
            result = cursor.getInt(fieldIndex);
        } else if (clazz == Long.class || clazz == Long.TYPE) {
            result = cursor.getLong(fieldIndex);
        } else if (clazz == Double.class || clazz == Double.TYPE) {
            result = cursor.getDouble(fieldIndex);
        } else if (clazz == String.class) {
            result = cursor.getString(fieldIndex);
        } else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            result = cursor.getInt(fieldIndex) > 0;
        } else if (clazz == Date.class) {
            result = new Date(cursor.getLong(fieldIndex));
        }

        if (result != null) {
            field.setAccessible(true);
            try {
                field.set(o, result);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Error to set value \"" + result + "\" in field \"" + field.getName() + "\".", e);
            }
        }
    }

    private static List<Field> getFields(Class clazz) {
        Class actualClass = clazz;
        final List<Field> fields = new ArrayList<>();

        do {
            fields.addAll(Arrays.asList(actualClass.getDeclaredFields()));
            actualClass = actualClass.getSuperclass();
        } while (actualClass != null);

        return fields;
    }
}