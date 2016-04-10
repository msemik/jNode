package pl.edu.uj.jnode.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.uj.jnode.context.testdata.CallbackWithContextFields;
import pl.edu.uj.jnode.context.testdata.ContextClass;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestsConfig.class)
public class ContextInjectingCallbackPreExecutionProcessorTest {
    @Autowired
    @InjectMocks
    private ContextInjectingCallbackPreExecutionProcessor processor;
    @Autowired
    private ApplicationContext applicationContext;
    @Mock
    private Callback callback;
    @Mock
    private JarContextRegistry jarContextRegistry;
    @Mock
    private JarContext jarContext;
    @Mock
    private Jar jar;
    private ContextClass autowiredContextClass = new ContextClass();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(jarContext).when(jarContextRegistry).get(jar);
        doReturn(this.getClass().getClassLoader()).when(jar).getClassLoader();
        jarContext.injectContext(Mockito.anyObject());

        Mockito.doAnswer(invocation -> {
            if (invocation.getArgumentAt(0, Object.class) instanceof CallbackWithContextFields) {
                CallbackWithContextFields callback = invocation.getArgumentAt(0, CallbackWithContextFields.class);
                callback.setAutowiredContextClass(autowiredContextClass);
            }
            return callback;
        }).when(jarContext).injectContext(Mockito.anyObject());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void whenProcessCallbackReturnSameObject() throws Exception {
        Callback processedCallback = processor.process(jar, callback);
        assertThat(processedCallback, sameInstance(callback));
        Mockito.verify(jarContext).injectContext(callback);
    }

    @Test
    public void whenProcessCallbackWithAutowiredCallbackContextShouldInjectIt() throws Exception {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        processor.process(jar, callback);
        assertThat(callback.getAutowiredContextClass(), equalTo(autowiredContextClass));
    }
}