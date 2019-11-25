package sample.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueriesImplementation implements Queries {

    private Connection connection;

    public QueriesImplementation(Connection connection) {
        this.connection = connection;
    }

    private ResultSet executePrintReturn(String sql) throws SQLException {
        Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        boolean hasMoreResultSets = s.execute(sql);
        ResultSet resultSet = null;
        while ( hasMoreResultSets || s.getUpdateCount() != -1 ) {
            if (hasMoreResultSets) {
                resultSet = s.getResultSet();
                return resultSet;
            }
            hasMoreResultSets = s.getMoreResults();
        }
        return null;
    }

    @Override
    public ResultSet query_one(String patient_id) throws SQLException {
        String sql = "SELECT doctor_id, name\n" +
                "    FROM doctors d\n" +
                "             INNER JOIN appointment p ON d.ssn = p.doctor_id\n" +
                "    WHERE patient_id = " + patient_id +
                "      AND d.name LIKE '[ML][a-z]+';";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_two() throws SQLException {
        String sql = "SELECT doctor_id, slot, avg(appointments), sum(appointments)\n" +
                "                  FROM doctors_weeks_stats\n" +
                "                  GROUP BY doctor_id, slot;;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_three() throws SQLException {
        String sql =               "SELECT patient_id\n" +
                "                  FROM patient_weeks_stats\n" +
                "                  WHERE not exists(SELECT s.week\n" +
                "                                   from patient_weeks_stats s\n" +
                "                                   where s.patient_id = patient_id\n" +
                "                                     and s.appointments < 2);";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_four() throws SQLException {
        String sql = "    SELECT sum(value1 + value2 + value3 + value4)\n" +
                "    FROM income;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_five() throws SQLException {
        String sql ="SELECT doctor_id\n" +
                "    FROM appointment\n" +
                "    WHERE  (SELECT count(*)\n" +
                "            FROM appointment A\n" +
                "            WHERE A.doctor_id = doctor_id\n" +
                "            AND date >= current_date - INTERVAL '10 year') >= 100\n" +
                "            AND not exists(SELECT A1.doctor_id\n" +
                "                FROM appointment A1\n" +
                "                WHERE valid(A1.doctor_id) = false);";
        return executePrintReturn(sql);
    }
}
