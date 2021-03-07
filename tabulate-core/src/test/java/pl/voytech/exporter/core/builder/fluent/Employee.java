package pl.voytech.exporter.core.builder.fluent;

import java.math.BigDecimal;

public class Employee {
    private String firstName;
    private String lastName;
    private Integer age;
    private BigDecimal salary;
    private String id;

    public Employee(String firstName, String lastName, Integer age, BigDecimal salary, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.salary = salary;
        this.id = id;
    }

    public Employee() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public String getId() {
        return id;
    }
}
