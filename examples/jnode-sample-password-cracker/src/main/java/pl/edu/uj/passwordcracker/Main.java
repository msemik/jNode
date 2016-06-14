package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.*;

import java.util.Scanner;

import static org.apache.commons.codec.digest.DigestUtils.md5;

/**
 * Created by michal on 2016-06-12.
 */

@ContextScan("pl.edu.uj.passwordcracker")
public class Main {
    private static final String CHARSET1 = "abcdefghijklmnoprstuwxyz";
    private static final String CHARSET2 = "abcdefghijklmnoprstuwxyzABCDEFGHIJKLMNOPRSTUWXYZ0123456789";
    private static int jobsSeparationFactor;

    public static void main(String[] args) {
        String line;
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Provide password you wish to hack");
            line = scanner.nextLine();
            System.out.println(line.length());
            if (jobsSeparationFactor <= 0) {
                System.out.println("Provide jobs separation factor");
                jobsSeparationFactor = scanner.nextInt();
            }
        }

        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        PasswordCrackerContext passwordCrackerContext = (PasswordCrackerContext) taskExecutor.getBean(PasswordCrackerContext.class);
        if (passwordCrackerContext == null) {
            System.err.println("Password context null");
            return;
        }

        byte[] encryptedPassword = md5(line.getBytes());
        passwordCrackerContext.setEncryptedPassword(encryptedPassword);
        PasswordGenerator passwordGenerator = new PasswordGenerator(CHARSET1, jobsSeparationFactor);
        passwordCrackerContext.setPasswordGenerator(passwordGenerator);

        PasswordCrackerCallback callback = new PasswordCrackerCallback(taskExecutor);
        taskExecutor.doAsync(() -> null, callback);
    }

}
