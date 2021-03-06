package io.craft.armor.api;

import io.craft.armor.ArmorContext;
import io.craft.armor.Armors;
import io.craft.armor.DefaultArmorAttribute;
import io.craft.armor.spi.ArmorProxyFactory;

import java.lang.reflect.Method;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * A spring factory hook to armed spring-based service.
 * While the service bean is initiating in spring container, this processor is callback to process {@link Armor} annotation
 * and create an armor proxy for the service bean.
 * 
 * @author mindwind
 * @author warden
 * @version 1.0, Dec 17, 2014
 */
@Component("craft.armor.bean.post.processor")
public class ArmorBeanPostProcessor implements BeanPostProcessor, Ordered {

	
	private static final Logger LOG = LoggerFactory.getLogger(ArmorBeanPostProcessor.class);
	
	
	@Getter @Setter private ArmorProxyFactory proxyFactory;
	                private ArmorContext      context     ;
	
	
	// ~ -------------------------------------------------------------------------------------------------------------
	
	
	public ArmorBeanPostProcessor() {
		proxyFactory = Armors.defaultProxyFactory();
		context      = Armors.defaultContext();
		init();
	}
	
	protected void init() { /* Any custom initialization process can be override in subclass. */ }
	
	
	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Armor armor = bean.getClass().getAnnotation(Armor.class);
		if (armor == null) { return bean; }
		
		Class<?>[] interfaces = bean.getClass().getInterfaces();
		if (interfaces == null || interfaces.length == 0) {
			LOG.warn("[CRAFT-ARMOR] Can not create armor proxy for |bean={}|, because it hasn't implement any interfaces.");
			return bean;
		}
		
		for (Class<?> clazz : interfaces) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				context.setAttribute(bean.getClass(), method.getName(), method.getParameterTypes(), new DefaultArmorAttribute());
			}
		}
		Object proxy = proxyFactory.getProxy(bean, interfaces);
		context.registerDelegateObject(beanName, bean);
		LOG.debug("[CRAFT-ARMOR] Create armor proxy for |bean={}|", bean);
		return proxy;
	}

}
