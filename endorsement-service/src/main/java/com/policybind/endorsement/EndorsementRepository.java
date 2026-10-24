package com.policybind.endorsement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EndorsementRepository extends JpaRepository<EndorsementEntity, String> {
}
