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

        // Agent 1: Alice
        User alice = new User();
        alice.setUsername("alice");
        alice.setPassword(passwordEncoder.encode("password"));
        alice.setEmail("alice@realestatecrm.com");
        alice.setFirstName("Alice");
        alice.setLastName("Anderson");
        alice.setRole(Role.AGENT);
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);

        // Broker 1: Bob
        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword(passwordEncoder.encode("password"));
        bob.setEmail("bob@realestatecrm.com");
        bob.setFirstName("Bob");
        bob.setLastName("Martinez");
        bob.setRole(Role.BROKER);
        bob.setStatus(UserStatus.ACTIVE);
        userRepository.save(bob);

        // Agent 2: Carol
        User carol = new User();
        carol.setUsername("carol");
        carol.setPassword(passwordEncoder.encode("password"));
        carol.setEmail("carol@realestatecrm.com");
        carol.setFirstName("Carol");
        carol.setLastName("Chen");
        carol.setRole(Role.AGENT);
        carol.setStatus(UserStatus.ACTIVE);
        userRepository.save(carol);

        // Agent 3: David
        User david = new User();
        david.setUsername("david");
        david.setPassword(passwordEncoder.encode("password"));
        david.setEmail("david@realestatecrm.com");
        david.setFirstName("David");
        david.setLastName("Thompson");
        david.setRole(Role.AGENT);
        david.setStatus(UserStatus.ACTIVE);
        userRepository.save(david);

        // Assistant: Emma
        User emma = new User();
        emma.setUsername("emma");
        emma.setPassword(passwordEncoder.encode("password"));
        emma.setEmail("emma@realestatecrm.com");
        emma.setFirstName("Emma");
        emma.setLastName("Rodriguez");
        emma.setRole(Role.ASSISTANT);
        emma.setStatus(UserStatus.ACTIVE);
        userRepository.save(emma);

        logger.info("Created 5 demo users (1 admin, 1 broker, 3 agents, 1 assistant)");

        // Property 1: Cozy Family Home (Alice)
        Property p1 = new Property();
        p1.setTitle("Cozy Family Home");
        p1.setDescription("3-bed, 2-bath cozy home near parks and schools. Perfect for growing families.");
        p1.setPrice(new BigDecimal("350000"));
        p1.setAgent(alice);
        p1 = propertyRepository.save(p1);
        setAttributeValueByName(p1, "City", "Springfield");
        setAttributeValueByName(p1, "State", "IL");
        setAttributeValueByName(p1, "ZIP Code", "62701");
        setAttributeValueByName(p1, "Bedrooms", new BigDecimal("3"));
        setAttributeValueByName(p1, "Bathrooms", new BigDecimal("2"));
        setAttributeValueByName(p1, "Square Footage", new BigDecimal("1800"));
        setAttributeValueByName(p1, "Property Type", "Single Family Home");
        setAttributeValueByName(p1, "Has Garage", Boolean.TRUE);
        setAttributeValueByName(p1, "Has Pool", Boolean.FALSE);
        setAttributeValueByName(p1, "Year Built", new BigDecimal("2005"));

        // Property 2: Modern Downtown Condo (Bob)
        Property p2 = new Property();
        p2.setTitle("Modern Downtown Condo");
        p2.setDescription("Stylish 2-bed condo with city views and amenities. Walking distance to everything.");
        p2.setPrice(new BigDecimal("525000"));
        p2.setAgent(bob);
        p2 = propertyRepository.save(p2);
        setAttributeValueByName(p2, "City", "Metropolis");
        setAttributeValueByName(p2, "State", "NY");
        setAttributeValueByName(p2, "ZIP Code", "10001");
        setAttributeValueByName(p2, "Bedrooms", new BigDecimal("2"));
        setAttributeValueByName(p2, "Bathrooms", new BigDecimal("2"));
        setAttributeValueByName(p2, "Square Footage", new BigDecimal("1200"));
        setAttributeValueByName(p2, "Property Type", "Condo");
        setAttributeValueByName(p2, "Has Garage", Boolean.FALSE);
        setAttributeValueByName(p2, "Has Pool", Boolean.TRUE);
        setAttributeValueByName(p2, "Year Built", new BigDecimal("2018"));

        // Property 3: Luxury Beachfront Villa (Carol)
        Property p3 = new Property();
        p3.setTitle("Luxury Beachfront Villa");
        p3.setDescription("5-bed, 4-bath oceanfront paradise with private beach access. Stunning sunset views.");
        p3.setPrice(new BigDecimal("1850000"));
        p3.setAgent(carol);
        p3 = propertyRepository.save(p3);
        setAttributeValueByName(p3, "City", "Miami Beach");
        setAttributeValueByName(p3, "State", "FL");
        setAttributeValueByName(p3, "ZIP Code", "33139");
        setAttributeValueByName(p3, "Bedrooms", new BigDecimal("5"));
        setAttributeValueByName(p3, "Bathrooms", new BigDecimal("4"));
        setAttributeValueByName(p3, "Square Footage", new BigDecimal("4500"));
        setAttributeValueByName(p3, "Property Type", "Single Family Home");
        setAttributeValueByName(p3, "Has Garage", Boolean.TRUE);
        setAttributeValueByName(p3, "Garage Spaces", new BigDecimal("3"));
        setAttributeValueByName(p3, "Has Pool", Boolean.TRUE);
        setAttributeValueByName(p3, "Has Hot Tub/Spa", Boolean.TRUE);
        setAttributeValueByName(p3, "Year Built", new BigDecimal("2020"));

        // Property 4: Charming Townhouse (David)
        Property p4 = new Property();
        p4.setTitle("Charming Townhouse");
        p4.setDescription("2-bed, 2.5-bath townhouse in quiet neighborhood. Move-in ready!");
        p4.setPrice(new BigDecimal("285000"));
        p4.setAgent(david);
        p4 = propertyRepository.save(p4);
        setAttributeValueByName(p4, "City", "Portland");
        setAttributeValueByName(p4, "State", "OR");
        setAttributeValueByName(p4, "ZIP Code", "97201");
        setAttributeValueByName(p4, "Bedrooms", new BigDecimal("2"));
        setAttributeValueByName(p4, "Bathrooms", new BigDecimal("2"));
        setAttributeValueByName(p4, "Half Bathrooms", new BigDecimal("1"));
        setAttributeValueByName(p4, "Square Footage", new BigDecimal("1400"));
        setAttributeValueByName(p4, "Property Type", "Townhouse");
        setAttributeValueByName(p4, "Has Garage", Boolean.TRUE);
        setAttributeValueByName(p4, "Year Built", new BigDecimal("2015"));

        // Property 5: Mountain Retreat Cabin (Alice)
        Property p5 = new Property();
        p5.setTitle("Mountain Retreat Cabin");
        p5.setDescription("3-bed, 2-bath rustic cabin with mountain views. Perfect getaway!");
        p5.setPrice(new BigDecimal("425000"));
        p5.setAgent(alice);
        p5 = propertyRepository.save(p5);
        setAttributeValueByName(p5, "City", "Aspen");
        setAttributeValueByName(p5, "State", "CO");
        setAttributeValueByName(p5, "ZIP Code", "81611");
        setAttributeValueByName(p5, "Bedrooms", new BigDecimal("3"));
        setAttributeValueByName(p5, "Bathrooms", new BigDecimal("2"));
        setAttributeValueByName(p5, "Square Footage", new BigDecimal("2000"));
        setAttributeValueByName(p5, "Property Type", "Single Family Home");
        setAttributeValueByName(p5, "Has Garage", Boolean.TRUE);
        setAttributeValueByName(p5, "Has Fireplace", Boolean.TRUE);
        setAttributeValueByName(p5, "Year Built", new BigDecimal("2010"));

        // Property 6: Starter Home (Carol)
        Property p6 = new Property();
        p6.setTitle("Affordable Starter Home");
        p6.setDescription("2-bed, 1-bath perfect for first-time buyers. Great investment opportunity.");
        p6.setPrice(new BigDecimal("189000"));
        p6.setAgent(carol);
        p6 = propertyRepository.save(p6);
        setAttributeValueByName(p6, "City", "Austin");
        setAttributeValueByName(p6, "State", "TX");
        setAttributeValueByName(p6, "ZIP Code", "78701");
        setAttributeValueByName(p6, "Bedrooms", new BigDecimal("2"));
        setAttributeValueByName(p6, "Bathrooms", new BigDecimal("1"));
        setAttributeValueByName(p6, "Square Footage", new BigDecimal("950"));
        setAttributeValueByName(p6, "Property Type", "Single Family Home");
        setAttributeValueByName(p6, "Has Garage", Boolean.FALSE);
        setAttributeValueByName(p6, "Year Built", new BigDecimal("1998"));

        // Property 7: Suburban Family Home (Bob)
        Property p7 = new Property();
        p7.setTitle("Spacious Suburban Home");
        p7.setDescription("4-bed, 3-bath home with large backyard. Top-rated school district!");
        p7.setPrice(new BigDecimal("475000"));
        p7.setAgent(bob);
        p7 = propertyRepository.save(p7);
        setAttributeValueByName(p7, "City", "Charlotte");
        setAttributeValueByName(p7, "State", "NC");
        setAttributeValueByName(p7, "ZIP Code", "28202");
        setAttributeValueByName(p7, "Bedrooms", new BigDecimal("4"));
        setAttributeValueByName(p7, "Bathrooms", new BigDecimal("3"));
        setAttributeValueByName(p7, "Square Footage", new BigDecimal("2800"));
        setAttributeValueByName(p7, "Property Type", "Single Family Home");
        setAttributeValueByName(p7, "Has Garage", Boolean.TRUE);
        setAttributeValueByName(p7, "Garage Spaces", new BigDecimal("2"));
        setAttributeValueByName(p7, "Has Pool", Boolean.TRUE);
        setAttributeValueByName(p7, "Year Built", new BigDecimal("2012"));

        // Property 8: Urban Loft (David)
        Property p8 = new Property();
        p8.setTitle("Industrial Urban Loft");
        p8.setDescription("1-bed, 1-bath loft with exposed brick and high ceilings. Artistic vibe!");
        p8.setPrice(new BigDecimal("395000"));
        p8.setAgent(david);
        p8 = propertyRepository.save(p8);
        setAttributeValueByName(p8, "City", "Seattle");
        setAttributeValueByName(p8, "State", "WA");
        setAttributeValueByName(p8, "ZIP Code", "98101");
        setAttributeValueByName(p8, "Bedrooms", new BigDecimal("1"));
        setAttributeValueByName(p8, "Bathrooms", new BigDecimal("1"));
        setAttributeValueByName(p8, "Square Footage", new BigDecimal("850"));
        setAttributeValueByName(p8, "Property Type", "Condo");
        setAttributeValueByName(p8, "Has Garage", Boolean.FALSE);
        setAttributeValueByName(p8, "Year Built", new BigDecimal("2016"));

        logger.info("Created 8 diverse properties with detailed attributes");

        // Share properties between agents
        PropertySharing share1 = new PropertySharing(p1, bob, alice);
        propertySharingRepository.save(share1);

        PropertySharing share2 = new PropertySharing(p3, alice, carol);
        propertySharingRepository.save(share2);

        PropertySharing share3 = new PropertySharing(p7, david, bob);
        propertySharingRepository.save(share3);

        logger.info("Created 3 property sharing relationships");

        // Create demo customers
        logger.info("Creating demo customers...");

        // Customer 1: John Smith - Mid-range family buyer
        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("Smith");
        customer1.setEmail("john.smith@example.com");
        customer1.setPhone("555-0101");
        customer1.setBudgetMin(new BigDecimal("300000"));
        customer1.setBudgetMax(new BigDecimal("450000"));
        customer1.setNotes("Looking for family home with 3+ bedrooms. Needs good school district.");
        customer1.setLeadSource("Website Inquiry");
        customer1.setAgent(alice);
        customer1 = customerRepository.save(customer1);

        // Customer 2: Sarah Johnson - Luxury buyer
        Customer customer2 = new Customer();
        customer2.setFirstName("Sarah");
        customer2.setLastName("Johnson");
        customer2.setEmail("sarah.johnson@example.com");
        customer2.setPhone("555-0102");
        customer2.setBudgetMin(new BigDecimal("800000"));
        customer2.setBudgetMax(new BigDecimal("2000000"));
        customer2.setNotes("High net worth client. Interested in waterfront properties with luxury amenities.");
        customer2.setLeadSource("Referral");
        customer2.setAgent(carol);
        customer2 = customerRepository.save(customer2);

        // Customer 3: Michael Williams - First-time buyer
        Customer customer3 = new Customer();
        customer3.setFirstName("Michael");
        customer3.setLastName("Williams");
        customer3.setEmail("michael.williams@example.com");
        customer3.setPhone("555-0103");
        customer3.setBudgetMin(new BigDecimal("150000"));
        customer3.setBudgetMax(new BigDecimal("250000"));
        customer3.setNotes("First-time buyer. Pre-approved for FHA loan. Looking for starter home.");
        customer3.setLeadSource("Open House");
        customer3.setAgent(alice);
        customer3 = customerRepository.save(customer3);

        // Customer 4: Jennifer Davis - Urban professional
        Customer customer4 = new Customer();
        customer4.setFirstName("Jennifer");
        customer4.setLastName("Davis");
        customer4.setEmail("jennifer.davis@example.com");
        customer4.setPhone("555-0104");
        customer4.setBudgetMin(new BigDecimal("350000"));
        customer4.setBudgetMax(new BigDecimal("550000"));
        customer4.setNotes("Young professional. Wants downtown condo with modern amenities.");
        customer4.setLeadSource("Social Media");
        customer4.setAgent(bob);
        customer4 = customerRepository.save(customer4);

        // Customer 5: Robert & Lisa Martinez - Growing family
        Customer customer5 = new Customer();
        customer5.setFirstName("Robert");
        customer5.setLastName("Martinez");
        customer5.setEmail("rmartinez@example.com");
        customer5.setPhone("555-0105");
        customer5.setBudgetMin(new BigDecimal("450000"));
        customer5.setBudgetMax(new BigDecimal("650000"));
        customer5.setNotes("Family with 2 kids. Need 4+ bedrooms, good schools, backyard for kids.");
        customer5.setLeadSource("Client Referral");
        customer5.setAgent(david);
        customer5 = customerRepository.save(customer5);

        // Customer 6: Emily Thompson - Downsizing retiree
        Customer customer6 = new Customer();
        customer6.setFirstName("Emily");
        customer6.setLastName("Thompson");
        customer6.setEmail("emily.thompson@example.com");
        customer6.setPhone("555-0106");
        customer6.setBudgetMin(new BigDecimal("250000"));
        customer6.setBudgetMax(new BigDecimal("400000"));
        customer6.setNotes("Retiree downsizing. Single-level preferred. Low maintenance.");
        customer6.setLeadSource("Walk-in");
        customer6.setAgent(carol);
        customer6 = customerRepository.save(customer6);

        // Customer 7: David Park - Investment buyer
        Customer customer7 = new Customer();
        customer7.setFirstName("David");
        customer7.setLastName("Park");
        customer7.setEmail("david.park@example.com");
        customer7.setPhone("555-0107");
        customer7.setBudgetMin(new BigDecimal("200000"));
        customer7.setBudgetMax(new BigDecimal("400000"));
        customer7.setNotes("Real estate investor. Looking for rental properties with good ROI.");
        customer7.setLeadSource("Investor Network");
        customer7.setAgent(bob);
        customer7 = customerRepository.save(customer7);

        // Customer 8: Amanda White - Vacation home buyer
        Customer customer8 = new Customer();
        customer8.setFirstName("Amanda");
        customer8.setLastName("White");
        customer8.setEmail("amanda.white@example.com");
        customer8.setPhone("555-0108");
        customer8.setBudgetMin(new BigDecimal("300000"));
        customer8.setBudgetMax(new BigDecimal("600000"));
        customer8.setNotes("Looking for vacation home in mountain or beach area. Weekends and summer use.");
        customer8.setLeadSource("Website Inquiry");
        customer8.setAgent(alice);
        customer8 = customerRepository.save(customer8);

        logger.info("Created 8 diverse demo customers with varying budgets and preferences");

        // Create sample saved searches
        logger.info("Creating sample saved searches...");
        createSampleSavedSearches(customer1, customer2, customer3, customer4, customer5, customer6, customer7, customer8);
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

    private void createSampleSavedSearches(Customer customer1, Customer customer2, Customer customer3,
                                           Customer customer4, Customer customer5, Customer customer6,
                                           Customer customer7, Customer customer8) {
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

            // Search 6: Urban Condos (Customer4 - Jennifer) - Location + Type + BOOLEAN
            PropertyAttribute viewAttr = getAttributeByName("View");
            List<SearchFilterDTO> urbanFilters = new ArrayList<>();

            SearchFilterDTO urbanTypeFilter = new SearchFilterDTO();
            urbanTypeFilter.setAttributeId(propertyTypeAttr.getId());
            urbanTypeFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            urbanTypeFilter.setSelectedValues(List.of("Condo"));
            urbanFilters.add(urbanTypeFilter);

            SearchFilterDTO cityViewFilter = new SearchFilterDTO();
            cityViewFilter.setAttributeId(viewAttr.getId());
            cityViewFilter.setDataType(PropertyDataType.MULTI_SELECT);
            cityViewFilter.setSelectedValues(List.of("City"));
            urbanFilters.add(cityViewFilter);

            SearchFilterDTO airConditioningFilter = new SearchFilterDTO();
            airConditioningFilter.setAttributeId(getAttributeByName("Central Air Conditioning").getId());
            airConditioningFilter.setDataType(PropertyDataType.BOOLEAN);
            airConditioningFilter.setBooleanValue(true);
            urbanFilters.add(airConditioningFilter);

            createSavedSearch(customer4, "Urban Living",
                    "Downtown condos with city views and AC",
                    urbanFilters);

            // Search 7: Large Family Homes (Customer5 - Robert Martinez)
            List<SearchFilterDTO> largeFamilyFilters = new ArrayList<>();

            SearchFilterDTO largeBedrooms = new SearchFilterDTO();
            largeBedrooms.setAttributeId(bedroomsAttr.getId());
            largeBedrooms.setDataType(PropertyDataType.NUMBER);
            largeBedrooms.setMinValue(new BigDecimal("4"));
            largeFamilyFilters.add(largeBedrooms);

            SearchFilterDTO multipleBaths = new SearchFilterDTO();
            multipleBaths.setAttributeId(bathroomsAttr.getId());
            multipleBaths.setDataType(PropertyDataType.NUMBER);
            multipleBaths.setMinValue(new BigDecimal("2.5"));
            largeFamilyFilters.add(multipleBaths);

            SearchFilterDTO largeGarage = new SearchFilterDTO();
            largeGarage.setAttributeId(getAttributeByName("Garage Spaces").getId());
            largeGarage.setDataType(PropertyDataType.NUMBER);
            largeGarage.setMinValue(new BigDecimal("2"));
            largeFamilyFilters.add(largeGarage);

            createSavedSearch(customer5, "Family Dream Home",
                    "Large homes for growing family (4+BR, 2.5+BA, 2-car garage)",
                    largeFamilyFilters);

            // Search 8: Single-Level Homes (Customer6 - Emily Thompson - Retiree)
            PropertyAttribute storiesAttr = getAttributeByName("Stories");
            List<SearchFilterDTO> singleLevelFilters = new ArrayList<>();

            SearchFilterDTO oneStory = new SearchFilterDTO();
            oneStory.setAttributeId(storiesAttr.getId());
            oneStory.setDataType(PropertyDataType.NUMBER);
            oneStory.setMaxValue(new BigDecimal("1"));
            singleLevelFilters.add(oneStory);

            SearchFilterDTO retireeBedroomsFilter = new SearchFilterDTO();
            retireeBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            retireeBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            retireeBedroomsFilter.setMinValue(new BigDecimal("2"));
            retireeBedroomsFilter.setMaxValue(new BigDecimal("3"));
            singleLevelFilters.add(retireeBedroomsFilter);

            createSavedSearch(customer6, "Single-Level Living",
                    "One-story homes perfect for retirement (2-3BR)",
                    singleLevelFilters);

            // Search 9: Investment Properties (Customer7 - David Park)
            PropertyAttribute yearBuiltAttr = getAttributeByName("Year Built");
            List<SearchFilterDTO> investmentFilters = new ArrayList<>();

            SearchFilterDTO investmentBedroomsFilter = new SearchFilterDTO();
            investmentBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            investmentBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            investmentBedroomsFilter.setMinValue(new BigDecimal("2"));
            investmentBedroomsFilter.setMaxValue(new BigDecimal("4"));
            investmentFilters.add(investmentBedroomsFilter);

            SearchFilterDTO investmentSqftFilter = new SearchFilterDTO();
            investmentSqftFilter.setAttributeId(squareFootageAttr.getId());
            investmentSqftFilter.setDataType(PropertyDataType.NUMBER);
            investmentSqftFilter.setMinValue(new BigDecimal("1000"));
            investmentSqftFilter.setMaxValue(new BigDecimal("2000"));
            investmentFilters.add(investmentSqftFilter);

            SearchFilterDTO newerConstructionFilter = new SearchFilterDTO();
            newerConstructionFilter.setAttributeId(yearBuiltAttr.getId());
            newerConstructionFilter.setDataType(PropertyDataType.NUMBER);
            newerConstructionFilter.setMinValue(new BigDecimal("2000"));
            investmentFilters.add(newerConstructionFilter);

            createSavedSearch(customer7, "Rental Investments",
                    "Properties suitable for rental (2-4BR, 1000-2000sqft, post-2000)",
                    investmentFilters);

            // Search 10: Beach/Mountain Vacation Homes (Customer8 - Amanda White)
            List<SearchFilterDTO> vacationFilters = new ArrayList<>();

            SearchFilterDTO vacationBedroomsFilter = new SearchFilterDTO();
            vacationBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            vacationBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            vacationBedroomsFilter.setMinValue(new BigDecimal("3"));
            vacationFilters.add(vacationBedroomsFilter);

            SearchFilterDTO scenicViewFilter = new SearchFilterDTO();
            scenicViewFilter.setAttributeId(viewAttr.getId());
            scenicViewFilter.setDataType(PropertyDataType.MULTI_SELECT);
            scenicViewFilter.setSelectedValues(List.of("Ocean", "Mountain", "Lake"));
            vacationFilters.add(scenicViewFilter);

            SearchFilterDTO vacationPoolFilter = new SearchFilterDTO();
            vacationPoolFilter.setAttributeId(hasPoolAttr.getId());
            vacationPoolFilter.setDataType(PropertyDataType.BOOLEAN);
            vacationPoolFilter.setBooleanValue(true);
            vacationFilters.add(vacationPoolFilter);

            createSavedSearch(customer8, "Vacation Getaway",
                    "Scenic vacation homes with pool (3+BR, ocean/mountain/lake view)",
                    vacationFilters);

            // Search 11: Sarah's Beachfront Search
            PropertyAttribute propertyStatusAttr = getAttributeByName("Property Status");
            List<SearchFilterDTO> beachfrontFilters = new ArrayList<>();

            SearchFilterDTO beachViewFilter = new SearchFilterDTO();
            beachViewFilter.setAttributeId(viewAttr.getId());
            beachViewFilter.setDataType(PropertyDataType.MULTI_SELECT);
            beachViewFilter.setSelectedValues(List.of("Ocean"));
            beachfrontFilters.add(beachViewFilter);

            SearchFilterDTO luxuryBedroomsFilter = new SearchFilterDTO();
            luxuryBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            luxuryBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            luxuryBedroomsFilter.setMinValue(new BigDecimal("4"));
            beachfrontFilters.add(luxuryBedroomsFilter);

            SearchFilterDTO activeStatusFilter = new SearchFilterDTO();
            activeStatusFilter.setAttributeId(propertyStatusAttr.getId());
            activeStatusFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            activeStatusFilter.setSelectedValues(List.of("Active"));
            beachfrontFilters.add(activeStatusFilter);

            createSavedSearch(customer2, "Beachfront Luxury Estates",
                    "High-end oceanfront properties (4+BR, active listings)",
                    beachfrontFilters);

            // Search 12: Customer3's Affordable Options
            List<SearchFilterDTO> affordableFilters = new ArrayList<>();

            SearchFilterDTO affordableBedroomsFilter = new SearchFilterDTO();
            affordableBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            affordableBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            affordableBedroomsFilter.setMinValue(new BigDecimal("2"));
            affordableBedroomsFilter.setMaxValue(new BigDecimal("3"));
            affordableFilters.add(affordableBedroomsFilter);

            SearchFilterDTO affordableSqftFilter = new SearchFilterDTO();
            affordableSqftFilter.setAttributeId(squareFootageAttr.getId());
            affordableSqftFilter.setDataType(PropertyDataType.NUMBER);
            affordableSqftFilter.setMaxValue(new BigDecimal("1500"));
            affordableFilters.add(affordableSqftFilter);

            SearchFilterDTO affordableTypeFilter = new SearchFilterDTO();
            affordableTypeFilter.setAttributeId(propertyTypeAttr.getId());
            affordableTypeFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            affordableTypeFilter.setSelectedValues(List.of("Single Family Home", "Townhouse", "Condo"));
            affordableFilters.add(affordableTypeFilter);

            createSavedSearch(customer3, "Budget-Friendly Homes",
                    "Affordable starter homes (2-3BR, under 1500sqft, various types)",
                    affordableFilters);

            // Search 13: Customer1's Pool Home Search
            List<SearchFilterDTO> poolHomeFilters = new ArrayList<>();

            SearchFilterDTO poolBedroomsFilter = new SearchFilterDTO();
            poolBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            poolBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            poolBedroomsFilter.setMinValue(new BigDecimal("3"));
            poolBedroomsFilter.setMaxValue(new BigDecimal("4"));
            poolHomeFilters.add(poolBedroomsFilter);

            SearchFilterDTO mustHavePoolFilter = new SearchFilterDTO();
            mustHavePoolFilter.setAttributeId(hasPoolAttr.getId());
            mustHavePoolFilter.setDataType(PropertyDataType.BOOLEAN);
            mustHavePoolFilter.setBooleanValue(true);
            poolHomeFilters.add(mustHavePoolFilter);

            SearchFilterDTO backyardFilter = new SearchFilterDTO();
            backyardFilter.setAttributeId(getAttributeByName("Has Deck/Patio").getId());
            backyardFilter.setDataType(PropertyDataType.BOOLEAN);
            backyardFilter.setBooleanValue(true);
            poolHomeFilters.add(backyardFilter);

            createSavedSearch(customer1, "Summer Fun Homes",
                    "Family homes with pool and outdoor space (3-4BR, pool, deck/patio)",
                    poolHomeFilters);

            // Search 14: Customer4's Second Option - Walk to Work
            PropertyAttribute walkabilityAttr = getAttributeByName("Walkability Score");
            List<SearchFilterDTO> walkableFilters = new ArrayList<>();

            SearchFilterDTO walkableBedroomsFilter = new SearchFilterDTO();
            walkableBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            walkableBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            walkableBedroomsFilter.setMinValue(new BigDecimal("1"));
            walkableBedroomsFilter.setMaxValue(new BigDecimal("2"));
            walkableFilters.add(walkableBedroomsFilter);

            SearchFilterDTO highWalkabilityFilter = new SearchFilterDTO();
            highWalkabilityFilter.setAttributeId(walkabilityAttr.getId());
            highWalkabilityFilter.setDataType(PropertyDataType.NUMBER);
            highWalkabilityFilter.setMinValue(new BigDecimal("80"));
            walkableFilters.add(highWalkabilityFilter);

            createSavedSearch(customer4, "Walkable Urban Living",
                    "Highly walkable urban properties (1-2BR, walkability 80+)",
                    walkableFilters);

            // Search 15: Customer5's School District Focus
            PropertyAttribute schoolDistrictAttr = getAttributeByName("School District");
            PropertyAttribute specialRoomsAttr = getAttributeByName("Special Rooms");
            List<SearchFilterDTO> schoolFocusFilters = new ArrayList<>();

            SearchFilterDTO schoolBedroomsFilter = new SearchFilterDTO();
            schoolBedroomsFilter.setAttributeId(bedroomsAttr.getId());
            schoolBedroomsFilter.setDataType(PropertyDataType.NUMBER);
            schoolBedroomsFilter.setMinValue(new BigDecimal("4"));
            schoolFocusFilters.add(schoolBedroomsFilter);

            SearchFilterDTO familyTypeFilter = new SearchFilterDTO();
            familyTypeFilter.setAttributeId(propertyTypeAttr.getId());
            familyTypeFilter.setDataType(PropertyDataType.SINGLE_SELECT);
            familyTypeFilter.setSelectedValues(List.of("Single Family Home"));
            schoolFocusFilters.add(familyTypeFilter);

            SearchFilterDTO homeOfficeFilter = new SearchFilterDTO();
            homeOfficeFilter.setAttributeId(specialRoomsAttr.getId());
            homeOfficeFilter.setDataType(PropertyDataType.MULTI_SELECT);
            homeOfficeFilter.setSelectedValues(List.of("Home Office"));
            schoolFocusFilters.add(homeOfficeFilter);

            createSavedSearch(customer5, "Family Homes with Office",
                    "Single-family homes for remote work (4+BR, home office)",
                    schoolFocusFilters);

            logger.info("Created 15 comprehensive saved searches for 8 customers demonstrating all filter types and real-world scenarios");

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