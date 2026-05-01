package com.sysmap.hubapi.dto.request;

import java.util.UUID;

public record ApproveRequest(UUID participantId, boolean approved) {}
