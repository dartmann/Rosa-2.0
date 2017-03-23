package de.davidartmann.android.rosa2.database.model;

import java.io.Serializable;

/**
 * Model class for the person table of db.
 * Created by david on 23.08.16.
 */
public class Person implements Serializable {

    public static final int SURE = 1;
    public static final int ALMOST_SURE = 2;
    public static final int UNSURE = 3;

    /**
     * Attributes of Object.
     */
    private int _id;            //0
    private long createTime;    //1
    private long updateTime;    //2
    private String name;        //3
    private String address;     //4
    private String phone;       //5
    private String email;       //6
    private String price;       //7
    private String misc;        //8
    private int category;       //9
    private boolean active;     //10
    private String pictureUrl;  //11
    private int position;       //12
    private String tabText;     //13


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMisc() {
        return misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTabText() {
        return tabText;
    }

    public void setTabText(String tabText) {
        this.tabText = tabText;
    }
}
