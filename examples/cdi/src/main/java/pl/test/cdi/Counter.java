package pl.test.cdi;

import pl.edu.uj.jnode.context.Context;

@Context
public class Counter
{
    private int counter = 0;

    public int preInc()
    {
        return ++counter;
    }
}
