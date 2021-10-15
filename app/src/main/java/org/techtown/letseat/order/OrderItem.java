package org.techtown.letseat.order;

import android.graphics.Bitmap;

public class OrderItem {

    private Bitmap bitmap;
    private String menuName;
    private String price;
    private String resName;

    public OrderItem(Bitmap bitmap, String menuName, String price, String resName) {
        this.bitmap = bitmap;
        this.menuName = menuName;
        this.price = price;
        this.resName = resName;
    }

    public Bitmap getBitmap() {return  bitmap;}
    public String getMenuName() {
        return menuName;
    }
    public String getPrice() {
        return price;
    }
    public String getResName() {
        return resName;
    }


}
