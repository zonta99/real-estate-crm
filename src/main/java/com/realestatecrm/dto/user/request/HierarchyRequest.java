package com.realestatecrm.dto.user.request;

import jakarta.validation.constraints.NotNull;

public record HierarchyRequest(@NotNull Long supervisorId) {
    public Long getSupervisorId() {
        return supervisorId;
    }
}