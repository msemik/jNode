package pl.edu.uj.jnode.context;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.uj.crosscuting.Resources;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.jnode.context.testdata.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestsConfig.class)
public class ContextScanTest
{
    @Autowired
    private ApplicationContext applicationContext;
    @Spy
    @InjectMocks
    private Jar jar = new MyJar("someNode", Paths.get("somePath"));
    private JarContext jarContext;
    private Resources resources = Resources.getInstance();

    public ContextScanTest()
    {
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void shouldCreateContextBeansFromPlPackageOnly()
    {
        doReturn(resources.getPath("context-scan.jar")).when(jar).getAbsolutePath();
        jarContext = applicationContext.getBean(JarContext.class, jar);
        List<Object> beans = jarContext.getBeans();
        List<String> namesOfBeans = beans.stream().map(Object::getClass).map(Class::getCanonicalName).collect(Collectors.toList());
        System.out.println(namesOfBeans);

        assertThat(namesOfBeans, hasItem("pl.test.example.SimpleContextClass"));
        assertThat(namesOfBeans.size(), is(equalTo(1999)));
    }

    @Test
    public void whenContextBeanAnnotationIsMissingShouldNotFindAnyBeans()
    {
        doReturn(resources.getPath("context-scan-missing.jar")).when(jar).getAbsolutePath();
        jarContext = applicationContext.getBean(JarContext.class, jar);
        assertThat(jarContext.getBeans().size(), equalTo(0));
    }
}