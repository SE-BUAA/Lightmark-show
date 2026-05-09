package top.ortus.timemark.backend.dto.module;

import java.time.LocalDate;

public class FlightOrderDetailDTO {
    private String id;
    private String order_id;
    private String product_id;
    private String flight_no;
    private LocalDate departure_date;
    private String passenger_list;
    private String baggage;
    private int insurance;

    public FlightOrderDetailDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getFlight_no() {
        return flight_no;
    }

    public void setFlight_no(String flight_no) {
        this.flight_no = flight_no;
    }

    public LocalDate getDeparture_date() {
        return departure_date;
    }

    public void setDeparture_date(LocalDate departure_date) {
        this.departure_date = departure_date;
    }

    public String getPassenger_list() {
        return passenger_list;
    }

    public void setPassenger_list(String passenger_list) {
        this.passenger_list = passenger_list;
    }

    public String getBaggage() {
        return baggage;
    }

    public void setBaggage(String baggage) {
        this.baggage = baggage;
    }

    public int getInsurance() {
        return insurance;
    }

    public void setInsurance(int insurance) {
        this.insurance = insurance;
    }
}