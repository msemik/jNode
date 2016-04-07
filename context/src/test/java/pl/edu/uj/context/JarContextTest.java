package pl.edu.uj.context;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xeustechnologies.jcl.JarClassLoader;
import pl.edu.uj.contexttestdata.*;
import pl.edu.uj.jarpath.Jar;

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
/*        JarClassLoader jarClassLoader = new JarClassLoader();
        jarClassLoader.getLocalLoader().setEnabled(false);
        jarClassLoader.getOsgiBootLoader().setEnabled(false);
        jarClassLoader.getParentLoader().setEnabled(false);
        jarClassLoader.getSystemLoader().setEnabled(false);
        jarClassLoader.getThreadLoader().setEnabled(false);
        jarClassLoader.getCurrentLoader().setEnabled(false);
        jarClassLoader.add(ContextClass.class.getCanonicalName());
        jarClassLoader.add(JarContext.class.getCanonicalName());*/
        doReturn(this.getClass().getClassLoader()) //execution duration problem
                .when(jar)
                .getClassLoader();
        jarContext = applicationContext.getBean(JarContext.class, jar);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void autowiredWhenAutowiredAnnotationIsPresentOnContextBean() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.autowire(callback);

        ContextClass contextField = callback.getAutowiredContextClass();
        assertThat(contextField, notNullValue());
    }

    @Test
    public void notAutowiredWhenAutowiredAnnotationIsNotPresentOnContextBean() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.autowire(callback);

        ContextClass contextField = callback.getNotAutowiredContextClass();
        assertThat(contextField, nullValue());
    }

    @Test
    public void notAutowiredWhenContextAnnotationIsNotPresent() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        jarContext.autowire(callback);

        Object nonContextField = callback.getNonContextClass();
        assertThat(nonContextField, nullValue());
    }
}