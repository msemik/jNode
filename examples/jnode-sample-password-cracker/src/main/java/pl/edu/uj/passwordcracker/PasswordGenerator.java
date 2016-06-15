package pl.edu.uj.passwordcracker;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import static java.lang.Math.abs;
import static java.math.BigInteger.*;

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
     * @return separated part of permutations which need to be checked. Permutations are represented as limited PasswordGenerator. number of permutations
     * is dependant on jobsSeparationFactor parameter. When length of word is already bigger than jobsSeparationFactor, then number of extracted permutations
     * will be equal to charSet.length() ^ jobsSeparationFactor. Otherwise it may be about twice larger.
     */
    public PasswordGenerator separateJobSet() {
        int baseJobsSeparationLimit = ArithmeticUtils.pow(charSet.length(), jobsSeparationFactor);
        byte[] copyOfIterationPointers = Arrays.copyOf(iterationPointers, iterationPointers.length);


        if (iterationPointers.length <= jobsSeparationFactor) {
            // when need to extend array there is more jobs, than when array is bigger than separation point (incremented index)
            // they all will be separated
            for (int i = iterationPointers.length; i < jobsSeparationFactor; i++) {
                baseJobsSeparationLimit += ArithmeticUtils.pow(charSet.length(), i);
            }
            iterationPointers = ArrayUtils.addAll(new byte[abs(iterationPointers.length - jobsSeparationFactor) + 1], iterationPointers);

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
        int oldIterationPointersLength = iterationPointers.length;
        iterationPointers = new byte[oldIterationPointersLength + 1];
        if (startingPosition != oldIterationPointersLength - 1) {
            //Fix case when array extended during incrementation by factor;
            //Currently its increased by one, so procedure will can be repeated and then decremented by one.
            //So final operation will result in incrementing by factor we wanted.

            incrementIterationPointers(startingPosition + 1);
            decrementIterationPointers();
        }
    }

    private void decrementIterationPointers() {
        for (int i = iterationPointers.length - 1; i >= 0; i--) {
            if (iterationPointers[i] > 0) {
                --iterationPointers[i];
                return;
            } else {
                iterationPointers[i] = (byte) (charSet.length() - 1);
            }
        }
    }

    private String buildValue() {
        StringBuilder b = new StringBuilder();
        for (byte iterationPointer : iterationPointers) {
            b.append(charSet.charAt(iterationPointer));
        }
        return b.toString();
    }

}
