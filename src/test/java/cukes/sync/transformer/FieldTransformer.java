package cukes.sync.transformer;

public interface FieldTransformer<V> {

    boolean supports(String fieldName, Class<V> fieldType, Class<?> parentType);

    V transform(String fieldValue);
}
