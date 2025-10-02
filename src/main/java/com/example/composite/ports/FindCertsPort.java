package com.example.composite.ports;

import com.example.composite.domain.Certificate;
import java.util.List;

public interface FindCertsPort {
    List<Certificate> findCerts(String username, boolean active);
}
