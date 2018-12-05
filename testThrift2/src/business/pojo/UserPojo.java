package business.pojo;

/**
 * 用户信息
 */
public class UserPojo extends AbstractPojo {
    private String userName;

    private Integer userSex;

    private String userAddr;

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
     * @return the userSex
     */
    public Integer getUserSex() {
        return userSex;
    }

    /**
     * @param userSex the userSex to set
     */
    public void setUserSex(Integer userSex) {
        this.userSex = userSex;
    }

    /**
     * @return the userAddr
     */
    public String getUserAddr() {
        return userAddr;
    }

    /**
     * @param userAddr the userAddr to set
     */
    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr;
    }

}	
