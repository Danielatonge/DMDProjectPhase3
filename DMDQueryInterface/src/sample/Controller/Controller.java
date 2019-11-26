package sample.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sample.Model.*;
import sample.Util.QueriesImplementation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML TableView qresult;
    @FXML TextField patientId;
    @FXML Label status;
    @FXML TextField dbname;
    @FXML TextField dbuser;
    @FXML PasswordField dbpassword;
    @FXML Tab tbpostgresql;
    @FXML Tooltip tooltip1;
    @FXML Tooltip tooltip2;
    @FXML Tooltip tooltip3;
    @FXML Tooltip tooltip4;
    @FXML Tooltip tooltip5;

    QueriesImplementation queries;
    Connection conn;
    private ObservableList data;
    public void initialize() {
        qresult.setPlaceholder(new Label("No rows to display"));
        qresult.getColumns().clear();
        tbpostgresql.setDisable(true);
    }

    private void stabilize() throws SQLException {
        String user = dbuser.getText();
        String password = dbpassword.getText();
        String databaseName = dbname.getText();
        String url = "jdbc:postgresql://localhost:5432/" + databaseName + "?allowMultiQueries=true";
        try {
        conn = DriverManager.getConnection(url, user, password);
        queries = new QueriesImplementation(conn);
        status.setText("Database " + databaseName + " opened successfully");
        } catch (SQLException e){
            status.setText("Wrong input Credentials | Try again");
            dbuser.clear();
            dbpassword.clear();
            dbname.clear();
        }
    }

    @FXML
    private void onClickConnect() throws SQLException {
        stabilize();
        tbpostgresql.setDisable(false);
        tooltip1.setText("Find all the possible doctors that match given description");
        tooltip2.setText("Find for each doctor, the total and average number of appointments\n" +
                "in each time slot of the week during the last year");
        tooltip3.setText("Find patients who visited the hospital every week, at least twice a week");
        tooltip4.setText("What would be the income of the hospital in the previous month");
        tooltip5.setText("Find out the doctors who have attended to at least five patients per year for the last 10 years");
    }

    private void updateTableView(int query_number, ResultSet resultSet) throws SQLException {
        data = getInitialTableData(query_number,resultSet);

        qresult.setItems(data);
        List<TableColumn<String, ObjectQueryTwo>> columnList = new ArrayList<>();
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {

            String viewExample = resultSet.getMetaData().getColumnName(i + 1);
            columnList.add(i, new TableColumn<>(viewExample));
            columnList.get(i).setCellValueFactory(new PropertyValueFactory<>(viewExample));
        }

        qresult.getColumns().clear();
        for (int i = 0; i < columnList.size(); i++) {
            qresult.getColumns().addAll(columnList.get(i));
        }

    }

    private ObservableList getInitialTableData(int query_number, ResultSet resultSet) throws SQLException {
        List list = new ArrayList();

        switch(query_number) {
            case 1:
                while (resultSet.next()) {
                    ObjectQueryOne objectQueryOne = new ObjectQueryOne(resultSet.getString(1), resultSet.getString(2),
                            resultSet.getString(3), resultSet.getString(4));
                    list.add(objectQueryOne);
                }
                break;
            case 2:
                while (resultSet.next()) {
                    ObjectQueryTwo objectQueryTwo = new ObjectQueryTwo(resultSet.getString(1), resultSet.getString(2),
                            resultSet.getString(3), resultSet.getString(4));
                    list.add(objectQueryTwo);
                }
                break;
            case 3:
                while (resultSet.next()) {
                    ObjectQueryThree objectQueryThree = new ObjectQueryThree(resultSet.getString(1), resultSet.getString(2));
                    list.add(objectQueryThree);
                }
                break;
            case 4:
                while (resultSet.next()) {
                    ObjectQueryFour objectQueryFour = new ObjectQueryFour(resultSet.getString(1));
                    list.add(objectQueryFour);
                }
                break;
            case 5:
                while (resultSet.next()) {
                    ObjectQueryFive objectQueryFive = new ObjectQueryFive(resultSet.getString(1), resultSet.getString(2));
                    list.add(objectQueryFive);
                }
                break;
        }

        ObservableList data = FXCollections.observableList(list);
        return data;
    }

    public void onClickQuery1() throws SQLException {
        String patient = patientId.getText();
        ResultSet resultSet = queries.query_one(patient);
        updateTableView(1,resultSet);
    }

    public void onClickQuery2() throws SQLException {
        ResultSet resultSet = queries.query_two();
        updateTableView(2,resultSet);
    }

    public void onClickQuery3() throws SQLException {
        ResultSet resultSet = queries.query_three();
        updateTableView(3,resultSet);
    }

    public void onClickQuery4() throws SQLException {
        ResultSet resultSet = queries.query_four();
        updateTableView(4,resultSet);
    }

    public void onClickQuery5() throws SQLException {
        ResultSet resultSet = queries.query_five();
        updateTableView(5,resultSet);
    }
}
