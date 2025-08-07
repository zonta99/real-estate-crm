package com.realestatecrm.entity;

import java.util.List;

public record Permission(
        String resource,
        List<String> actions
) {}