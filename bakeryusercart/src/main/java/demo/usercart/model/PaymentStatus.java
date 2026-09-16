package demo.usercart.model;

public enum PaymentStatus {

    PENDING("待付款"),
    PAID("已付款");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
