package org.example.auth;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *  The AuthService class provides authentication services for users.
 */
public class AuthService {

    /**
     * Checks if a user exists in the authentication file
     * @param usernameToCheck the username to check
     * @return true if the user exists, false otherwise
     * @throws IOException if an I/O error happens
     */
    public boolean checkIfUserExists(String usernameToCheck) throws IOException {
        String filePath = "User_Profile.txt";
        Path path = Paths.get(filePath);
        List<String> lineData = Files.readAllLines(path);
        for(int i=1;i<lineData.size();i++){
            String currLine = lineData.get(i);
            String[] parts = currLine.split("\\|\\|");
            if(parts[0].equals(usernameToCheck)){
                return true;
            }
        }
        return false;
    }


    /**
     * Hashes a password using the MD5 algorithm
     * @param password password the password to hash
     * @return the hashed password
     */
    public String hashPassword(String password){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for(byte by:digest){
                stringBuilder.append(String.format("%02x",by));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Checks if a password is correct for a given user.
     * @param userName the username to check
     * @param password the password to check
     * @return true if the password is correct, false otherwise
     * @throws IOException if an I/O error happens
     */
    public boolean checkIfPasswordCorrect(String userName,String password) throws IOException {
        String filePath = "User_Profile.txt";
        Path path = Paths.get(filePath);
        System.out.println(path);
        List<String> lineData = Files.readAllLines(path);
        for(int i=1;i<lineData.size();i++){
            String currLine = lineData.get(i);
            String[] parts = currLine.split("\\|\\|");
            if(parts[0].equals(userName)){
                String originalPassword = parts[1];
                if(originalPassword.equals(hashPassword(password))){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a security answer is correct for a given user.
     * @param userName the username to check
     * @param secAns1 the security answer to check
     * @return true if the security answer is correct, false otherwise
     * @throws IOException if an I/O error happens
     */
    public boolean checkIfSecurityAnswerCorrect(String userName,String secAns1) throws IOException {
        String filePath = "User_Profile.txt";
        Path path = Paths.get(filePath);
        List<String> lineData = Files.readAllLines(path);
        for(int i=1;i<lineData.size();i++){
            String currLine = lineData.get(i);
            String[] parts = currLine.split("\\|\\|");
            if(userName.equals(parts[0])){
                return parts[3].equalsIgnoreCase(secAns1);
            }
        }
        return false;
    }
}
