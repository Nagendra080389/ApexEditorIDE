package com.forceFilesEditor.service;

import com.forceFilesEditor.model.Person;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Service is responsible For checking username and Password.
 */
@Service
public class LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    private Map<String, Person> personMap;
    private MessageDigest messageDigest;

    public LoginService() {
        this.personMap = new ConcurrentHashMap<>();
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public Hash addInPersonMap(Person person){
        byte[] digest = messageDigest.digest((person.getUsername()+person.getPassword()).getBytes());
        String b64url = Base64.encodeBase64URLSafeString(digest);
        Hash hash = new Hash(b64url);
        personMap.put(hash.getHash(), person);
        LOGGER.info("Person map populated with username: "+ person.getUsername());
        return hash;
    }

    /**
     *
     * @param hash
     * @return
     */
    public Person getPersonByHash(String hash) {
        return personMap.get(hash);
    }

    /**
     *
     * @param person
     * @return
     */
    public Boolean checkUsername(Person person) {

        // Implement Here.
        return true;
    }
}
