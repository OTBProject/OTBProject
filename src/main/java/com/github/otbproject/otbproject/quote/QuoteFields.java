package com.github.otbproject.otbproject.quote;

import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;
import java.util.HashSet;

public class QuoteFields {
    public static final String ID = "id";
    public static final String TEXT = "text";

    public static final String TABLE_NAME = "tblQuotes";
    public static final HashSet<String> PRIMARY_KEYS = new HashSet<>();

    static {
        PRIMARY_KEYS.add(ID);
    }

    public static TableFields getTableFields() {
        HashMap<String,String> quoteFields = new HashMap<>();
        quoteFields.put(ID, DataTypes.INTEGER);
        quoteFields.put(TEXT, DataTypes.STRING);
        return new TableFields(quoteFields, PRIMARY_KEYS);
    }
}
