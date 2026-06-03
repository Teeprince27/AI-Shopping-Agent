package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.model.UserProfile;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserProfileService {

    private final Map<String, UserProfile> profiles = new HashMap<>();

    @PostConstruct
    public void initProfiles() {
        profiles.put("U001", UserProfile.builder()
                .userId("U001").name("Adaeze Okonkwo")
                .purchaseHistory(List.of("P003", "P005"))
                .browsingHistory(List.of("P001", "P009"))
                .preferredCategories(List.of("Toys", "Art & Craft", "Books"))
                .childrenAges(5)
                .preferredCurrency("NGN")
                .build());

        profiles.put("U002", UserProfile.builder()
                .userId("U002").name("Emeka Nwosu")
                .purchaseHistory(List.of("P006", "P008"))
                .browsingHistory(List.of("P001", "P006"))
                .preferredCategories(List.of("Electronics", "Toys"))
                .childrenAges(6)
                .preferredCurrency("NGN")
                .build());

        profiles.put("GUEST", UserProfile.builder()
                .userId("GUEST").name("Guest User")
                .purchaseHistory(List.of())
                .browsingHistory(List.of())
                .preferredCategories(List.of("Toys"))
                .childrenAges(0)
                .preferredCurrency("NGN")
                .build());
    }

    public Optional<UserProfile> getUserProfile(String userId) {
        return Optional.ofNullable(profiles.getOrDefault(userId, profiles.get("GUEST")));
    }
}
