package pl.edu.uj.jnode.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.jarpath.NewJarCreatedEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestsConfig.class)
public class JarContextRegistryTest {
    @Autowired
    JarContextRegistry jarContextRegistry;
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Mock
    private Jar jar;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(this.getClass().getClassLoader()).when(jar).getChildFirstClassLoader();
        Mockito.when(jar.getAnnotation(Mockito.anyString())).thenCallRealMethod();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void whenGetContextShouldRetrieveAlwaysSame() throws Exception {
        assertThat(jarContextRegistry.hasFor(jar), equalTo(false));
        JarContext jarContext = jarContextRegistry.get(jar);
        JarContext anotherJarContext = jarContextRegistry.get(jar);
        assertThat(jarContextRegistry.hasFor(jar), equalTo(true));

        assertThat(jarContext, notNullValue());
        assertThat(jarContext, sameInstance(anotherJarContext));
    }

    @Test
    public void onNewJarCreatedEventShouldRegisterNewJarContext() throws Exception {
        assertThat(jarContextRegistry.hasFor(jar), equalTo(false));
        eventPublisher.publishEvent(new NewJarCreatedEvent(this, jar));
        assertThat(jarContextRegistry.hasFor(jar), equalTo(true));
    }
}