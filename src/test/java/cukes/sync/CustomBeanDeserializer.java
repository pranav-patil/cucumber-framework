package cukes.sync;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;


import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import cukes.sync.transformer.FieldTransformer;
import org.springframework.beans.BeanUtils;

public class CustomBeanDeserializer extends BeanDeserializer {

    private FieldTransformerFactory transformerFactory = new FieldTransformerFactory();

    public CustomBeanDeserializer(BeanDeserializerBuilder builder, BeanDescription beanDesc, BeanPropertyMap properties,
                                  Map<String, SettableBeanProperty> backRefs, HashSet<String> ignorableProps,
                                  boolean ignoreAllUnknown, boolean hasViews) {

        super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
    }

    protected CustomBeanDeserializer(BeanDeserializerBase src) {
        super(src);
    }

    protected CustomBeanDeserializer(BeanDeserializerBase src, boolean ignoreAllUnknown) {
        super(src, ignoreAllUnknown);
    }

    protected CustomBeanDeserializer(BeanDeserializerBase src, NameTransformer unwrapper) {
        super(src, unwrapper);
    }

    public CustomBeanDeserializer(BeanDeserializerBase src, ObjectIdReader oir) {
        super(src, oir);
    }

    public CustomBeanDeserializer(BeanDeserializerBase src, Set<String> ignorableProps) {
        super(src, ignorableProps);
    }

    public CustomBeanDeserializer(BeanDeserializerBase src, BeanPropertyMap props) {
        super(src, props);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // common case first
        if (p.isExpectedStartObjectToken()) {
            if (_vanillaProcessing) {
                return vanillaDeserialize(p, ctxt, p.nextToken());
            }
            // 23-Sep-2015, tatu: This is wrong at some many levels, but for now... it is
            //    what it is, including "expected behavior".
            p.nextToken();
            if (_objectIdReader != null) {
                return deserializeWithObjectId(p, ctxt);
            }
            return deserializeFromObject(p, ctxt);
        }
        return _customDeserializeOther(p, ctxt, p.getCurrentToken());
    }

    private Object _customDeserializeOther(JsonParser p, DeserializationContext ctxt, JsonToken t) throws IOException {
        // and then others, generally requiring use of @JsonCreator
        if (t != null) {
            switch (t) {
                case VALUE_STRING:
                    return deserializeFromString(p, ctxt);
                case VALUE_NUMBER_INT:
                    return deserializeFromNumber(p, ctxt);
                case VALUE_NUMBER_FLOAT:
                    return deserializeFromDouble(p, ctxt);
                case VALUE_EMBEDDED_OBJECT:
                    return deserializeFromEmbedded(p, ctxt);
                case VALUE_TRUE:
                case VALUE_FALSE:
                    return deserializeFromBoolean(p, ctxt);
                case VALUE_NULL:
                    return deserializeFromNull(p, ctxt);
                case START_ARRAY:
                    // these only work if there's a (delegating) creator...
                    return deserializeFromArray(p, ctxt);
                case FIELD_NAME:
                case END_OBJECT: // added to resolve [JACKSON-319], possible related issues
                    if (_vanillaProcessing) {
                        return vanillaDeserialize(p, ctxt, t);
                    }
                    if (_objectIdReader != null) {
                        return deserializeWithObjectId(p, ctxt);
                    }
                    return deserializeFromObject(p, ctxt);
                default:
            }
        }
        return ctxt.handleUnexpectedToken(handledType(), p);
    }

    private Object vanillaDeserialize(JsonParser p, DeserializationContext ctxt, JsonToken t) throws IOException {
        final Object bean = _valueInstantiator.createUsingDefault(ctxt);
        // [databind#631]: Assign current value, to be accessible by custom serializers
        p.setCurrentValue(bean);
        if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
            String propName = p.getCurrentName();
            do {
                p.nextToken();
                SettableBeanProperty prop = _beanProperties.find(propName);

                if (prop != null) { // normal case
                    try {
                        deserializeAndSetProperty(p, ctxt, bean, prop);
                    } catch (Exception e) {
                        wrapAndThrow(e, bean, propName, ctxt);
                    }
                    continue;
                }
                handleUnknownVanilla(p, ctxt, bean, propName);
            } while ((propName = p.nextFieldName()) != null);
        }
        return bean;
    }

    private void deserializeAndSetProperty(JsonParser p, DeserializationContext ctxt, Object bean, SettableBeanProperty prop) throws IOException {

        boolean isTransformed = false;
        Class<?> rawClass = prop.getType().getRawClass();

        if(BeanUtils.isSimpleValueType(rawClass)) {

            Optional<FieldTransformer> fieldTransformer = transformerFactory.getFieldTransformer(prop.getName(), rawClass, bean.getClass());

            if(fieldTransformer.isPresent()) {
                String tokenValue = getTokenValue(p);
                Object transformedValue = fieldTransformer.get().transform(tokenValue);
                prop.set(bean, transformedValue);
                isTransformed = true;
            }
        }

        if(!isTransformed) {
            prop.deserializeAndSet(p, ctxt, bean);
        }
    }

    private String getTokenValue(JsonParser parser) throws IOException {

        JsonToken jsonToken = parser.getCurrentToken();

        switch (jsonToken) {
            case VALUE_STRING:
                return parser.getText();
            case VALUE_NULL:
                return null;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
            case VALUE_TRUE:
            case VALUE_FALSE:
                return parser.getValueAsString();
            default:
                return jsonToken.name();
        }
    }
}
