package domain;

public class Atmuser {

    private String name;
    private String password;

    public Atmuser() {
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Atmuser(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
