package es.storeapp.web.forms;

public class OrderForm {
    
    private String name;
    private String address;
    private Boolean payNow;

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

    public Boolean getPayNow() {
        return payNow;
    }

    public void setPayNow(Boolean payNow) {
        this.payNow = payNow;
    }
        
}
