package com.github.otbproject.otbproject.database;

import com.github.otbproject.otbproject.command.AliasFields;
import com.github.otbproject.otbproject.command.CommandFields;
import com.github.otbproject.otbproject.command.scheduler.SchedulerFields;
import com.github.otbproject.otbproject.quote.QuoteFields;
import com.github.otbproject.otbproject.user.UserFields;

import java.util.HashMap;

public class DatabaseHelper {
    private DatabaseHelper() {}

    /**
     * @return a HashMap used to create all the tables by the DatabaseWrapper.
     * Tables are hard-coded into the method.
     */
    public static HashMap<String, TableFields> getMainTablesHashMap() {
        HashMap<String, TableFields> tables = new HashMap<>();
        tables.put(CommandFields.TABLE_NAME, CommandFields.getTableFields());
        tables.put(AliasFields.TABLE_NAME, AliasFields.getTableFields());
        tables.put(UserFields.TABLE_NAME, UserFields.getTableFields());
        tables.put(SchedulerFields.TABLE_NAME, SchedulerFields.getTableFields());
        //tables.put(FilterFields.TABLE_NAME, FilterFields.getTableFields());
        //tables.put(FilterGroupFields.TABLE_NAME, FilterGroupFields.getTableFields());
        return tables;
    }

    public static HashMap<String, TableFields> getQuoteTablesHashMap() {
        HashMap<String, TableFields> tables = new HashMap<>();
        tables.put(QuoteFields.TABLE_NAME, QuoteFields.getTableFields());
        return tables;
    }
}
