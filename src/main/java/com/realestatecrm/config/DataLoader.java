package com.realestatecrm.config;

import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import com.realestatecrm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final PropertyAttributeOptionRepository propertyAttributeOptionRepository;
    private final PropertyRepository propertyRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final PropertySharingRepository propertySharingRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(UserRepository userRepository,
                      PropertyAttributeRepository propertyAttributeRepository,
                      PropertyAttributeOptionRepository propertyAttributeOptionRepository,
                      PropertyRepository propertyRepository,
                      AttributeValueRepository attributeValueRepository,
                      PropertySharingRepository propertySharingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.propertyAttributeOptionRepository = propertyAttributeOptionRepository;
        this.propertyRepository = propertyRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.propertySharingRepository = propertySharingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DataLoader starting ===");

        // Only load data if the database is empty
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
        createUtilityAttributes();
        createExteriorAttributes();
        createInteriorAttributes();
        createDocumentationAttributes();
        System.out.println("Property attributes created successfully!");

        // Create demo agents and properties with attribute values
        System.out.println("Creating demo agents and properties...");
        User alice = new User();
        alice.setUsername("alice");
        alice.setPassword(passwordEncoder.encode("password"));
        alice.setEmail("alice@realestatecrm.com");
        alice.setFirstName("Alice");
        alice.setLastName("Agent");
        alice.setRole(Role.AGENT);
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);

        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword(passwordEncoder.encode("password"));
        bob.setEmail("bob@realestatecrm.com");
        bob.setFirstName("Bob");
        bob.setLastName("Broker");
        bob.setRole(Role.BROKER);
        bob.setStatus(UserStatus.ACTIVE);
        userRepository.save(bob);

        Property p1 = new Property();
        p1.setTitle("Cozy Family Home");
        p1.setDescription("3-bed, 2-bath cozy home near parks and schools.");
        p1.setPrice(new BigDecimal("350000"));
        p1.setAgent(alice);
        p1 = propertyRepository.save(p1);

        Property p2 = new Property();
        p2.setTitle("Modern Downtown Condo");
        p2.setDescription("Stylish 2-bed condo with city views and amenities.");
        p2.setPrice(new BigDecimal("525000"));
        p2.setAgent(bob);
        p2 = propertyRepository.save(p2);

        // Set a few attribute values for the properties
        setAttributeValueByName(p1, "City", "Springfield");
        setAttributeValueByName(p1, "Bedrooms", new BigDecimal("3"));
        setAttributeValueByName(p1, "Property Type", "Single Family Home");
        setAttributeValueByName(p1, "Has Garage", Boolean.TRUE);

        setAttributeValueByName(p2, "City", "Metropolis");
        setAttributeValueByName(p2, "Bedrooms", new BigDecimal("2"));
        setAttributeValueByName(p2, "Property Type", "Condo");
        setAttributeValueByName(p2, "Has Garage", Boolean.FALSE);

        // Share p1 with Bob as a demo
        PropertySharing share = new PropertySharing(p1, bob, alice);
        propertySharingRepository.save(share);
        System.out.println("Demo agents, properties, and attribute values created!");
    }

    private void setAttributeValueByName(Property property, String attributeName, Object value) {
        PropertyAttribute attribute = propertyAttributeRepository.findByNameContainingIgnoreCase(attributeName)
                .stream()
                .filter(a -> a.getName().equalsIgnoreCase(attributeName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Attribute not found: " + attributeName));

        AttributeValue av = new AttributeValue(property, attribute);
        // Clear and set based on type
        av.setTextValue(null);
        av.setNumberValue(null);
        av.setBooleanValue(null);
        av.setMultiSelectValue(null);
        if (attribute.getDataType() == PropertyDataType.TEXT || attribute.getDataType() == PropertyDataType.SINGLE_SELECT) {
            av.setTextValue((String) value);
        } else if (attribute.getDataType() == PropertyDataType.NUMBER) {
            av.setNumberValue((BigDecimal) value);
        } else if (attribute.getDataType() == PropertyDataType.BOOLEAN) {
            av.setBooleanValue((Boolean) value);
        } else if (attribute.getDataType() == PropertyDataType.MULTI_SELECT) {
            av.setMultiSelectValue((String) value);
        }
        attributeValueRepository.save(av);
    }

    private void createBasicAttributes() {
        // Address (TEXT, Required)
        createAttribute("Address", PropertyDataType.TEXT, PropertyCategory.BASIC, 1, true, true);

        // Property Type (SINGLE_SELECT, Required)
        PropertyAttribute propertyType = createAttribute("Property Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.BASIC, 2, true, true);
        createOptions(propertyType, "Single Family Home", "Condo", "Townhouse", "Duplex", "Triplex", "Fourplex", "Multi-Family", "Vacant Land", "Commercial", "Industrial", "Mobile Home", "Manufactured Home");

        // City (TEXT, Required)
        createAttribute("City", PropertyDataType.TEXT, PropertyCategory.BASIC, 3, true, true);

        // State (TEXT, Required)
        createAttribute("State", PropertyDataType.TEXT, PropertyCategory.BASIC, 4, true, true);

        // ZIP Code (TEXT, Required)
        createAttribute("ZIP Code", PropertyDataType.TEXT, PropertyCategory.BASIC, 5, true, true);

        // MLS Number
        createAttribute("MLS Number", PropertyDataType.TEXT, PropertyCategory.BASIC, 6, false, true);

        // Property Status
        PropertyAttribute propertyStatus = createAttribute("Property Status", PropertyDataType.SINGLE_SELECT, PropertyCategory.BASIC, 7, false, true);
        createOptions(propertyStatus, "Active", "Pending", "Sold", "Withdrawn", "Expired", "Coming Soon", "Under Contract");
    }

    private void createStructureAttributes() {
        createAttribute("Bedrooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 1, false, true);
        createAttribute("Bathrooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 2, false, true);
        createAttribute("Half Bathrooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 3, false, true);
        createAttribute("Total Rooms", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 4, false, true);
        createAttribute("Square Footage", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 5, false, true);
        createAttribute("Lot Size (sq ft)", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 6, false, true);
        createAttribute("Year Built", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 7, false, true);
        createAttribute("Year Renovated", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 8, false, false);
        createAttribute("Stories", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 9, false, true);
        createAttribute("Garage Spaces", PropertyDataType.NUMBER, PropertyCategory.STRUCTURE, 10, false, true);

        // Architectural Style
        PropertyAttribute archStyle = createAttribute("Architectural Style", PropertyDataType.SINGLE_SELECT, PropertyCategory.STRUCTURE, 11, false, true);
        createOptions(archStyle, "Contemporary", "Traditional", "Colonial", "Victorian", "Ranch", "Cape Cod", "Tudor", "Mediterranean", "Craftsman", "Modern", "Mid-Century Modern", "Spanish", "Georgian", "Federal", "Art Deco", "Other");

        // Foundation Type
        PropertyAttribute foundation = createAttribute("Foundation Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.STRUCTURE, 12, false, false);
        createOptions(foundation, "Concrete Slab", "Crawl Space", "Full Basement", "Partial Basement", "Pier & Beam", "Stone", "Block", "Other");
    }

    private void createFeatureAttributes() {
        createAttribute("Has Garage", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 1, false, true);
        createAttribute("Has Pool", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 2, false, true);
        createAttribute("Has Hot Tub/Spa", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 3, false, true);
        createAttribute("Has Fireplace", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 4, false, true);
        createAttribute("Central Air Conditioning", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 5, false, true);
        createAttribute("Has Deck/Patio", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 6, false, true);
        createAttribute("Has Balcony", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 7, false, true);
        createAttribute("Has Security System", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 8, false, true);
        createAttribute("Has Sprinkler System", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 9, false, false);
        createAttribute("Has Workshop", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 10, false, false);
        createAttribute("Has Guest House", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 11, false, false);

        // Basement Type (SINGLE_SELECT)
        PropertyAttribute basement = createAttribute("Basement Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 12, false, true);
        createOptions(basement, "None", "Partial", "Full", "Finished", "Walk-out", "English");

        // Pool Type
        PropertyAttribute poolType = createAttribute("Pool Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 13, false, false);
        createOptions(poolType, "None", "In-Ground", "Above-Ground", "Indoor", "Infinity", "Salt Water");

        // Heating Type
        PropertyAttribute heating = createAttribute("Heating Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 14, false, true);
        createOptions(heating, "Central Gas", "Central Electric", "Heat Pump", "Radiant", "Baseboard", "Wall Unit", "Wood Stove", "Geothermal", "Solar", "Oil", "Coal", "None", "Other");

        // Cooling Type
        PropertyAttribute cooling = createAttribute("Cooling Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 15, false, true);
        createOptions(cooling, "Central Air", "Window Units", "Wall Units", "Evaporative", "Heat Pump", "Geothermal", "None", "Other");

        // Included Appliances (MULTI_SELECT)
        PropertyAttribute appliances = createAttribute("Included Appliances", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 16, false, true);
        createOptions(appliances, "Refrigerator", "Dishwasher", "Washer", "Dryer", "Range/Oven", "Microwave", "Garbage Disposal", "Wine Cooler", "Ice Maker", "Trash Compactor");

        // Parking Features
        PropertyAttribute parking = createAttribute("Parking Features", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 17, false, true);
        createOptions(parking, "Attached Garage", "Detached Garage", "Carport", "Covered Parking", "Circular Driveway", "RV Parking", "Boat Parking", "Street Parking");
    }

    private void createLocationAttributes() {
        createAttribute("School District", PropertyDataType.TEXT, PropertyCategory.LOCATION, 1, false, true);
        createAttribute("Neighborhood", PropertyDataType.TEXT, PropertyCategory.LOCATION, 2, false, true);
        createAttribute("Subdivision", PropertyDataType.TEXT, PropertyCategory.LOCATION, 3, false, true);
        createAttribute("Distance to Downtown (miles)", PropertyDataType.NUMBER, PropertyCategory.LOCATION, 4, false, false);
        createAttribute("Walkability Score", PropertyDataType.NUMBER, PropertyCategory.LOCATION, 5, false, false);

        // View Type
        PropertyAttribute view = createAttribute("View", PropertyDataType.MULTI_SELECT, PropertyCategory.LOCATION, 6, false, true);
        createOptions(view, "Ocean", "Lake", "River", "Mountain", "City", "Golf Course", "Park", "Garden", "Pool", "No View");

        // Transportation Access
        PropertyAttribute transportation = createAttribute("Transportation Access", PropertyDataType.MULTI_SELECT, PropertyCategory.LOCATION, 7, false, false);
        createOptions(transportation, "Bus Line", "Metro/Subway", "Highway Access", "Airport Nearby", "Train Station", "Ferry");

        // Nearby Amenities
        PropertyAttribute amenities = createAttribute("Nearby Amenities", PropertyDataType.MULTI_SELECT, PropertyCategory.LOCATION, 8, false, false);
        createOptions(amenities, "Shopping Mall", "Restaurants", "Hospital", "Schools", "Parks", "Golf Course", "Beach", "Ski Resort", "Marina", "Library", "Community Center");
    }

    private void createFinancialAttributes() {
        createAttribute("Annual Property Tax", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 1, false, true);
        createAttribute("HOA Fee (Monthly)", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 2, false, true);
        createAttribute("Insurance Cost (Annual)", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 3, false, false);
        createAttribute("Utility Costs (Monthly Average)", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 4, false, false);
        createAttribute("Special Assessments", PropertyDataType.NUMBER, PropertyCategory.FINANCIAL, 5, false, false);

        // Tax Status
        PropertyAttribute taxStatus = createAttribute("Tax Status", PropertyDataType.SINGLE_SELECT, PropertyCategory.FINANCIAL, 6, false, false);
        createOptions(taxStatus, "Current", "Delinquent", "Exempt", "Abated");
    }

    private void createUtilityAttributes() {
        // Water Source
        PropertyAttribute water = createAttribute("Water Source", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 18, false, false);
        createOptions(water, "Public", "Well", "Community", "Other");

        // Sewer Type
        PropertyAttribute sewer = createAttribute("Sewer Type", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 19, false, false);
        createOptions(sewer, "Public Sewer", "Septic Tank", "Community System", "Other");

        // Internet/Cable
        PropertyAttribute internet = createAttribute("Internet/Cable Ready", PropertyDataType.BOOLEAN, PropertyCategory.FEATURES, 20, false, false);

        // Utilities Included
        PropertyAttribute utilities = createAttribute("Utilities Included", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 21, false, false);
        createOptions(utilities, "Water", "Sewer", "Electric", "Gas", "Trash", "Internet", "Cable TV", "None");
    }

    private void createExteriorAttributes() {
        // Exterior Material
        PropertyAttribute exterior = createAttribute("Exterior Material", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 22, false, true);
        createOptions(exterior, "Brick", "Vinyl Siding", "Wood Siding", "Stucco", "Stone", "Aluminum", "Fiber Cement", "Block", "Cedar", "Log");

        // Roof Material
        PropertyAttribute roof = createAttribute("Roof Material", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 23, false, false);
        createOptions(roof, "Asphalt Shingle", "Metal", "Tile", "Slate", "Wood Shake", "Flat/Built-up", "Membrane", "Other");

        // Roof Age
        createAttribute("Roof Age (years)", PropertyDataType.NUMBER, PropertyCategory.FEATURES, 24, false, false);

        // Driveway Material
        PropertyAttribute driveway = createAttribute("Driveway Material", PropertyDataType.SINGLE_SELECT, PropertyCategory.FEATURES, 25, false, false);
        createOptions(driveway, "Concrete", "Asphalt", "Gravel", "Brick", "Stone", "Dirt", "Other");

        // Landscaping
        PropertyAttribute landscaping = createAttribute("Landscaping", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 26, false, false);
        createOptions(landscaping, "Front Yard", "Back Yard", "Mature Trees", "Garden", "Lawn", "Desert", "Natural", "Professional");
    }

    private void createInteriorAttributes() {
        // Flooring Type
        PropertyAttribute flooring = createAttribute("Primary Flooring", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 27, false, true);
        createOptions(flooring, "Hardwood", "Carpet", "Tile", "Laminate", "Vinyl", "Stone", "Marble", "Bamboo", "Cork", "Concrete");

        // Kitchen Features
        PropertyAttribute kitchen = createAttribute("Kitchen Features", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 28, false, false);
        createOptions(kitchen, "Granite Counters", "Stainless Appliances", "Island", "Pantry", "Breakfast Bar", "Updated Cabinets", "Gas Range", "Double Oven");

        // Bathroom Features
        PropertyAttribute bathroom = createAttribute("Bathroom Features", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 29, false, false);
        createOptions(bathroom, "Master Bath", "Walk-in Shower", "Jetted Tub", "Double Vanity", "Separate Tub/Shower", "Updated Fixtures");

        // Special Rooms
        PropertyAttribute specialRooms = createAttribute("Special Rooms", PropertyDataType.MULTI_SELECT, PropertyCategory.FEATURES, 30, false, false);
        createOptions(specialRooms, "Home Office", "Library", "Wine Cellar", "Media Room", "Game Room", "Exercise Room", "Craft Room", "Music Room", "Safe Room");
    }

    private void createDocumentationAttributes() {
        createAttribute("Property Disclosure Available", PropertyDataType.BOOLEAN, PropertyCategory.BASIC, 8, false, false);
        createAttribute("Survey Available", PropertyDataType.BOOLEAN, PropertyCategory.BASIC, 9, false, false);
        createAttribute("HOA Documents Available", PropertyDataType.BOOLEAN, PropertyCategory.BASIC, 10, false, false);

        // Date fields
        createAttribute("Date Listed", PropertyDataType.DATE, PropertyCategory.BASIC, 11, false, false);
        createAttribute("Date Sold", PropertyDataType.DATE, PropertyCategory.BASIC, 12, false, false);
        createAttribute("Last Inspection Date", PropertyDataType.DATE, PropertyCategory.BASIC, 13, false, false);
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