package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.Context;

/**
 * Created by michal on 2016-06-12.
 */
@Context
public class PasswordCrackerContext {
    private PasswordGenerator passwordGenerator;
    private byte[] encryptedPassword;

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    public byte[] getEncryptedPassword() {
        return new byte[0];
    }

    public void setEncryptedPassword(byte[] encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
