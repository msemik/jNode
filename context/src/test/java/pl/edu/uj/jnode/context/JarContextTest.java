package pl.edu.uj.jnode.context;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.jnode.context.testdata.*;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestsConfig.class)
public class JarContextTest
{
    @Autowired
    private ApplicationContext applicationContext;
    @Spy
    private Jar jar = new MyJar("someNode", Paths.get("somePath"));
    private JarContext jarContext;

    public JarContextTest()
    {
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        doReturn(ClassLoader.getSystemClassLoader())
                .when(jar)
                .getClassLoader();
        Mockito.when(jar.getAnnotation(Mockito.anyString())).thenCallRealMethod();
        jarContext = applicationContext.getBean(JarContext.class, jar);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void injectedWhenInjectContextAnnotationIsPresentOnContextBean() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.injectContext(callback);

        ContextClass contextField = callback.getAutowiredContextClass();
        assertThat(contextField, notNullValue());
    }

    @Test
    public void notInjectedWhenInjectContextAnnotationIsNotPresentOnContextBean() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.injectContext(callback);

        ContextClass contextField = callback.getNotAutowiredContextClass();
        assertThat(contextField, nullValue());
    }

    @Test
    public void notInjectedWhenContextAnnotationIsNotPresent() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.injectContext(callback);

        Object nonContextField = callback.getNonContextClass();
        assertThat(nonContextField, nullValue());
    }
}