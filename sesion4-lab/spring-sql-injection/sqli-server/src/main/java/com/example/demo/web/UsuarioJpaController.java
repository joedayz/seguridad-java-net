package com.example.demo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.jpa.ResultadoBusquedaJpa;
import com.example.demo.service.UserSearchService;

/**
 * Expone la MISMA busqueda por email con Hibernate/JPA en dos variantes:
 *
 *  GET /api/usuarios-jpa/vulnerable?email=...  -> HQL concatenado (explotable)
 *  GET /api/usuarios-jpa/seguro?email=...        -> HQL con parametro nombrado
 *
 * La respuesta incluye la consulta HQL ejecutada para que, en clase, se vea como
 * la inyeccion reescribe la query.
 */
@RestController
@RequestMapping("/api/usuarios-jpa")
public class UsuarioJpaController {

    private final UserSearchService userSearchService;

    public UsuarioJpaController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/vulnerable")
    public Map<String, Object> vulnerable(@RequestParam String email) {
        return aRespuesta(
                "VULNERABLE (concatenacion de String en HQL)",
                email,
                userSearchService.findUsersVulnerable(email));
    }

    @GetMapping("/seguro")
    public Map<String, Object> seguro(@RequestParam String email) {
        return aRespuesta(
                "SEGURO (HQL con parametro nombrado :email)",
                email,
                userSearchService.findUsersSeguro(email));
    }

    private Map<String, Object> aRespuesta(String modo, String email, ResultadoBusquedaJpa resultado) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("modo", modo);
        respuesta.put("emailRecibido", email);
        respuesta.put("consultaEjecutada", resultado.consultaEjecutada());
        respuesta.put("totalFilas", resultado.total());
        respuesta.put("usuarios", resultado.users());
        return respuesta;
    }
}
