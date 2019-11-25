package sample.Model;

public class ObjectQueryOne {
    private String doctor_id = null;
    private String name = null;

    public ObjectQueryOne() {
    }

    public ObjectQueryOne(String doctor_id, String name) {
        this.doctor_id = doctor_id;
        this.name = name;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public String getName() {
        return name;
    }
}
