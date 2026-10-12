package com.hospital.management.dto;

public class ReceptionistRegisterDto {

    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String employeeId;
    private String shift = "General Shift (09:00 - 17:00)";
    private String password;
    private String confirmPassword;

    public ReceptionistRegisterDto() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
