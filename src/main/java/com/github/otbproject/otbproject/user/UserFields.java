package com.github.otbproject.otbproject.user;

import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;
import java.util.HashSet;

public class UserFields {
    public static final String NICK = "nick";
    public static final String USER_LEVEL = "userLevel";

    public static final String TABLE_NAME = "tblUsers";
    public static final HashSet<String> PRIMARY_KEYS = new HashSet<>();

    static {
        PRIMARY_KEYS.add(NICK);
    }

    public static TableFields getTableFields() {
        HashMap<String,String> userFields = new HashMap<>();
        userFields.put(NICK, DataTypes.STRING);
        userFields.put(USER_LEVEL, DataTypes.STRING);
        return new TableFields(userFields, PRIMARY_KEYS);
    }
}
