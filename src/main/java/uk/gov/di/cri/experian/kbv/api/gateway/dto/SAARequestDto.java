package uk.gov.di.cri.experian.kbv.api.gateway.dto;

import java.util.StringJoiner;

public class SAARequestDto {

    private String title;
    private String firstName;
    private String initials;
    private String surname;
    private String gender;
    private String flat;
    private String houseNo;
    private String houseName;
    private String street;
    private String district;
    private String postTown;
    private String postcode;
    private String dobMM;
    private String dobDD;
    private String dobCCYY;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPostTown() {
        return postTown;
    }

    public void setPostTown(String postTown) {
        this.postTown = postTown;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getDobMM() {
        return dobMM;
    }

    public void setDobMM(String dobMM) {
        this.dobMM = dobMM;
    }

    public String getDobDD() {
        return dobDD;
    }

    public void setDobDD(String dobDD) {
        this.dobDD = dobDD;
    }

    public String getDobCCYY() {
        return dobCCYY;
    }

    public void setDobCCYY(String dobCCYY) {
        this.dobCCYY = dobCCYY;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SAARequestDto.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("firstName='" + firstName + "'")
                .add("initials='" + initials + "'")
                .add("surname='" + surname + "'")
                .add("gender='" + gender + "'")
                .add("flat='" + flat + "'")
                .add("houseNo='" + houseNo + "'")
                .add("houseName='" + houseName + "'")
                .add("street='" + street + "'")
                .add("district='" + district + "'")
                .add("postTown='" + postTown + "'")
                .add("postcode='" + postcode + "'")
                .add("dobDD='" + dobDD + "'")
                .add("dobMM='" + dobMM + "'")
                .add("dobCCYY='" + dobCCYY + "'")
                .toString();
    }
}
