package sample.Model;

public class ObjectQueryThree {
    private String patient_id = null;
    private String full_name = null;

    public ObjectQueryThree() {
    }

    public ObjectQueryThree(String patient_id, String full_name) {
        this.patient_id = patient_id;
        this.full_name = full_name;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public String getFull_name() {
        return full_name;
    }
}
