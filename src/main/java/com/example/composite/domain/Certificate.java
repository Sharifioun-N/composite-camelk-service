package com.example.composite.domain;

import java.time.LocalDate;

public record Certificate(
        String serial,
        String subject,
        LocalDate notBefore,
        LocalDate notAfter,
        boolean active
) {}
