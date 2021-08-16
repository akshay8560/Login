
package com.example.cultino;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Data {

    @SerializedName("fav_mandi")
    @Expose
    private List<Object> favMandi = null;
    @SerializedName("other_mandi")
    @Expose
    private List<OtherMandi> otherMandi = null;

    public List<Object> getFavMandi() {
        return favMandi;
    }

    public void setFavMandi(List<Object> favMandi) {
        this.favMandi = favMandi;
    }

    public List<OtherMandi> getOtherMandi() {
        return otherMandi;
    }

    public void setOtherMandi(List<OtherMandi> otherMandi) {
        this.otherMandi = otherMandi;
    }

}
