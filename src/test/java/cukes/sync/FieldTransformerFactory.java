package cukes.sync;


import cukes.sync.transformer.FieldTransformer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldTransformerFactory {

    private static final Set<FieldTransformer> FIELD_TRANSFORMERS;

    static {
        Reflections reflections = new Reflections(ClasspathHelper.forPackage("cukes.sync.transformer"), new SubTypesScanner());
        Set<Class<? extends FieldTransformer>> transformerClasses = reflections.getSubTypesOf(FieldTransformer.class);

        FIELD_TRANSFORMERS = transformerClasses.stream().map(x -> {
            try {
                return x.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
         }).collect(Collectors.toSet());
    }

    public Optional<FieldTransformer> getFieldTransformer(String fieldName, Class<?> fieldType, Class<?> parentType) {
        return FIELD_TRANSFORMERS.stream()
            .filter(e -> e.supports(fieldName, fieldType, parentType))
            .findFirst();
    }
}
