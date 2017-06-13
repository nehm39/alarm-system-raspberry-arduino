package pl.szymongajewski.system.alarmowy.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Szymon Gajewski on 30.12.2015.
 */
public class Alarm {
    private int id;
    @SerializedName("start_time")
    private String startDate;
    @SerializedName("end_time")
    private String endDate;

    public Alarm(int id, String startDate, String endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
