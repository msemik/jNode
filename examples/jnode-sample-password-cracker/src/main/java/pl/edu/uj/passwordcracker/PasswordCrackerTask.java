package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;

/**
 * Created by michal on 2016-06-12.
 */
public class PasswordCrackerTask implements Task {
    private MessageDigest md5;
    private PasswordGenerator passwordGenerator;
    private byte[] encryptedPassword;
    @InjectContext
    private PasswordCrackerContext passwordCrackerContext;

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
            if(++i == 1){
                System.out.println(taskId + " starting cracking since " + candidateForPassword);
            }
            //System.out.println(taskId + " trying " + candidateForPassword);

            byte[] digest = encrypt(candidateForPassword.getBytes());
            if(candidateForPassword.equals("aa"))
            System.out.println(candidateForPassword + " --- " + Arrays.toString(digest) + " --- " + Arrays.toString(encryptedPassword));
            if (Arrays.equals(digest, encryptedPassword)) {
                System.out.println("Found password: " + candidateForPassword);
                return candidateForPassword;
            }
        }
        System.out.println(taskId + " finishing after " + i + " tries.");
        return null;
    }

    private byte[] encrypt(byte[] bytes) {
        if (md5 == null) {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return md5.digest(bytes);
    }
}
