package factorial;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Callback;

import java.math.BigInteger;

/**
 * Created by alanhawrot on 13.04.2016.
 */
public class FactorialCallback implements Callback {
    @InjectContext
    private FactorialResult factorialResult;

    @Override
    public void onSuccess(Object taskResult) {
        BigInteger partialResult = (BigInteger) taskResult;
        BigInteger result = factorialResult.multiplyByPartialResult(partialResult);
        System.out.println(result);
    }

    @Override
    public void onFailure(Throwable ex) {
        System.out.println(ex.getMessage());
    }
}
