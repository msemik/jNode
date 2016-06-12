package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.*;
import pl.edu.uj.jnode.userlib.*;

import java.security.*;
import java.util.Scanner;

/**
 * Created by michal on 2016-06-12.
 */

@ContextScan("pl.edu.uj.passwordcracker")
public class Main {
    private static final String CHARSET1 = "abcdefghijklmnoprstuwxyz";
    private static final String CHARSET2 = "abcdefghijklmnoprstuwxyzABCDEFGHIJKLMNOPRSTUWXYZ0123456789";
    private static final int JOBS_SEPARATION_FACTOR = 6;

    public static void main(String[] args) {
        System.out.println("Provide password you wish to hack");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        byte[] encryptedPassword = encryptUserPassword(line);
        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        PasswordCrackerContext passwordCrackerContext = (PasswordCrackerContext) taskExecutor.getBean(PasswordCrackerContext.class);
        PasswordGenerator passwordGenerator = new PasswordGenerator(CHARSET1, JOBS_SEPARATION_FACTOR);
        passwordCrackerContext.setEncryptedPassword(encryptedPassword);
        passwordCrackerContext.setPasswordGenerator(passwordGenerator);

        PasswordCrackerCallback callback = new PasswordCrackerCallback(taskExecutor);
        taskExecutor.doAsync(() -> null, callback);
    }

    private static byte[] encryptUserPassword(String line) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(line.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
