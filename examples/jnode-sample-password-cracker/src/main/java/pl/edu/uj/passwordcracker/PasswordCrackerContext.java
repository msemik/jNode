package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.Context;

@Context
public class PasswordCrackerContext {
    private PasswordGenerator passwordGenerator;
    private byte[] encryptedPassword;

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(byte[] encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
