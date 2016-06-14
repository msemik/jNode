package pl.edu.uj.passwordcracker;

import org.apache.commons.codec.digest.DigestUtils;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;
import java.util.Arrays;

public class PasswordCrackerTask implements Task {
    private PasswordGenerator passwordGenerator;
    private byte[] encryptedPassword;

    public PasswordCrackerTask(PasswordGenerator passwordGenerator, byte[] encryptedPassword) {
        this.passwordGenerator = passwordGenerator;
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public Serializable call() throws Exception {
        int i = 0;
        int taskId = System.identityHashCode(this) % 101;
        while (passwordGenerator.hasNext()) {
            String candidateForPassword = passwordGenerator.next();
            if (++i == 1) {
                System.out.println(taskId + " starting cracking since " + candidateForPassword);
            }
            //System.out.println(taskId + " trying " + candidateForPassword);

            byte[] digest = DigestUtils.md5(candidateForPassword.getBytes());

            if (Arrays.equals(digest, encryptedPassword)) {
                System.out.println("Found password: " + candidateForPassword);
                return candidateForPassword;
            }
        }
        System.out.println(taskId + " finishing after " + i + " tries.");
        return "";
    }

}
