package cukes.sync.transformer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateFieldTransformer implements FieldTransformer<Long> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean supports(String fieldName, Class<Long> fieldType, Class<?> parentType) {
        return "someDateField".equals(fieldName);
    }

    @Override
    public Long transform(String fieldValue) {

        if(fieldValue != null && fieldValue instanceof String) {
            LocalDate date = LocalDate.parse(fieldValue.toString(), DATE_FORMATTER);
            return date.atTime(0,0,0).toEpochSecond(ZoneOffset.UTC);
        }

        return 0L;
    }
}
