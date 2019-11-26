package sample.Model;

public class ObjectQueryFive {
    private String doctor_id = null;
    private String decade_appointments = null;

    public ObjectQueryFive() {
    }

    public ObjectQueryFive(String doctorId, String appointment){
        this.doctor_id = doctorId;
        this.decade_appointments = appointment;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public String getDecade_appointments() {
        return decade_appointments;
    }
}
