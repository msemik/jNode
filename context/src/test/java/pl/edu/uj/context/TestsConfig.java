package pl.edu.uj.context;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan("pl.edu.uj.context")
public class TestsConfig
{
}
