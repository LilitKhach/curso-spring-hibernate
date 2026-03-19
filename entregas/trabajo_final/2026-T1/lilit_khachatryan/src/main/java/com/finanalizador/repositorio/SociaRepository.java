package com.finanalizador.repositorio;

import com.finanalizador.modelo.Socia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SociaRepository extends JpaRepository<Socia, Long> {
}
