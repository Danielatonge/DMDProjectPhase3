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
        String sql = "SELECT DISTINCT doctor_id, first_name, last_name, date\n" +
                "    FROM doctors d\n" +
                "             INNER JOIN appointment p ON d.ssn = p.doctor_id\n" +
                "    WHERE patient_id = " + patient_id +
                "      AND date = (SELECT a.date from appointment a where a.date < now() and a.patient_id = "+patient_id+" ORDER BY date DESC LIMIT 1)\n" +
                "      AND (first_name similar to '[ML][a-z]+'\n" +
                "        OR last_name similar to '[ML][a-z]+'\n" +
                "               AND NOT (first_name similar to '[ML][a-z]+' AND last_name similar to '[ML][a-z]+'));";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_two() throws SQLException {
        String sql = "CREATE OR REPLACE FUNCTION weekOf(d timestamp)\n" +
                "    RETURNS integer AS\n" +
                "$func$\n" +
                "BEGIN\n" +
                "    RETURN EXTRACT(WEEK FROM d);\n" +
                "END\n" +
                "$func$ LANGUAGE plpgsql;\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION yearOf(d timestamp)\n" +
                "    RETURNS integer AS\n" +
                "$func$\n" +
                "BEGIN\n" +
                "    RETURN EXTRACT(YEAR FROM d);\n" +
                "END\n" +
                "$func$ LANGUAGE plpgsql;\n" +
                "\n" +
                "CREATE OR REPLACE VIEW doctor_stats_helper AS\n" +
                "SELECT a1.doctor_id,\n" +
                "       weekOf(a1.date) AS week,\n" +
                "       a1.slot\n" +
                "FROM appointment a1\n" +
                "WHERE a1.date >= current_date - INTERVAL '1 year'\n" +
                "  and a1.date <= current_date\n" +
                "GROUP BY a1.doctor_id, weekOf(a1.date), a1.slot;\n" +
                "\n" +
                "CREATE OR REPLACE VIEW doctors_weeks_stats AS\n" +
                "SELECT *,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment a\n" +
                "        WHERE h.doctor_id = a.doctor_id\n" +
                "          and weekOf(a.date) = h.week\n" +
                "          and a.slot = h.slot\n" +
                "          and a.date >= current_date - INTERVAL '1 year'\n" +
                "          and a.date <= current_date) as appointments\n" +
                "FROM doctor_stats_helper h;\n" +
                "SELECT doctor_id, slot, sum(appointments) / 52 as avg, sum(appointments)\n" +
                "                  FROM doctors_weeks_stats\n" +
                "                  GROUP BY doctor_id, slot;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_three() throws SQLException {
        String sql =               "CREATE OR REPLACE VIEW patient_stats_helper AS\n" +
                "SELECT patient_id,\n" +
                "       weekOf(date) AS week\n" +
                "FROM Appointment\n" +
                "WHERE date >= current_date - INTERVAL '1 month'\n" +
                "  and date <= current_date\n" +
                "GROUP BY patient_id, week;\n" +
                "\n" +
                "CREATE OR REPLACE VIEW patient_weeks_stats AS\n" +
                "SELECT *,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment a\n" +
                "        WHERE h.patient_id = a.patient_id\n" +
                "          AND date >= current_date - INTERVAL '1 month'\n" +
                "          and date <= current_date\n" +
                "          AND weekOf(a.date) = h.week) AS appointments\n" +
                "FROM patient_stats_helper h;\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION weeks_count_last_month()\n" +
                "    RETURNS integer AS\n" +
                "$func$\n" +
                "BEGIN\n" +
                "    return (select count(*)\n" +
                "            from (select distinct extract(week from generate_series(current_date - '1 month'::interval, current_date,\n" +
                "                                                                    '1 day'::interval))) as weeks);\n" +
                "END\n" +
                "$func$ LANGUAGE plpgsql;\n" +
                "\n" +
                "SELECT DISTINCT patient_id, name as full_name\n" +
                "                  FROM patient_weeks_stats stats\n" +
                "                           INNER JOIN patient p on p.medical_insurance_id = stats.patient_id\n" +
                "                  WHERE (SELECT count(*)\n" +
                "                         from patient_weeks_stats s\n" +
                "                         where s.patient_id = stats.patient_id\n" +
                "                           and s.appointments >= 2) = weeks_count_last_month();";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_four() throws SQLException {
        String sql = "CREATE OR REPLACE VIEW last_month_appointments AS\n" +
                "SELECT  distinct A.patient_id,\n" +
                "       (EXTRACT(year FROM age(current_date,P.birthday)))::integer as age,\n" +
                "       (SELECT count(A1.patient_id)\n" +
                "        FROM appointment A1\n" +
                "        WHERE A1.patient_id = A.patient_id\n" +
                "           AND date >= current_date - INTERVAL '1 month' and date <= current_date ) as appointments\n" +
                "FROM appointment A, patient P\n" +
                "WHERE P.medical_insurance_id = A.patient_id AND date >= current_date - INTERVAL '1 month' and date <= current_date;\n" +
                "\n" +
                "CREATE OR REPLACE VIEW income AS\n" +
                "    SELECT\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments\n" +
                "    WHERE age < 50 AND appointments < 3)*200 as value1,\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments\n" +
                "    WHERE age < 50  and appointments >= 3)*250 as value2,\n" +
                "    (SELECT  count(*)\n" +
                "    FROM last_month_appointments L\n" +
                "    WHERE age >= 50  and appointments < 3)*400 as value3,\n" +
                "    (SELECT count(*)\n" +
                "    FROM last_month_appointments L\n" +
                "    WHERE age >= 50 and appointments >= 3)*500 as value4;\n" +
                "\n" +
                "    SELECT sum(value1 + value2 + value3 + value4)\n" +
                "    FROM income;";
        return executePrintReturn(sql);
    }

    @Override
    public ResultSet query_five() throws SQLException {
        String sql ="CREATE OR REPLACE VIEW doctor_year_report_helper AS\n" +
                "SELECT doctor_id,\n" +
                "       yearOf(date) AS year\n" +
                "FROM Appointment\n" +
                "WHERE date >= current_date - INTERVAL '10 year'\n" +
                "  and date <= current_date;\n" +
                "\n" +
                "with stats AS (SELECT *,\n" +
                "       (SELECT count(*)\n" +
                "        FROM appointment a\n" +
                "        WHERE h.doctor_id = a.doctor_id\n" +
                "          AND date >= current_date - INTERVAL '10 year'\n" +
                "          and date <= current_date\n" +
                "          AND yearOf(a.date) = h.year) AS year_appointments,\n" +
                "\n" +
                "       (SELECT count(a.doctor_id)\n" +
                "       FROM appointment a\n" +
                "       WHERE a.doctor_id = h.doctor_id\n" +
                "         AND date >= current_date - INTERVAL '10 year') as decade_appointments\n" +
                "FROM doctor_year_report_helper h)\n" +
                "\n" +
                "SELECT distinct doctor_id, decade_appointments\n" +
                "from stats p\n" +
                "where not exists(select doctor_id from stats p2 where p2.doctor_id = p.doctor_id and p2.year_appointments < 5) and p.decade_appointments >= 100;";
        return executePrintReturn(sql);
    }
}
