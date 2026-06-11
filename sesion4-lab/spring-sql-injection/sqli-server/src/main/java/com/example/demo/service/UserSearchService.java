package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.jpa.ResultadoBusquedaJpa;
import com.example.demo.jpa.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Busqueda de usuarios con Hibernate/JPA. Contiene DOS implementaciones para
 * comparar lado a lado:
 *
 *  - {@link #findUsersVulnerable(String)}: ANTES. Concatena la entrada del
 *    usuario en el HQL. Es explotable (HQL injection).
 *  - {@link #findUsersSeguro(String)}: DESPUES. Usa un parametro nombrado
 *    ({@code :email}). Hibernate lo vincula de forma segura.
 */
@Service
public class UserSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /**
     * PELIGRO: concatena la entrada del usuario directamente en el HQL.
     * Un atacante puede alterar la logica de la query (p. ej. {@code ' OR '1'='1})
     * y acceder a datos no autorizados. Usar JPA no protege si el HQL se arma
     * concatenando strings.
     */
    public ResultadoBusquedaJpa findUsersVulnerable(String email) {
        String hql = "FROM User u WHERE u.email = '" + email + "'";

        List<User> users = entityManager
                .createQuery(hql, User.class)
                .getResultList();

        return new ResultadoBusquedaJpa(hql, users);
    }

    // ==========================================================================
    // DESPUES — SEGURO
    // ==========================================================================

    /**
     * SEGURO: usa un parametro nombrado en el HQL. El valor se vincula con
     * {@code setParameter} y Hibernate lo trata como dato, nunca como parte del
     * comando.
     */
    public ResultadoBusquedaJpa findUsersSeguro(String email) {
        String hql = "FROM User u WHERE u.email = :email";

        List<User> users = entityManager
                .createQuery(hql, User.class)
                .setParameter("email", email)
                .getResultList();

        return new ResultadoBusquedaJpa(
                hql + "   [parametro vinculado: " + email + "]",
                users);
    }
}
