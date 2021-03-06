package Model;

public class User {

    /*properties*/
    private int id;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;

    /*getters and setters*/
    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", age=" + age + ", gender=" +
                gender + "]";
    }

    /**
     * @param leaf_index the index of the leaf node
     * @param newValue   the new value of the leaf
     */
    public void set_value(int leaf_index, Object newValue) {
        switch (leaf_index) {
            case 0:
                setFirstName(newValue + "");
                break;
            case 1:
                setLastName(newValue + "");
                break;
            case 2:
                setGender(newValue + "");
                break;
            case 3:
                setAge((int) newValue);
                break;
        }
    }
}
