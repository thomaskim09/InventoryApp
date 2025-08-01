package com.inventory;

public class Supplier {
    private int id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;

    public Supplier(int id, String name, String contactPerson, String email, String phone) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
    }

    // --- Getters and Setters ---
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
