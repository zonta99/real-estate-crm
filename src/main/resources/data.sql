-- Initial Property Attributes for Real Estate CRM
-- This data will be loaded on application startup

-- Insert initial admin user (password: admin123 - BCrypt encoded)
INSERT INTO users (username, password, email, first_name, last_name, role, status, created_date, updated_date) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5ojI3Xf3b5/A/V7kBr.u', 'admin@realestatecrm.com', 'System', 'Administrator', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- ========================================
-- BASIC CATEGORY ATTRIBUTES
-- ========================================

-- Address (TEXT, Required)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Address', 'TEXT', true, true, 'BASIC', 1, NOW(), NOW());

-- Property Type (SINGLE_SELECT, Required)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Property Type', 'SINGLE_SELECT', true, true, 'BASIC', 2, NOW(), NOW());

-- City (TEXT, Required)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('City', 'TEXT', true, true, 'BASIC', 3, NOW(), NOW());

-- State (TEXT, Required)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('State', 'TEXT', true, true, 'BASIC', 4, NOW(), NOW());

-- ZIP Code (TEXT, Required)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('ZIP Code', 'TEXT', true, true, 'BASIC', 5, NOW(), NOW());

-- ========================================
-- STRUCTURE CATEGORY ATTRIBUTES
-- ========================================

-- Bedrooms (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Bedrooms', 'NUMBER', false, true, 'STRUCTURE', 1, NOW(), NOW());

-- Bathrooms (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Bathrooms', 'NUMBER', false, true, 'STRUCTURE', 2, NOW(), NOW());

-- Square Footage (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Square Footage', 'NUMBER', false, true, 'STRUCTURE', 3, NOW(), NOW());

-- Lot Size (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Lot Size (sq ft)', 'NUMBER', false, true, 'STRUCTURE', 4, NOW(), NOW());

-- Year Built (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Year Built', 'NUMBER', false, true, 'STRUCTURE', 5, NOW(), NOW());

-- Stories (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Stories', 'NUMBER', false, true, 'STRUCTURE', 6, NOW(), NOW());

-- ========================================
-- FEATURES CATEGORY ATTRIBUTES
-- ========================================

-- Garage (BOOLEAN)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Has Garage', 'BOOLEAN', false, true, 'FEATURES', 1, NOW(), NOW());

-- Pool (BOOLEAN)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Has Pool', 'BOOLEAN', false, true, 'FEATURES', 2, NOW(), NOW());

-- Fireplace (BOOLEAN)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Has Fireplace', 'BOOLEAN', false, true, 'FEATURES', 3, NOW(), NOW());

-- Central Air (BOOLEAN)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Central Air Conditioning', 'BOOLEAN', false, true, 'FEATURES', 4, NOW(), NOW());

-- Basement (SINGLE_SELECT)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Basement Type', 'SINGLE_SELECT', false, true, 'FEATURES', 5, NOW(), NOW());

-- Appliances (MULTI_SELECT)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Included Appliances', 'MULTI_SELECT', false, true, 'FEATURES', 6, NOW(), NOW());

-- ========================================
-- LOCATION CATEGORY ATTRIBUTES
-- ========================================

-- School District (TEXT)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('School District', 'TEXT', false, true, 'LOCATION', 1, NOW(), NOW());

-- Neighborhood (TEXT)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Neighborhood', 'TEXT', false, true, 'LOCATION', 2, NOW(), NOW());

-- ========================================
-- FINANCIAL CATEGORY ATTRIBUTES
-- ========================================

-- Property Tax (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('Annual Property Tax', 'NUMBER', false, true, 'FINANCIAL', 1, NOW(), NOW());

-- HOA Fee (NUMBER)
INSERT INTO property_attributes (name, data_type, is_required, is_searchable, category, display_order, created_date, updated_date) VALUES
    ('HOA Fee (Monthly)', 'NUMBER', false, true, 'FINANCIAL', 2, NOW(), NOW());

-- ========================================
-- PROPERTY ATTRIBUTE OPTIONS
-- ========================================

-- Property Type options
INSERT INTO property_attribute_options (attribute_id, option_value, display_order) VALUES
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'House', 1),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'Condo', 2),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'Townhouse', 3),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'Duplex', 4),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'Land', 5),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Property Type'), 'Commercial', 6);

-- Basement Type options
INSERT INTO property_attribute_options (attribute_id, option_value, display_order) VALUES
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Basement Type'), 'None', 1),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Basement Type'), 'Partial', 2),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Basement Type'), 'Full', 3),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Basement Type'), 'Finished', 4),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Basement Type'), 'Walk-out', 5);

-- Included Appliances options
INSERT INTO property_attribute_options (attribute_id, option_value, display_order) VALUES
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Refrigerator', 1),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Dishwasher', 2),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Washer', 3),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Dryer', 4),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Range/Oven', 5),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Microwave', 6),
                                                                                       ((SELECT id FROM property_attributes WHERE name = 'Included Appliances'), 'Garbage Disposal', 7);