package cukes.config;

import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rinoto
 * @see <a href="https://github.com/rinoto/spring-auto-mock">spring-auto-mock</a>
 * It automagically creates mockito mocks for dependencies not found on the classpath.
 * <p>
 * Initially based on the work from Justin Ryan published on <a
 * href="http://java.dzone.com/tips/automatically-inject-mocks">DZone</a>
 */
@Component
public class AutoMockRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private static final Map<Class<?>, Object> MOCKS = new ConcurrentHashMap<>();

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

		MOCKS.clear();

		for (String beanName : registry.getBeanDefinitionNames()) {
			final BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			registerMocksForBean(registry, beanDefinition);
		}
	}

	private void registerMocksForBean(BeanDefinitionRegistry registry, final BeanDefinition beanDefinition) {
		Class<?> beanClass = getBeanClass(beanDefinition);
		registerMocksForClass(registry, beanClass);
	}

	private void registerMocksForClass(BeanDefinitionRegistry registry, Class<?> beanClass) {
		if (beanClass == null) {
			return;
		}
		for (final FieldDefinition fieldDef : findAllAutoWired(beanClass)) {
			if (!isBeanAlreadyRegistered(registry, fieldDef)) {
				registerMockFactoryBeanForField(registry, fieldDef);
			}
		}
		// the parents also need to be registered
		registerMocksForClass(registry, beanClass.getSuperclass());
	}

	private boolean isBeanAlreadyRegistered(BeanDefinitionRegistry registry, FieldDefinition fieldDef) {
		if (ListableBeanFactory.class.isInstance(registry)) {
			ListableBeanFactory listableBeanFactory = ListableBeanFactory.class.cast(registry);
			if (listableBeanFactory.getBeanNamesForType(fieldDef.type).length != 0) {
				return true;
			}
		} else if (registry.isBeanNameInUse(fieldDef.name)) {
			// if BeanDefinitionRegistry doesn't implement ListableBeanFactory,
			// fall back to name check
			return true;
		}
		return false;
	}

	private Class<?> getBeanClass(final BeanDefinition beanDefinition) {

		final String beanClassName = beanDefinition.getBeanClassName();
		if (beanClassName == null) {
			return getClassFromMethodMetadata(beanDefinition);
		}

		try {
			return Class.forName(beanClassName);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Class not found for bean: " + beanClassName);
		}
	}

	/**
	 * Case when the Bean is being declared in a @Configuration, and we cannot
	 * get the BeanClassName directly from the BeanDefinition.
	 * <p>
	 * In this case we need to get the class from the IntrospectedMethod in the
	 * beanDefinition "source"
	 * 
	 * @param beanDefinition
	 * @return
	 */
	private Class<?> getClassFromMethodMetadata(final BeanDefinition beanDefinition) {
		final Object source = beanDefinition.getSource();
		if (source != null && StandardMethodMetadata.class.isInstance(source)) {
			final StandardMethodMetadata methodMetadata = StandardMethodMetadata.class.cast(source);
			final Method introspectedMethod = methodMetadata.getIntrospectedMethod();
			if (introspectedMethod != null) {
				return introspectedMethod.getReturnType();
			}
		}
		return null;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// nothing to do
	}

	private Set<FieldDefinition> findAllAutoWired(Class<?> targetBean) {
		// first finding all fields
		Set<FieldDefinition> nonAutowired = new HashSet<>();
		List<Field> declaredFields = Arrays.asList(targetBean.getDeclaredFields());
		for (Field field : declaredFields) {
			if (!field.getType().isArray() && !field.getType().isPrimitive() && isAutowiringAnnotationPresent(field)) {
				nonAutowired.add(new FieldDefinition(field.getName(), field.getType()));
			}
		}
		// now the constructors
		Constructor<?>[] constructors = targetBean.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			if (isAutowiringAnnotationPresent(constructor)) {
				Class<?>[] typeParameters = constructor.getParameterTypes();
				for (Class<?> typeParameter : typeParameters) {
					nonAutowired.add(new FieldDefinition(typeParameter.getSimpleName(), typeParameter));
				}
			}
		}
		return nonAutowired;
	}

	private boolean isAutowiringAnnotationPresent(AnnotatedElement field) {
		return field.isAnnotationPresent(Autowired.class);
	}

	private void registerMockFactoryBeanForField(final BeanDefinitionRegistry registry, final FieldDefinition fieldDef) {
		GenericBeanDefinition mockFactoryBeanDefinition = new GenericBeanDefinition();
		mockFactoryBeanDefinition.setBeanClass(MockFactoryBean.class);
		MutablePropertyValues values = new MutablePropertyValues();
		values.addPropertyValue(new PropertyValue("type", fieldDef.type));
		mockFactoryBeanDefinition.setPropertyValues(values);

		registry.registerBeanDefinition(fieldDef.name, mockFactoryBeanDefinition);
	}

	/**
	 * Container class for name and type
	 * 
	 * @author ruben
	 */
	private class FieldDefinition {

		String name;
		Class<?> type;

		public FieldDefinition(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}

	}

	/**
	 * Factory that creates mock instances based on the <code>type</code>
	 */
	public static class MockFactoryBean implements FactoryBean<Object> {

		private Class<?> type;

		public MockFactoryBean() {

		}

		public void setType(final Class<?> type) {
			this.type = type;
		}

		@Override
		public Object getObject() throws Exception {
			final Object mock = Mockito.mock(type);
			MOCKS.put(type, mock);
			return mock;
		}

		@Override
		public Class<?> getObjectType() {
			return type;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}

	/**
	 * It calls Mockito.reset in all mocks that have been created
	 */
	public static void initMocks() {
		for (Object mock : MOCKS.values()) {
			Mockito.reset(mock);
		}
	}

	/**
	 * It returned the created mocks
	 * 
	 * @return
	 */
	public static Map<Class<?>, Object> getCreatedMocks() {
		return MOCKS;
	}
}
