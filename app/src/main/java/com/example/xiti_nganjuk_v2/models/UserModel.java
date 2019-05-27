package com.example.xiti_nganjuk_v2.models;

public class UserModel {
    private String userId;
    private String birthDate;
    private String firstName;
    private String lastName;
    private String gender;
    private String profilePic;

    public UserModel(String userId, String birthDate, String firstName, String lastName, String gender, String profilePic) {
        this.userId = userId;
        this.birthDate = birthDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.profilePic = profilePic;
    }

    public String getUserId() {
        return userId;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
