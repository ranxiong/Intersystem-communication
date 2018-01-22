package com.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

public class UserInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -377525163661420263L;

    private String userName;
    private String userDesc;
    private Integer userAge;
    private Boolean userSex;

    public UserInfo() throws RemoteException {

    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userDesc
     */
    public String getUserDesc() {
        return userDesc;
    }

    /**
     * @param userDesc the userDesc to set
     */
    public void setUserDesc(String userDesc) {
        this.userDesc = userDesc;
    }

    /**
     * @return the userAge
     */
    public Integer getUserAge() {
        return userAge;
    }

    /**
     * @param userAge the userAge to set
     */
    public void setUserAge(Integer userAge) {
        this.userAge = userAge;
    }

    /**
     * @return the userSex
     */
    public Boolean getUserSex() {
        return userSex;
    }

    /**
     * @param userSex the userSex to set
     */
    public void setUserSex(Boolean userSex) {
        this.userSex = userSex;
    }
}