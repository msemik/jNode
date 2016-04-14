package pl.test.cdi;

import pl.edu.uj.jnode.context.Context;

@Context
public class Dependency {
    public Dependency() {
        System.out.println("!!!!!!!!!!!!!!! Creating dependency !!!!!!!!!!!!!!!");
    }
}
