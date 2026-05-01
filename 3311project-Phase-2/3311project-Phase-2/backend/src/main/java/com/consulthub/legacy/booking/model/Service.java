package com.consulthub.legacy.booking.model;

import com.consulthub.legacy.admin.PricingPolicy;

/**
 * Represents a consulting service that can be booked.
 */
public class Service {

    private String serviceId;
    private String name;
    private int durationMinutes;
    private double price;

    public Service(String serviceId, String name, double price) {
        this(serviceId, name, 60, price);
    }

    public Service(String serviceId, String name, int durationMinutes, double price) {
        this.serviceId = serviceId;
        this.name = name;
        this.durationMinutes = durationMinutes;
        setPrice(price);
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double requestedPrice) {
        if (requestedPrice < PricingPolicy.getInstance().getMinPrice()) {
            this.price = PricingPolicy.getInstance().getMinPrice();
        } else if (requestedPrice > PricingPolicy.getInstance().getMaxPrice()) {
            this.price = PricingPolicy.getInstance().getMaxPrice();
        } else {
            this.price = requestedPrice;
        }
    }

    @Override
    public String toString() {
        return "Service{id='" + serviceId + "', name='" + name + "', durationMinutes="
                + durationMinutes + ", price=" + price + '}';
    }
}
