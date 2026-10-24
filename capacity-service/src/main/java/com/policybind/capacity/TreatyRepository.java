package com.policybind.capacity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TreatyRepository extends JpaRepository<TreatyEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE treaties SET held_cents = held_cents + :amount WHERE code = :code AND held_cents + :amount + bound_cents <= limit_cents", nativeQuery = true)
    int hold(@Param("code") String code, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE treaties SET held_cents = held_cents - :amount WHERE code = :code AND held_cents >= :amount", nativeQuery = true)
    int release(@Param("code") String code, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE treaties SET held_cents = held_cents - :amount, bound_cents = bound_cents + :amount WHERE code = :code AND held_cents >= :amount", nativeQuery = true)
    int commitHold(@Param("code") String code, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE treaties SET bound_cents = bound_cents - :amount WHERE code = :code AND bound_cents >= :amount", nativeQuery = true)
    int giveBack(@Param("code") String code, @Param("amount") int amount);
}
