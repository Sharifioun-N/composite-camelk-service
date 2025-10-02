package com.example.composite.api;

import com.example.composite.domain.Certificate;
import java.util.List;

public record UserCertificates(List<Certificate> valid, List<Certificate> invalid) {}
