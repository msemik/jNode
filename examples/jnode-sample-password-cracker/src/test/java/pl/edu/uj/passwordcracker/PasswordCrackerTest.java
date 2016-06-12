package pl.edu.uj.passwordcracker;

import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class PasswordCrackerTest {
    public static final String ABC = "abc";
    private PasswordCracker passwordCracker;
    private PasswordCracker secondPasswordCracker;

    @Test
    public void hasNext() throws Exception {
        assertHasNextIs(createPasswordCrackerWithLimit(0), false);
        assertHasNextIs(createPasswordCrackerWithLimit(-1), true);
        passwordCracker = createPasswordCrackerWithLimit(1);
        assertHasNextIs(createPasswordCrackerWithLimit(1), true);
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(passwordCracker, true);
        passwordCracker.next();
        assertHasNextIs(passwordCracker, false);

        passwordCracker = createPasswordCrackerWithLimit(2);
        passwordCracker.next();
        passwordCracker.next();
        assertHasNextIs(passwordCracker, false);

        passwordCracker = createPasswordCrackerWithLimit(-1);
        passwordCracker.next();
        passwordCracker.next();
        assertHasNextIs(passwordCracker, true);

    }

    private PasswordCracker createPasswordCrackerWithLimit(int limit) {
        return new PasswordCracker(new byte[1], ABC, BigInteger.valueOf(limit), 1);
    }

    @Test
    public void next() throws Exception {
        passwordCracker = createPasswordCrackerWithLimit(-1);
        assertNextStringsEqual(passwordCracker, //
                               "a", "b", "c", //
                               "aa", "ab", "ac", "ba", "bb", "bc", "ca", "cb", "cc", //
                               "aaa", "aab", "aac", "aba", "abb", "abc", "aca", "acb", "acc", "baa");

        passwordCracker = createPasswordCrackerWithLimit(22);
        assertNextStringsEqual(passwordCracker, //
                               "a", "b", "c", //
                               "aa", "ab", "ac", "ba", "bb", "bc", "ca", "cb", "cc", //
                               "aaa", "aab", "aac", "aba", "abb", "abc", "aca", "acb", "acc", "baa");

    }

    private void assertNextStringsEqual(PasswordCracker passwordCracker, String... expectedStrings) {
        for (String expectedString : expectedStrings) {
            assertThat(passwordCracker.next(), equalTo(expectedString));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void nextFailsWhenLimitIsNotEnough() throws Exception {
        passwordCracker = createPasswordCrackerWithLimit(2);
        assertNextStringsEqual(passwordCracker, "a", "b", "c");
    }

    @Test(expected = IllegalStateException.class)
    public void nextFailsWhenLimitIsNotEnough2() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("aaa", 1, 1);
        secondPasswordCracker = passwordCracker.separateJobSet();
        passwordCracker.next();
    }

    @Test(expected = IllegalStateException.class)
    public void nextFailsWhenLimitIsNotEnough3() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("aaa", 1, 1);
        secondPasswordCracker = passwordCracker.separateJobSet();
        secondPasswordCracker.next();
        secondPasswordCracker.next();
        secondPasswordCracker.next();
        secondPasswordCracker.next();
    }

    @Test
    public void separateJobSet1() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("aaa", 1, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "aba");
        assertNextStringsEqual(secondPasswordCracker, "aaa");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "abb");
        assertNextStringsEqual(secondPasswordCracker, "aab");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "abc");
        assertNextStringsEqual(secondPasswordCracker, "aac");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, false);

        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "baa");
        assertNextStringsEqual(secondPasswordCracker, "aca");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "bab");
        assertNextStringsEqual(secondPasswordCracker, "acb");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "bac");
        assertNextStringsEqual(secondPasswordCracker, "acc");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, false);
    }

    @Test
    public void separateJobSet2() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("cccc", 3, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertNextStringsEqual(passwordCracker, "aaccc", "abaaa");
        assertNextStringsEqual(secondPasswordCracker, "cccc", "aaaaa");
        assertHasNextIs(passwordCracker, true);
        assertHasNextIs(secondPasswordCracker, true);
        assertLastIs(secondPasswordCracker, "aaccb");
    }

    private void assertLastIs(PasswordCracker passwordCracker, String lastString) {
        String l = null;
        int i = 0;
        while (passwordCracker.hasNext()) {
            ++i;
            l = passwordCracker.next();
        }
        System.out.println("found strings: " + i);
        assertThat(l, equalTo(lastString));
    }

    @Test
    public void separateJobSet3() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("aaa", 3, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "aaaa", "aaab");
        assertNextStringsEqual(secondPasswordCracker, "aaa", "aab");
        assertLastIs(secondPasswordCracker, "ccc");
    }

    @Test
    public void separateJobSet4() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("ccc", 3, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "accc", "baaa");
        assertNextStringsEqual(secondPasswordCracker, "ccc", "aaaa");
        assertLastIs(secondPasswordCracker, "accb");
    }

    @Test
    public void separateJobSet5() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("ccc", 4, -1);
        //stopAt(passwordCracker, "aaccc");
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "aaccc", "abaaa");
        assertNextStringsEqual(secondPasswordCracker, "ccc", "aaaa");
        assertLastIs(secondPasswordCracker, "aaccb");
    }

    @Test
    public void separateJobSet6() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("ccc", 5, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "aaaccc", "aabaaa");
        assertNextStringsEqual(secondPasswordCracker, "ccc", "aaaa");
        assertLastIs(secondPasswordCracker, "aaaccb");
    }

    @Test
    public void separateJobSet7() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("ccc", 6, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "aaaaccc", "aaabaaa");
        assertNextStringsEqual(secondPasswordCracker, "ccc", "aaaa");
        assertLastIs(secondPasswordCracker, "aaaaccb");
    }

    @Test
    public void separateJobSet8() throws Exception {
        passwordCracker = createPasswordCrackerWithInitialSequenceFactorAndLimit("acacbacbacbac", 6, -1);
        secondPasswordCracker = this.passwordCracker.separateJobSet();
        assertNextStringsEqual(passwordCracker, "acacbbabacbac", "acacbbabacbba");
        assertNextStringsEqual(secondPasswordCracker, "acacbacbacbac", "acacbacbacbba");
        assertLastIs(secondPasswordCracker, "acacbbabacbab");
    }

    private void stopAt(PasswordCracker passwordCracker, String expectedString) {
        int i = 0;
        Set<String> s = new HashSet<>();
        while (true) {
            ++i;
            String next = passwordCracker.next();
            if (s.contains(next)) {
                throw new IllegalStateException("no !");
            }
            s.add(next);//System.out.println(i + ":\t\t" + next);
            if (next.equals(expectedString)) {
                System.out.println("found in iteration " + i + " " + expectedString);
                return;
            }
        }
    }


    private void assertHasNextIs(PasswordCracker passwordCracker, boolean hasNex) {
        assertThat(passwordCracker.hasNext(), equalTo(hasNex));
    }

    private PasswordCracker createPasswordCrackerWithInitialSequenceFactorAndLimit(String initialSequence, int factor, int limit) {
        byte[] iterationPositions = new byte[initialSequence.length()];
        for (int i = 0; i < initialSequence.length(); i++) {
            iterationPositions[i] = (byte) ABC.indexOf(initialSequence.charAt(i));
        }
        return new PasswordCracker(iterationPositions, ABC, BigInteger.valueOf(limit), factor);
    }

}