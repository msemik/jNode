package factorial;

import pl.edu.uj.jnode.context.Context;

import java.math.BigInteger;

/**
 * Created by alanhawrot on 13.04.2016.
 */
@Context
public class FactorialResult {
    private BigInteger result = BigInteger.ONE;

    public BigInteger multiplyByPartialResult(BigInteger partialResult) {
        return result = result.multiply(partialResult);
    }
}
