package com.clertonleal.protocursor;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProtoCursor {

    private static final String TAG = ProtoCursor.class.getName();
    private static final int INVALID_FIELD = -1;

    public static <T> T toObject(Cursor cursor, Class<T> clazz) {
        try {
            final T instance = clazz.newInstance();
            for(Field field : clazz.getDeclaredFields()) {
                setFieldInObject(instance, field, cursor);
            }

            return instance;
        } catch (InstantiationException e) {
            Log.e(TAG, "Error to instantiate a object of " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "The default constructor of " + clazz.getName() + " is not visible", e);
        }

        return null;
    }

    public static <T> List<T> toList(Cursor cursor, Class<T> clazz) {
        final List<T> list =  new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                final T instance = toObject(cursor, clazz);
                if (instance != null) {
                    list.add(instance);
                }
            } while (cursor.moveToNext());

        }

        return list;
    }

    private static void setFieldInObject(Object o, Field field, Cursor cursor) {
        final DatabaseField annotation = field.getAnnotation(DatabaseField.class);
        if (annotation != null) {
            final Class clazz = field.getType();
            final int fieldIndex = cursor.getColumnIndex(annotation.fieldName());
            if (fieldIndex == INVALID_FIELD) {
                Log.e(TAG, "Dot find column to to field: " + field.getName());
                return;
            }

            Object result = null;
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
            }

            if(result != null) {
                field.setAccessible(true);
                try {
                    field.set(o, result);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Error to set value: " + result + " in field: " + field.getName(), e);
                }
            }
        }
    }

}
