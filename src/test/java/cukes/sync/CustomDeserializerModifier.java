package cukes.sync;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class CustomDeserializerModifier extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config,
                                                  final BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {

        if (deserializer instanceof BeanDeserializerBase) {
            return new CustomBeanDeserializer((BeanDeserializerBase) deserializer);
        }
        return deserializer;
    }
}
