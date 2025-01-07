package org.example.auth;

import org.example.Logs.GeneralLogsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/**
 * The AuthPage class provides a authentication interface.
 */
public class AuthPage {
    Scanner sc;
    AuthService authService;
    public AuthPage(){
        this.sc = new Scanner(System.in);
        authService = new AuthService();
    }

    /**
     * Display auth page
     * @throws IOException exception
     */
    public String displayAuthPage() throws IOException {
        while (true) {
            System.out.println("Welcome to TinyDB");
            System.out.println("-----------------");
            System.out.println("1. Sign Up");
            System.out.println("2. Sign In");
            System.out.println("3. Exit");
            System.out.println("Choose an option:");
            int option = sc.nextInt();
            switch (option) {
                case 1:
                    signUpUser();
                    break;
                case 2:
                    String userName = loginUser();
                    if(userName!=null){
                        return userName;
                    }
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice");

            }
        }
    }

    /**
     * Handle sign-up process for a new user
     * @throws IOException exception
     */
    private void signUpUser() throws IOException {
        System.out.println("Please enter your username:");
        String userName = sc.next();
        System.out.println("Please enter your password");
        String password = sc.next();
        if(!authService.checkIfUserExists(userName)){
            System.out.println("Please enter your security question");
            String secQues1 = sc.next();
            System.out.println("Please enter your security answer");
            String secAns1 = sc.next();
            String hashedPassword = authService.hashPassword(password);
            writeIntoAuthFile(userName,hashedPassword,secQues1,secAns1);
            GeneralLogsService.logUserRegistered(userName);
        }else{
            System.out.println("User already exists");
        }
    }

    /**
     * Handle login process for an existing user
     * @return true if login is successful, false otherwise
     * @throws IOException exception
     */
    private String loginUser() throws IOException {
        System.out.println("Please enter your username:");
        String userName = sc.next();
        System.out.println("Please enter your password");
        String password = sc.next();
        if(!authService.checkIfUserExists(userName)){
            System.out.println("The user does not exist with the provided username");
            return null;
        }
        if(!authService.checkIfPasswordCorrect(userName,password)){
            System.out.println("User Password is wrong");
            return null;
        }
        System.out.println("Please enter your security answer");
        String secAns1 = sc.next();

        if(!authService.checkIfSecurityAnswerCorrect(userName,secAns1)){
            System.out.println("Security answer is wrong");
            return null;
        }
        GeneralLogsService.logUserLoggedIn(userName);
        return userName;

    }

    /**
     * Write user's authentication information to a file
     * @param userName the user's username
     * @param password the user's hashed password
     * @param secQues1 the user's security question
     * @param secAns1 the user's security answer
     * @return true if write operation is successful
     */
    private boolean writeIntoAuthFile(String userName,String password,String secQues1,String secAns1){
        String filePath = "User_Profile.txt";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(userName).append("||");
        stringBuilder.append(password).append("||");
        stringBuilder.append(secQues1).append("||");
        stringBuilder.append(secAns1);
        Path path = Paths.get(filePath);
        try {
            Files.write(path,stringBuilder.toString().getBytes("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
