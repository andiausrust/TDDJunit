package com.andi;

/**
 * Created by AndreasMayer on Dec, 2018
 */
public class UserInputValidator {

    public boolean isValidFullName(String fullName) {
    return FullNameValidator.isValidFullName(fullName);
}

    public boolean isValidUsername(String username) {
        return ServerUsernameValidator.isValidUsername(username);
    }
}
