package com.app.venus.shared.domain;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class PublicIdGenerator {
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int RANDOM_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String nextId(String prefix) {
        StringBuilder id = new StringBuilder(prefix).append('_');
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            id.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return id.toString();
    }
}
