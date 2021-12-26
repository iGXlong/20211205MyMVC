package domain;

public class User {

    private String name;
    private Integer pass;

    public User() {
    }

    public User(String name, int pass) {
        this.name = name;
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pass=" + pass +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPass() {
        return pass;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }
}
