package com.github.otbproject.otbproject.command;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.util.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Aliases {
    private Aliases() {}

    public static Optional<Alias> get(DatabaseWrapper db, String aliasName) {
        return db.getRecord(AliasFields.TABLE_NAME, aliasName, AliasFields.NAME,
                rs -> {
                    if (rs.isAfterLast()) {
                        return null;
                    }
                    Alias alias = new Alias();
                    alias.setName(rs.getString(AliasFields.NAME));
                    alias.setCommand(rs.getString(AliasFields.COMMAND));
                    alias.setModifyingUserLevel(UserLevel.valueOf(rs.getString(AliasFields.MODIFYING_UL)));
                    alias.setEnabled(Boolean.valueOf(rs.getString(AliasFields.ENABLED)));
                    return alias;
                });
    }

    public static List<String> getAliases(DatabaseWrapper db) {
        List<Object> list = db.getRecordsList(AliasFields.TABLE_NAME, AliasFields.NAME);
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
        return db.updateRecord(AliasFields.TABLE_NAME, map.get(AliasFields.NAME), AliasFields.NAME, map);
    }

    public static boolean exists(DatabaseWrapper db, String aliasName) {
        return db.exists(AliasFields.TABLE_NAME, aliasName, AliasFields.NAME);
    }

    public static boolean add(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.insertRecord(AliasFields.TABLE_NAME, map);
    }

    public static boolean remove(DatabaseWrapper db, String aliasName) {
        return db.removeRecord(AliasFields.TABLE_NAME, aliasName, AliasFields.NAME);
    }

    public static boolean addAliasFromObj(DatabaseWrapper db, Alias alias) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(AliasFields.NAME, alias.getName());
        map.put(AliasFields.COMMAND, alias.getCommand());
        map.put(AliasFields.MODIFYING_UL, alias.getModifyingUserLevel().name());
        map.put(AliasFields.ENABLED, String.valueOf(alias.isEnabled()));
        if (Aliases.exists(db, alias.getName())) {
            return Aliases.update(db, map);
        } else {
            return Aliases.add(db, map);
        }
    }
}
