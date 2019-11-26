package sample.Model;

public class ObjectQueryTwo {
    private String doctor_id = null;
    private String slot = null;
    private String avg = null;
    private String sum = null;

    public ObjectQueryTwo() {
    }

    public ObjectQueryTwo(String doctorId, String slot, String average, String sum){
        this.doctor_id = doctorId;
        this.slot = slot;
        this.avg = average.substring(0, 7);
        this.sum = sum;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public String getSlot() {
        return slot;
    }

    public String getAvg() {
        return avg;
    }

    public String getSum() {
        return sum;
    }
}
