package io.craft.armor;

import io.craft.armor.api.ArmorDegradeException;
import io.craft.armor.api.ArmorFactory;
import io.craft.armor.api.ArmorService;
import io.craft.armor.api.ArmorTimeoutException;
import io.craft.atom.test.CaseCounter;

import java.lang.reflect.Proxy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mindwind
 * @version 1.0, Dec 18, 2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class TestArmor extends AbstractJUnit4SpringContextTests {
	
	           
	@Autowired private DemoService  demoService ;
               private ArmorService armorService;
           

    @Before
    public void before() {
    	armorService = ArmorFactory.armorService();
    }
	
	@Test
	public void testArmorStartup() {
		boolean isProxy = Proxy.isProxyClass(demoService.getClass());
		Assert.assertTrue(isProxy);
		boolean isOk = demoService.isOk();
		Assert.assertTrue(isOk);
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor startup. ", CaseCounter.incr(2)));
	}
	
	@Test
	public void testArmorDegrade() {
		armorService.degrade(DemoServiceImpl.class, "echo", new Class<?>[] { String.class });
		String in = "test";
		try {
			demoService.echo(in);
			Assert.fail();
		} catch (ArmorDegradeException e) {
			Assert.assertTrue(true);
		}
		armorService.upgrade(DemoServiceImpl.class, "echo", new Class<?>[] { String.class });
		
		String out = demoService.echo(in);
		Assert.assertEquals(out, in);
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor degrade. ", CaseCounter.incr(2)));
	}
	
	@Test
	public void testArmorTransfer() {
		DemoService ds1 = (DemoService) armorService.getDelegateObject("demoService");
		DemoService ds2 = (DemoService) armorService.getDelegateObject("demoService2");
		boolean isProxy = Proxy.isProxyClass(ds1.getClass()) || Proxy.isProxyClass(ds2.getClass());
		Assert.assertFalse(isProxy);
		String in = "test";
		String out = demoService.echo(in);
		Assert.assertEquals(out, in);
		armorService.setTransferObject(ds1, ds2);
		out = demoService.echo(in);
		Assert.assertEquals(out, in + in);
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor transfer. ", CaseCounter.incr(3)));
	}
	
	@Test
	public void testArmorCascade() {
		String in = "test";
		String out = demoService.echoCascade(in);
		Assert.assertEquals(out, in);
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor cascade. ", CaseCounter.incr(1)));
	}
	
	@Test
	public void testArmorTimeout() {
		armorService.setTimeoutInMillis(DemoServiceImpl.class, "timeout", new Class<?>[] { int.class }, 25);
		try {
			demoService.timeout(50);
		} catch (ArmorTimeoutException e) {
			Assert.assertTrue(true);
		}
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor timeout. ", CaseCounter.incr(1)));
	}
	
	@Test
	public void testArmorThrowException() {
		try {
			demoService.throwException();
			Assert.fail();
		} catch (IllegalAccessException e) {
			Assert.assertTrue(true);
		}
		System.out.println(String.format("[CRAFT-ATOM-NIO] (^_^)  <%s>  Case -> test armor throw exception. ", CaseCounter.incr(1)));
	}

}
