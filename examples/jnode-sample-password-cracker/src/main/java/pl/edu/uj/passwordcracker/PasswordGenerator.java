package pl.edu.uj.passwordcracker;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import static java.lang.Math.abs;
import static java.math.BigInteger.*;
import static java.util.Arrays.fill;
import static org.apache.commons.lang3.ArrayUtils.addAll;

public class PasswordGenerator implements Serializable {
    private static BigInteger NO_LIMIT = BigInteger.valueOf(-1);
    private byte[] iterationPointers;
    private String charSet;
    private BigInteger iterationsLimit = NO_LIMIT;
    private int jobsSeparationFactor;

    public PasswordGenerator(String charSet, int jobsSeparationFactor) {
        this(new byte[1], charSet, NO_LIMIT, jobsSeparationFactor);
    }

    protected PasswordGenerator(byte[] iterationPointers, String charSet, BigInteger iterationsLimit, int jobsSeparationFactor) {
        this.iterationPointers = iterationPointers;
        this.charSet = charSet;
        this.iterationsLimit = iterationsLimit;
        this.jobsSeparationFactor = jobsSeparationFactor;
    }

    public boolean hasNext() {
        if (iterationsLimit == NO_LIMIT) {
            return true;
        }
        return !iterationsLimit.equals(ZERO);
    }

    public String next() {
        if (iterationsLimit.equals(ZERO)) {
            throw new IllegalStateException("No next value present.");
        }
        if (iterationsLimit != NO_LIMIT) {
            iterationsLimit = iterationsLimit.subtract(ONE);
        }
        String result = buildValue();
        incrementIterationPointers(iterationPointers.length - 1);
        return result;
    }

    /**
     * @return Separated set of password candidates which need to be checked. Set is represented as limited PasswordGenerator.
     * Size of set is dependant on jobsSeparationFactor parameter.
     * When length of word is already greater than jobsSeparationFactor, then number of extracted words
     * will be equal to charSet.length() ^ jobsSeparationFactor. Otherwise it may be even twice larger.
     */
    public PasswordGenerator separateJobSet() {
        int baseJobsSeparationLimit = ArithmeticUtils.pow(charSet.length(), jobsSeparationFactor);
        byte[] copyOfIterationPointers = Arrays.copyOf(iterationPointers, iterationPointers.length);


        if (iterationPointers.length <= jobsSeparationFactor) {
            // When need to extend array there is more passwords, than when array is bigger than separation point (incremented index)
            // All these password will be separated in single job set for convenience.
            for (int i = iterationPointers.length; i < jobsSeparationFactor; i++) {
                baseJobsSeparationLimit += ArithmeticUtils.pow(charSet.length(), i);
            }
            iterationPointers = addAll(new byte[abs(iterationPointers.length - jobsSeparationFactor) + 1], iterationPointers);

        } else {
            incrementIterationPointers(iterationPointers.length - jobsSeparationFactor - 1);
        }

        BigInteger copyIterationsLimit = BigInteger.valueOf(baseJobsSeparationLimit);
        if (iterationsLimit.equals(ZERO)) {
            copyIterationsLimit = BigInteger.valueOf(0);
        } else if (iterationsLimit != NO_LIMIT) {
            iterationsLimit = iterationsLimit.subtract(BigInteger.valueOf(baseJobsSeparationLimit));
            if (iterationsLimit.compareTo(ZERO) < 0) {
                iterationsLimit = ZERO;
            }
        }
        return new PasswordGenerator(copyOfIterationPointers, charSet, copyIterationsLimit, jobsSeparationFactor);

    }

    private void incrementIterationPointers(int startingPosition) {

        for (int i = startingPosition; i >= 0; i--) {
            if (iterationPointers[i] + 1 < charSet.length()) {
                ++iterationPointers[i];
                return;
            } else {
                iterationPointers[i] = 0;
            }
        }
        iterationPointers = addAll(new byte[1], iterationPointers);
    }

    private String buildValue() {
        StringBuilder b = new StringBuilder();
        for (byte iterationPointer : iterationPointers) {
            b.append(charSet.charAt(iterationPointer));
        }
        return b.toString();
    }

}
