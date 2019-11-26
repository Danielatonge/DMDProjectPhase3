package sample.Model;

public class ObjectQueryOne {
    private String doctor_id = null;
    private String first_name = null;
    private String last_name = null;
    private String date = null;

    public String getLast_name() {
        return last_name;
    }

    public String getDate() {
        return date;
    }

    public ObjectQueryOne() {
    }

    public ObjectQueryOne(String doctor_id, String first_name, String last_name, String date) {
        this.doctor_id = doctor_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.date = date;
    }

    public String getDoctor_id() {
        return doctor_id;
    }


    public String getFirst_name() {
        return first_name;
    }
}
