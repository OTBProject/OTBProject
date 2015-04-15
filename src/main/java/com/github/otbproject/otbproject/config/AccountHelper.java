package com.github.otbproject.otbproject.config;

public class AccountHelper {
    public static Account getCopy(Account account) {
        Account copy = new Account();

        copy.setName(account.getName());
        copy.setPasskey(account.getPasskey());

        return copy;
    }
}
