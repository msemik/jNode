package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;
import java.security.*;

/**
 * Created by michal on 2016-06-12.
 */
public class PasswordCrackerTask implements Task {
    private MessageDigest md5;
    private PasswordGenerator passwordGenerator;
    @InjectContext
    private PasswordCrackerContext passwordCrackerContext;

    public PasswordCrackerTask(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Serializable call() throws Exception {
        byte[] encryptedPassword = passwordCrackerContext.getEncryptedPassword();
        while (passwordGenerator.hasNext()) {
            String candidateForPassword = passwordGenerator.next();
            byte[] digest = encrypt(candidateForPassword.getBytes());
            if (digest.equals(encryptedPassword)) {
                return candidateForPassword;
            }
        }
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
