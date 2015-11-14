package com.github.otbproject.otbproject.user;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.util.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Users {
    private Users() {}

    public static Optional<User> get(DatabaseWrapper db, String userNick) {
        return db.getRecord(UserFields.TABLE_NAME, userNick, UserFields.NICK,
                rs -> {
                    if (rs.isAfterLast()) {
                        return null;
                    }
                    User user = new User();
                    user.setNick(rs.getString(UserFields.NICK));
                    user.setUserLevel(UserLevel.valueOf(rs.getString(UserFields.USER_LEVEL)));
                    return user;
                });
    }

    public static List<String> getUsers(DatabaseWrapper db) {
        List<Object> list = db.getRecordsList(UserFields.TABLE_NAME, UserFields.NICK);
        if (list == null) {
            return null;
        }
        try {
            return list.stream().map(key -> (String) key).collect(Collectors.toList());
        } catch (ClassCastException e) {
            App.logger.catching(e);
            Watcher.logException();
            return null;
        }
    }

    public static boolean update(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.updateRecord(UserFields.TABLE_NAME, map.get(UserFields.NICK), UserFields.NICK, map);
    }

    public static boolean exists(DatabaseWrapper db, String userName) {
        return db.exists(UserFields.TABLE_NAME, userName, UserFields.NICK);
    }

    public static boolean add(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.insertRecord(UserFields.TABLE_NAME, map);
    }

    public static boolean remove(DatabaseWrapper db, String userName) {
        return db.removeRecord(UserFields.TABLE_NAME, userName, UserFields.NICK);
    }

    public static boolean addUserFromObj(DatabaseWrapper db, User user) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(UserFields.NICK, user.getNick());
        map.put(UserFields.USER_LEVEL, user.getUserLevel().name());

        if (exists(db, user.getNick())) {
            return update(db, map);
        } else {
            return add(db, map);
        }
    }
}
