package com.nekonade.network.message.test;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;

@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)//必须有这个注解，要不然@SpyBean和@MockBean标记的类会为null
@PowerMockIgnore({"org.springframework.*","javax.*","org.mockito.*"})
public abstract class AbstractNekoNadeUnitTest extends AbstractTestNGSpringContextTests {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }
}
