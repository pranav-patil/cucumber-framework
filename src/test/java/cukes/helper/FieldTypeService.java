package cukes.helper;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static cukes.stub.DateStubService.DEFAULT_DATE_FORMAT;

@Service
public class FieldTypeService {

    private static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    public Map<String, Object> convertToObjectMap(Class<?> tableClass, Map<String, String> dataMap) {
        Map<String, Object> row = new HashMap<>();

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            row.put(key, getObject(tableClass, key, entry.getValue()));
        }
        return row;
    }

    public Object getObject(Class<?> clazz, String fieldName, String fieldValue) {
        Field field = getDeclaredField(clazz, fieldName);
        if (field != null) {
            return toObject(field.getType(), fieldValue);
        } else {
            return detectObjectType(fieldValue);
        }
    }

    private Field getDeclaredField(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
                return getDeclaredField(type.getSuperclass(), fieldName);
            }
        }
        return null;
    }

    private Object toObject(Class clazz, String value) {

        if (value == null || "null".equals(value)) {
            return null;
        }

        if (Date.class == clazz) {
            try {
                return DEFAULT_DATE_FORMATTER.parse(value);
            } catch (ParseException pex) {
            }
        }

        if (String.class == clazz) return value;
        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || int.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(value);
        return value;
    }

    private Object detectObjectType(String string) {

        if (string.matches("(true|false)")) {
            return Boolean.valueOf(string);
        }

        if (string.equals("null")) {
            return null;
        }

        try {
            return DEFAULT_DATE_FORMATTER.parse(string);
        } catch (ParseException pex) {
        }

        return string;
    }
}
