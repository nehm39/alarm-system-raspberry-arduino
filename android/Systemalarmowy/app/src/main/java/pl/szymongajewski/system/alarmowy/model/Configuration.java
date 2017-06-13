package pl.szymongajewski.system.alarmowy.model;

/**
 * Created by Szymon Gajewski on 30.12.2015.
 */
public class Configuration {
    private int id;
    private String name;
    private String value;

    public Configuration(int id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
