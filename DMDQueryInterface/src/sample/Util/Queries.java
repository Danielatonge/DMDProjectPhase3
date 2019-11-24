package sample.Util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Queries {
    ResultSet query_one(String patient_id) throws SQLException;

    ResultSet query_two() throws SQLException;

    ResultSet query_three() throws SQLException;

    ResultSet query_four() throws SQLException;

    ResultSet query_five() throws SQLException;
}
