package sample.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueriesImplementation implements Queries {

    private Connection connection;

    QueriesImplementation(Connection connection) {
        this.connection = connection;
    }

    private ResultSet executePrintReturn(String sql) throws SQLException {
        Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        boolean hasMoreResultSets = s.execute(sql);
        ResultSet resultSet = null;
        while ( hasMoreResultSets || s.getUpdateCount() != -1 ) {
            if (hasMoreResultSets) {
                resultSet = s.getResultSet();
                for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                    System.out.print(resultSet.getMetaData().getColumnName(i + 1) + " ");
                }
                System.out.println();
                while (resultSet.next()) {
                    for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                        System.out.print(resultSet.getString(i + 1) + " ");
                    }
                    System.out.println();
                }
                resultSet.beforeFirst();
                return resultSet;
            }
            hasMoreResultSets = s.getMoreResults();
        }
        return null;
    }

    @Override
    public ResultSet query_one(String patient_id) throws SQLException {
        String sql = "PREPARE Query1 AS\n" +
                "    SELECT doctor_id, name\n" +
                "    FROM doctors d\n" +
                "             INNER JOIN appointment p ON d.ssn = p.doctor_id\n" +
                "    WHERE patient_id = $1\n" +
                "      AND d.name LIKE '[ML][a-z]+';";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_two() throws SQLException {
        String sql = "CREATE FUNCTION weekOf(d timestamp)\n" +
                "    RETURNS integer AS\n" +
                "$func$\n" +
                "BEGIN\n" +
                "    RETURN EXTRACT(WEEK FROM d);\n" +
                "END\n" +
                "$func$ LANGUAGE plpgsql;\n" +
                "\n" +
                "CREATE VIEW doctors_weeks_stats AS\n" +
                "SELECT doctor_id,\n" +
                "       weekOf(date)                          AS week,\n" +
                "       slot,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment a\n" +
                "        WHERE doctor_id = a.doctor_id\n" +
                "          and weekOf(a.date) = weekOf(date) and slot = a.slot) as appointments\n" +
                "FROM appointment\n" +
                "WHERE date >= current_date - INTERVAL '1 year'\n" +
                "GROUP BY doctor_id, week, slot;\n" +
                "\n" +
                "PREPARE Query2 AS SELECT doctor_id, slot, avg(appointments), sum(appointments)\n" +
                "                  FROM doctors_weeks_stats\n" +
                "                  GROUP BY doctor_id, slot;;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_three() throws SQLException {
        String sql = "CREATE VIEW patient_weeks_stats AS\n" +
                "SELECT patient_id,\n" +
                "       weekOf(date)                          AS week,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment a\n" +
                "        WHERE patient_id = a.patient_id\n" +
                "          AND weekOf(a.date) = weekOf(date)) AS appointments\n" +
                "FROM Appointment\n" +
                "WHERE date >= current_date - INTERVAL '1 month'\n" +
                "GROUP BY patient_id, week;\n" +
                "\n" +
                "PREPARE Query3 AS SELECT patient_id\n" +
                "                  FROM patient_weeks_stats\n" +
                "                  WHERE not exists(SELECT s.week\n" +
                "                                   from patient_weeks_stats s\n" +
                "                                   where s.patient_id = patient_id\n" +
                "                                     and s.appointments < 2);";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_four() throws SQLException {
        String sql = "CREATE VIEW last_month_appointments AS\n" +
                "SELECT patient_id,\n" +
                "       (EXTRACT(year FROM age(current_date,birthday)))::integer as age,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment A\n" +
                "        WHERE A.patient_id = patient_id) as appointments\n" +
                "FROM appointment, patient\n" +
                "WHERE date >= current_date - INTERVAL '1 month';\n" +
                "\n" +
                "CREATE VIEW income AS\n" +
                "    SELECT\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments\n" +
                "    WHERE age < 50 and appointments < 3)*200 as value1,\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments\n" +
                "    WHERE age < 50  and appointments >= 3)*250 as value2,\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments L\n" +
                "    WHERE age >= 50  and appointments < 3)*400 as value3,\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments L\n" +
                "    WHERE age >= 50 and appointments >= 3)*500 as value4\n" +
                "    FROM last_month_appointments;\n" +
                "\n" +
                "PREPARE Query4 AS\n" +
                "    SELECT sum(value1 + value2 + value3 + value4)\n" +
                "    FROM income;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_five() throws SQLException {
        String sql = "CREATE OR REPLACE FUNCTION valid (id integer)\n" +
                "       RETURNS boolean AS\n" +
                "$$\n" +
                "DECLARE counter integer := 0;\n" +
                "BEGIN\n" +
                "    loop\n" +
                "        exit when counter = 11;\n" +
                "        if((SELECT count(id)\n" +
                "            FROM appointment\n" +
                "            WHERE date_part('year', current_date)\n" +
                "                - date_part('year', date) == counter) < 5) THEN\n" +
                "        RETURN FALSE;\n" +
                "        END IF;\n" +
                "        counter := counter + 1;\n" +
                "    end loop;\n" +
                "    RETURN TRUE;\n" +
                "END\n" +
                "$$ LANGUAGE plpgsql;\n" +
                "\n" +
                "\n" +
                "PREPARE Query5 AS\n" +
                "    SELECT doctor_id\n" +
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
