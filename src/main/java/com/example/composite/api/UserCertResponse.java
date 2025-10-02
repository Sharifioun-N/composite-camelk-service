package com.example.composite.api;

import java.util.List;

public record UserCertResponse(String serialNumber, List<UserEntry> users) {}
