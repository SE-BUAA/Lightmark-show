package top.ortus.timemark.backend.dto.module;

import java.math.BigDecimal;

public class RoomTypeDTO {
    private String id;
    private String hotel_id;
    private String room_name;
    private BigDecimal price;
    private String cancel_policy;
    private int breakfast;

    public RoomTypeDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotel_id() {
        return hotel_id;
    }

    public void setHotel_id(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCancel_policy() {
        return cancel_policy;
    }

    public void setCancel_policy(String cancel_policy) {
        this.cancel_policy = cancel_policy;
    }

    public int getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(int breakfast) {
        this.breakfast = breakfast;
    }
}