package pl.test.cdi;

import pl.edu.uj.jnode.context.Context;

import java.io.Serializable;

@Context
public class Dependency implements Serializable {
    public Dependency() {
        System.out.println("!!!!!!!!!!!!!!! Creating dependency !!!!!!!!!!!!!!!");
    }
}
