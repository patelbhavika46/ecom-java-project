package com.divacode.ecom_proj.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "cart_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    private String itemId;
    private String name;
    private int quantity;
    private double price;

    @Override
    public String toString() {
        return "CartItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
