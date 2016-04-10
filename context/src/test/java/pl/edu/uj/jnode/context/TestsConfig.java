package pl.edu.uj.jnode.context;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan({"pl.edu.uj.context", "pl.edu.uj.contexttestdata"})
public class TestsConfig {
}
