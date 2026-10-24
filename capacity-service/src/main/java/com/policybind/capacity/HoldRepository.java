package com.policybind.capacity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldRepository extends JpaRepository<HoldEntity, String> {
}
