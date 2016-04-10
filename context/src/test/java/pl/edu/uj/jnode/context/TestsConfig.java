package pl.edu.uj.jnode.context;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan({ "pl.edu.uj.jnode.context" })
public class TestsConfig
{
}
