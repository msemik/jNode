package pl.edu.uj.jnode.context;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.edu.uj.jnode.cluster.task.ExternalTask;
import pl.edu.uj.jnode.context.testdata.*;
import pl.edu.uj.jnode.engine.workerpool.*;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestsConfig.class)
public class ContextInjectingPreExecutionProcessorTest
{
    public static final String SOME_ID = "1";
    public static final String SOME_NODE_ID = "someNodeId";
    @Autowired
    @InjectMocks
    private ContextInjectingPreExecutionProcessor processor;
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
    @Captor
    private ArgumentCaptor<RawTask> taskArgumentCaptor;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        doReturn(jarContext).when(jarContextRegistry).get(jar);
        doReturn(this.getClass().getClassLoader()).when(jar).getChildFirstClassLoader();
        doReturn(Paths.get("someJarFilename.jar")).when(jar).getFileName();
        doNothing().when(jarContext).injectContext(taskArgumentCaptor.capture());


        Mockito.doAnswer(invocation -> {
            if(invocation.getArgumentAt(0, Object.class) instanceof CallbackWithContextFields)
            {
                CallbackWithContextFields callback = invocation.getArgumentAt(0, CallbackWithContextFields.class);
                callback.setAutowiredContextClass(autowiredContextClass);
            }
            return callback;
        }).when(jarContext).injectContext(Mockito.anyObject());
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void whenProcessCallbackReturnSameObject() throws Exception
    {
        Callback processedCallback = processor.process(jar, callback);
        assertThat(processedCallback, sameInstance(callback));
        verify(jarContext).injectContext(callback);
    }

    @Test
    public void whenProcessCallbackWithInjectContextAnnotationShouldInjectIt() throws Exception
    {
        CallbackWithContextFields callback = new CallbackWithContextFields();
        processor.process(jar, callback);
        assertThat(callback.getAutowiredContextClass(), equalTo(autowiredContextClass));
    }

    @Test
    public void whenProcessDefaultWorkerPoolTaskShouldInjectContextToItsRawTask() throws Exception
    {
        doNothing().when(jarContext).injectContext(taskArgumentCaptor.capture());
        RawTask rawTask = new RawTask();
        WorkerPoolTask workerPoolTask = new DefaultWorkerPoolTask(rawTask, jar, SOME_ID);
        processor.process(workerPoolTask);
        RawTask arg = taskArgumentCaptor.getValue();
        assertThat(rawTask, equalTo(arg));
    }

    @Test
    public void whenProcessExternalTaskShouldInjectContextToItsRawTask() throws Exception
    {
        doNothing().when(jarContext).injectContext(taskArgumentCaptor.capture());
        RawTask rawTask = new RawTask();
        WorkerPoolTask workerPoolTask = new DefaultWorkerPoolTask(rawTask, jar, SOME_ID);
        workerPoolTask = new ExternalTask(workerPoolTask, SOME_NODE_ID);
        processor.process(workerPoolTask);
        verify(jarContext).injectContext(Mockito.any());
        RawTask arg = taskArgumentCaptor.getValue();
        assertThat(rawTask, equalTo(arg));
    }
}