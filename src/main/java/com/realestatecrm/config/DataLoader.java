package com.realestatecrm.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.realestatecrm.dto.savedsearch.SearchFilterDTO;
import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import com.realestatecrm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final PropertyAttributeOptionRepository propertyAttributeOptionRepository;
    private final PropertyRepository propertyRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final PropertySharingRepository propertySharingRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:}")
    private String adminPassword;

    @Autowired
    public DataLoader(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      PropertyAttributeRepository propertyAttributeRepository,
                      PropertyAttributeOptionRepository propertyAttributeOptionRepository,
                      PropertyRepository propertyRepository,
                      AttributeValueRepository attributeValueRepository,
                      PropertySharingRepository propertySharingRepository,
                      SavedSearchRepository savedSearchRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.propertyAttributeOptionRepository = propertyAttributeOptionRepository;
        this.propertyRepository = propertyRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.propertySharingRepository = propertySharingRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== DataLoader starting ===");

        // Only load data if the database is empty
        if (userRepository.count() == 0) {
            logger.info("Loading initial data...");
            loadInitialData();
            logger.info("Initial data loaded successfully!");
        } else {
            logger.info("Database already contains data, skipping initialization.");
        }

        logger.info("=== DataLoader completed ===");
    }

    private void loadInitialData() {
        // Create admin user from environment variables
        logger.info("Creating admin user...");

        // SECURITY: Validate admin password is set and strong
        if (adminPassword == null || adminPassword.isEmpty()) {
            throw new IllegalStateException("ADMIN_PASSWORD environment variable must be set. Use a strong password!");
        }
        if (adminPassword.length() < 12) {
            logger.warn("SECURITY WARNING: Admin password is weak! Use at least 12 characters.");
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail("admin@realestatecrm.com");
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        logger.info("Admin user created: {}", admin.getUsername());

        // Create basic property attributes
        logger.info("Creating property attributes...");
        createBasicAttributes();
        createStructureAttributes();
        createFeatureAttributes();
        createLocationAttributes();
        createFinancialAttributes();
        createUtilityAttributes();
        createExteriorAttributes();
        createInteriorAttributes();
        createDocumentationAttributes();
        logger.info("Property attributes created successfully!");

        // Create demo agents and properties with attribute values
        logger.info("Creating demo agents and properties...");
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
        logger.info("Demo agents, properties, and attribute values created!");

        // Create demo customers
        logger.info("Creating demo customers...");
        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("Smith");
        customer1.setEmail("john.smith@example.com");
        customer1.setPhone("555-0101");
        customer1.setBudgetMin(new BigDecimal("300000"));
        customer1.setBudgetMax(new BigDecimal("450000"));
        customer1.setAgent(alice);
        customer1 = customerRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setFirstName("Sarah");
        customer2.setLastName("Johnson");
        customer2.setEmail("sarah.johnson@example.com");
        customer2.setPhone("555-0102");
        customer2.setBudgetMin(new BigDecimal("400000"));
        customer2.setBudgetMax(new BigDecimal("600000"));
        customer2.setAgent(bob);
        customer2 = customerRepository.save(customer2);

        Customer customer3 = new Customer();
        customer3.setFirstName("Michael");
        customer3.setLastName("Williams");
        customer3.setEmail("michael.williams@example.com");
        customer3.setPhone("555-0103");
        customer3.setBudgetMin(new BigDecimal("200000"));
        customer3.setBudgetMax(new BigDecimal("350000"));
        customer3.setAgent(alice);
        customer3 = customerRepository.save(customer3);
        logger.info("Demo customers created!");

        // Create sample saved searches
        logger.info("Creating sample saved searches...");
        createSampleSavedSearches(customer1, customer2, customer3);
        logger.info("Sample saved searches created!");
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
            BigDecimal bd = (value instanceof BigDecimal) ? (BigDecimal) value : new BigDecimal(value.toString());
            av.setNumberValue(bd.setScale(2, java.math.RoundingMode.HALF_UP));
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

    private void createSampleSavedSearches(Customer customer1, Customer customer2, Customer customer3) {
        try {
            // Get commonly used attributes
            PropertyAttribute bedroomsAttr = getAttributeByName("Bedrooms");
            PropertyAttribute bathroomsAttr = getAttributeByName("Bathrooms");
            PropertyAttribute propertyTypeAttr = getAttributeByName("Property Type");
            PropertyAttribute cityAttr = getAttributeByName("City");
            PropertyAttribute hasGarageAttr = getAttributeByName("Has Garage");
            PropertyAttribute hasPoolAttr = getAttributeByName("Has Pool");
            PropertyAttribute squareFootageAttr = getAttributeByName("Square Footage");
            PropertyAttribute appliancesAttr = getAttributeByName("Included Appliances");
            PropertyAttribute parkingAttr = getAttributeByName("Parking Features");

            // Search 1: Family Homes (Alice) - NUMBER and SINGLE_SELECT filters
            List<SearchFilterDTO> familyHomeFilters = new ArrayList<>();

            SearchFilterDTO bedroomsFilter = new SearchFilterDTO();
            bedroomsFilter.setAttributeId(bedroomsAttr.getId());
            bedroomsFilter.setDataType(PropertyDataType.NUMBER);
            bedroomsFilter.setMinValue(new BigDecimal("3"));
            bedroomsFilter.setMaxValue(new BigDecimal("5"));
            familyHomeFilters.add(bedroomsFilter);

            SearchFilterDTO bathroomsFilter = new SearchFilterDTO();
            bathroomsFilter.setAttributeId(bathroomsAttr.getId());
            bathroomsFilter.setDataType(PropertyDataType.NUMBER);
            bathroomsFilter.setMinValue(new BigDecimal("2"));
            familyHomeFilters.add(bathroomsFilter);

            SearchFilterDTO propertyTypeFilter = new SearchFilterDTO();
            propertyTypeFilter.setAttributeId(propertyTypeAttr.getId());
            propertyTypeFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            propertyTypeFilter.setSelectedValues(List.of("Single Family Home", "Townhouse"));
            familyHomeFilters.add(propertyTypeFilter);

            createSavedSearch(customer1, "Family Homes 3-5BR",
                    "Single family homes and townhouses with 3-5 bedrooms and at least 2 bathrooms",
                    familyHomeFilters);

            // Search 2: Downtown Condos (Customer2 - Sarah) - SINGLE_SELECT and TEXT filters
            List<SearchFilterDTO> downtownCondoFilters = new ArrayList<>();

            SearchFilterDTO condoTypeFilter = new SearchFilterDTO();
            condoTypeFilter.setAttributeId(propertyTypeAttr.getId());
            condoTypeFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            condoTypeFilter.setSelectedValues(List.of("Condo"));
            downtownCondoFilters.add(condoTypeFilter);

            SearchFilterDTO cityFilter = new SearchFilterDTO();
            cityFilter.setAttributeId(cityAttr.getId());
            cityFilter.setDataType(PropertyDataType.TEXT);
            cityFilter.setTextValue("downtown");
            downtownCondoFilters.add(cityFilter);

            createSavedSearch(customer2, "Downtown Condos",
                    "Condos in downtown areas",
                    downtownCondoFilters);

            // Search 3: Luxury Properties (Customer2 - Sarah) - NUMBER and BOOLEAN filters
            List<SearchFilterDTO> luxuryFilters = new ArrayList<>();

            SearchFilterDTO sqftFilter = new SearchFilterDTO();
            sqftFilter.setAttributeId(squareFootageAttr.getId());
            sqftFilter.setDataType(PropertyDataType.NUMBER);
            sqftFilter.setMinValue(new BigDecimal("2500"));
            luxuryFilters.add(sqftFilter);

            SearchFilterDTO garageFilter = new SearchFilterDTO();
            garageFilter.setAttributeId(hasGarageAttr.getId());
            garageFilter.setDataType(PropertyDataType.BOOLEAN);
            garageFilter.setBooleanValue(true);
            luxuryFilters.add(garageFilter);

            SearchFilterDTO poolFilter = new SearchFilterDTO();
            poolFilter.setAttributeId(hasPoolAttr.getId());
            poolFilter.setDataType(PropertyDataType.BOOLEAN);
            poolFilter.setBooleanValue(true);
            luxuryFilters.add(poolFilter);

            createSavedSearch(customer2, "Luxury Properties",
                    "Large homes (2500+ sq ft) with garage and pool",
                    luxuryFilters);

            // Search 4: Starter Homes (Customer3 - Michael) - Multiple NUMBER filters
            List<SearchFilterDTO> starterHomeFilters = new ArrayList<>();

            SearchFilterDTO starterBedroomsFilter = new SearchFilterDTO();
            starterBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            starterBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            starterBedroomsFilter.setMinValue(new BigDecimal("2"));
            starterBedroomsFilter.setMaxValue(new BigDecimal("3"));
            starterHomeFilters.add(starterBedroomsFilter);

            SearchFilterDTO starterBathroomsFilter = new SearchFilterDTO();
            starterBathroomsFilter.setAttributeId(bathroomsAttr.getId());
            starterBathroomsFilter.setDataType(PropertyDataType.NUMBER);
            starterBathroomsFilter.setMinValue(new BigDecimal("1"));
            starterBathroomsFilter.setMaxValue(new BigDecimal("2"));
            starterHomeFilters.add(starterBathroomsFilter);

            SearchFilterDTO starterSqftFilter = new SearchFilterDTO();
            starterSqftFilter.setAttributeId(squareFootageAttr.getId());
            starterSqftFilter.setDataType(PropertyDataType.NUMBER);
            starterSqftFilter.setMaxValue(new BigDecimal("1500"));
            starterHomeFilters.add(starterSqftFilter);

            createSavedSearch(customer3, "Starter Homes",
                    "Smaller homes for first-time buyers (2-3BR, 1-2BA, under 1500 sq ft)",
                    starterHomeFilters);

            // Search 5: Modern Amenities (Customer1 - John) - MULTI_SELECT filters
            List<SearchFilterDTO> modernFilters = new ArrayList<>();

            SearchFilterDTO appliancesFilter = new SearchFilterDTO();
            appliancesFilter.setAttributeId(appliancesAttr.getId());
            appliancesFilter.setDataType(PropertyDataType.MULTI_SELECT);
            appliancesFilter.setSelectedValues(List.of("Dishwasher", "Washer", "Dryer", "Microwave"));
            modernFilters.add(appliancesFilter);

            SearchFilterDTO parkingFilter = new SearchFilterDTO();
            parkingFilter.setAttributeId(parkingAttr.getId());
            parkingFilter.setDataType(PropertyDataType.MULTI_SELECT);
            parkingFilter.setSelectedValues(List.of("Attached Garage", "Covered Parking"));
            modernFilters.add(parkingFilter);

            createSavedSearch(customer1, "Modern Amenities",
                    "Properties with modern appliances and covered parking",
                    modernFilters);

            logger.info("Created 5 sample saved searches for 3 customers demonstrating all filter types");

        } catch (Exception e) {
            logger.error("Error creating sample saved searches: {}", e.getMessage(), e);
        }
    }

    private void createSavedSearch(Customer customer, String name, String description, List<SearchFilterDTO> filters) {
        try {
            String filtersJson = objectMapper.writeValueAsString(filters);

            SavedSearch savedSearch = new SavedSearch();
            savedSearch.setCustomer(customer);
            savedSearch.setName(name);
            savedSearch.setDescription(description);
            savedSearch.setFiltersJson(filtersJson);

            savedSearchRepository.save(savedSearch);
            logger.info("Created saved search: '{}' for customer: {} {}", name, customer.getFirstName(), customer.getLastName());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize filters for saved search '{}': {}", name, e.getMessage());
        }
    }

    private PropertyAttribute getAttributeByName(String attributeName) {
        return propertyAttributeRepository.findByNameContainingIgnoreCase(attributeName)
                .stream()
                .filter(a -> a.getName().equalsIgnoreCase(attributeName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Attribute not found: " + attributeName));
    }
}