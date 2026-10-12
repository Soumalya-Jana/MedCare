package com.hospital.management.dto;

public class DoctorRegisterDto {

    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String gender = "Male";
    private String specialization;
    private String qualification;
    private Double consultationFee = 500.0;
    private String availableDays = "Mon,Tue,Wed,Thu,Fri";
    private String availableTimeStart = "09:00";
    private String availableTimeEnd = "17:00";
    private String address;
    private String password;
    private String confirmPassword;

    public DoctorRegisterDto() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getAvailableTimeStart() { return availableTimeStart; }
    public void setAvailableTimeStart(String availableTimeStart) { this.availableTimeStart = availableTimeStart; }

    public String getAvailableTimeEnd() { return availableTimeEnd; }
    public void setAvailableTimeEnd(String availableTimeEnd) { this.availableTimeEnd = availableTimeEnd; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
