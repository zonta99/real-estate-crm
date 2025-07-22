package com.realestatecrm.config;

import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.entity.PropertyAttributeOption;
import com.realestatecrm.entity.User;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import com.realestatecrm.repository.PropertyAttributeOptionRepository;
import com.realestatecrm.repository.PropertyAttributeRepository;
import com.realestatecrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final PropertyAttributeOptionRepository propertyAttributeOptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(UserRepository userRepository,
                      PropertyAttributeRepository propertyAttributeRepository,
                      PropertyAttributeOptionRepository propertyAttributeOptionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.propertyAttributeOptionRepository = propertyAttributeOptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DataLoader starting ===");

        // Only load data if database is empty
        if (userRepository.count() == 0) {
            System.out.println("Loading initial data...");
            loadInitialData();
            System.out.println("Initial data loaded successfully!");
        } else {
            System.out.println("Database already contains data, skipping initialization.");
        }

        System.out.println("=== DataLoader completed ===");
    }

    private void loadInitialData() {
        // Create admin user
        System.out.println("Creating admin user...");
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@realestatecrm.com");
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        System.out.println("Admin user created: " + admin.getUsername());

        // Create basic property attributes
        System.out.println("Creating property attributes...");
        createBasicAttributes();
        createStructureAttributes();
        createFeatureAttributes();
        createLocationAttributes();
        createFinancialAttributes();
        System.out.println("Property attributes created successfully!");
    }

    private void createBasicAttributes() {
        // Address (TEXT, Required)
        createAttribute("Address", PropertyDataType.TEXT, PropertyCategory.BASIC, 1, true, true);

        // Property Type (SINGLE_SELECT, Required)
        PropertyAttribute propertyType = createAttribute("Property Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.BASIC, 2, true, true);
        createOptions(propertyType, "House", "Condo", "Townhouse", "Duplex", "Land", "Commercial");

        // City (TEXT, Required)
        createAttribute("City", PropertyDataType.TEXT, PropertyCategory.BASIC, 3, true, true);

        // State (TEXT, Required)
        createAttribute("State", PropertyDataType.TEXT, PropertyCategory.BASIC, 4, true, true);

        // ZIP Code (TEXT, Required)
        createAttribute("ZIP Code", PropertyDataType.TEXT, PropertyCategory.BASIC, 5, true, true);
    }

    private void createStructureAttributes() {
        createAttribute("Bedrooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 1, false, true);
        createAttribute("Bathrooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 2, false, true);
        createAttribute("Square Footage", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 3, false, true);
        createAttribute("Lot Size (sq ft)", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 4, false, true);
        createAttribute("Year Built", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 5, false, true);
        createAttribute("Stories", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 6, false, true);
    }

    private void createFeatureAttributes() {
        createAttribute("Has Garage", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 1, false, true);
        createAttribute("Has Pool", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 2, false, true);
        createAttribute("Has Fireplace", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 3, false, true);
        createAttribute("Central Air Conditioning", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 4, false, true);

        // Basement Type (SINGLE_SELECT)
        PropertyAttribute basement = createAttribute("Basement Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 5, false, true);
        createOptions(basement, "None", "Partial", "Full", "Finished", "Walk-out");

        // Included Appliances (MULTI_SELECT)
        PropertyAttribute appliances = createAttribute("Included Appliances", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 6, false, true);
        createOptions(appliances, "Refrigerator", "Dishwasher", "Washer", "Dryer", "Range/Oven", "Microwave", "Garbage Disposal");
    }

    private void createLocationAttributes() {
        createAttribute("School District", PropertyDataType.TEXT, PropertyCategory.LOCATION, 1, false, true);
        createAttribute("Neighborhood", PropertyDataType.TEXT, PropertyCategory.LOCATION, 2, false, true);
    }

    private void createFinancialAttributes() {
        createAttribute("Annual Property Tax", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 1, false, true);
        createAttribute("HOA Fee (Monthly)", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 2, false, true);
    }

    private PropertyAttribute createAttribute(String name, PropertyDataType dataType, PropertyCategory category,
                                              int displayOrder, boolean isRequired, boolean isSearchable) {
        PropertyAttribute attribute = new PropertyAttribute();
        attribute.setName(name);
        attribute.setDataType(dataType);
        attribute.setCategory(category);
        attribute.setDisplayOrder(displayOrder);
        attribute.setIsRequired(isRequired);
        attribute.setIsSearchable(isSearchable);
        return propertyAttributeRepository.save(attribute);
    }

    private void createOptions(PropertyAttribute attribute, String... optionValues) {
        for (int i = 0; i < optionValues.length; i++) {
            PropertyAttributeOption option = new PropertyAttributeOption();
            option.setAttribute(attribute);
            option.setOptionValue(optionValues[i]);
            option.setDisplayOrder(i + 1);
            propertyAttributeOptionRepository.save(option);
        }
    }
}