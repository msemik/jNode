package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.*;

import java.security.*;
import java.util.Scanner;

/**
 * Created by michal on 2016-06-12.
 */

@ContextScan("pl.edu.uj.passwordcracker")
public class Main
{
    private static final String CHARSET1 = "abcdefghijklmnoprstuwxyz";
    private static final String CHARSET2 = "abcdefghijklmnoprstuwxyzABCDEFGHIJKLMNOPRSTUWXYZ0123456789";
    private static int JOBS_SEPARATION_FACTOR;

    public static void main(String[] args)
    {
        String line;
        try(Scanner scanner = new Scanner(System.in))
        {
            System.out.println("Provide password you wish to hack");
            line = scanner.nextLine();
            System.out.println(line.length());
            if(JOBS_SEPARATION_FACTOR <= 0)
            {
                System.out.println("Provide jobs separation factor");
                JOBS_SEPARATION_FACTOR = scanner.nextInt();
            }
        }

        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        PasswordCrackerContext passwordCrackerContext = (PasswordCrackerContext) taskExecutor.getBean(PasswordCrackerContext.class);
        if(passwordCrackerContext == null)
        {
            System.err.println("Password context null");
            return;
        }
        byte[] encryptedPassword = encryptUserPassword(line);
        passwordCrackerContext.setEncryptedPassword(encryptedPassword);
        PasswordGenerator passwordGenerator = new PasswordGenerator(CHARSET1, JOBS_SEPARATION_FACTOR);
        passwordCrackerContext.setPasswordGenerator(passwordGenerator);

        PasswordCrackerCallback callback = new PasswordCrackerCallback(taskExecutor);
        taskExecutor.doAsync(() -> null, callback);
    }

    private static byte[] encryptUserPassword(String line)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(line.getBytes());
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

}
