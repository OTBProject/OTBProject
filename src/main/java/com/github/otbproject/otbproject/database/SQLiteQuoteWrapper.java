package com.github.otbproject.otbproject.database;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.quote.QuoteFields;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteQuoteWrapper extends DatabaseWrapper {

    private SQLiteQuoteWrapper(String path, HashMap<String, TableFields> tables) throws SQLException, ClassNotFoundException {
        super(path, tables);
    }

    public static SQLiteQuoteWrapper createDatabase(String path, HashMap<String, TableFields> tables) {
        try {
            return new SQLiteQuoteWrapper(path, tables);
        } catch (SQLException | ClassNotFoundException e) {
            App.logger.catching(e);
            return null;
        }
    }

    @Override
    public boolean removeRecord(String table, Object identifier, String fieldName) {
        PreparedStatement preparedStatement = null;
        String sql = "UPDATE " + table + " SET " + QuoteFields.TEXT + "= NULL WHERE " + fieldName + "=?";
        boolean bool = false;
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            setValue(preparedStatement, 1, identifier);
            int i = preparedStatement.executeUpdate();
            if (i > 0) {
                bool = true;
            }
            connection.commit();
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            bool = false;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
                bool = false;
            }
            lock.unlock();
        }
        return bool;
    }

    @Override
    public boolean insertRecord(String table, HashMap<String, Object> map) {
        // Try to fill empty slot
        String sql = "SELECT " + QuoteFields.ID + " FROM " + table + " WHERE " + QuoteFields.TEXT + " IS NULL ORDER BY " + QuoteFields.ID + " ASC LIMIT 1";

        lock.lock();
        try {
            connection.setAutoCommit(false);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if (rs.next()) {
                Integer id = rs.getInt(QuoteFields.ID);
                map.put(QuoteFields.ID, id);
                return updateRecord(table, id, QuoteFields.ID, map);
            }
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            return false;
        } finally {
            lock.unlock();
        }

        // Create new record otherwise
        return super.insertRecord(table, map);
    }

    public ResultSet getRandomRecord(String table) {
        String sql = "SELECT * FROM " + table + " WHERE " + QuoteFields.TEXT + " IS NOT NULL ORDER BY RANDOM() LIMIT 1";
        ResultSet rs = null;
        lock.lock();
        try {
            connection.setAutoCommit(false);
            rs = connection.createStatement().executeQuery(sql);
            connection.commit();
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return rs;
    }

    public ArrayList<Object> getNonRemovedRecordsList(String table, String key) {
        String sql = "";
        lock.lock();
        try {
            ArrayList<Object> set = new ArrayList<>();
            sql = "SELECT " + key + " FROM " + table + " WHERE " + QuoteFields.TEXT + " IS NOT NULL";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                set.add(resultSet.getString(key));
            }
            return set;
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            return null;
        } finally {
            lock.unlock();
        }
    }
}
