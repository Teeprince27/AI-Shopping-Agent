package com.temitope.ShoppingAgent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private String userId;
    private String name;
    private List<String> purchaseHistory;       // product IDs previously bought
    private List<String> browsingHistory;        // product IDs recently viewed
    private List<String> preferredCategories;
    private int childrenAges;                   // for gift recommendations
    private String preferredCurrency;
}
