package com.example.demo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repo.ResultadoBusqueda;
import com.example.demo.repo.UsuarioRepository;

/**
 * Expone la MISMA busqueda por email en dos variantes para la demo del
 * "antes y despues":
 *
 *  GET /api/usuarios/vulnerable?email=...   -> concatenacion (explotable)
 *  GET /api/usuarios/seguro?email=...       -> PreparedStatement (a prueba de SQLi)
 *
 * La respuesta incluye el SQL ejecutado para que, en clase, se vea como la
 * inyeccion reescribe la consulta.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/vulnerable")
    public Map<String, Object> vulnerable(@RequestParam String email) {
        return aRespuesta("VULNERABLE (concatenacion de String)", email, repository.buscarVulnerable(email));
    }

    @GetMapping("/seguro")
    public Map<String, Object> seguro(@RequestParam String email) {
        return aRespuesta("SEGURO (PreparedStatement)", email, repository.buscarSeguro(email));
    }

    private Map<String, Object> aRespuesta(String modo, String email, ResultadoBusqueda resultado) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("modo", modo);
        respuesta.put("emailRecibido", email);
        respuesta.put("sqlEjecutado", resultado.sqlEjecutado());
        respuesta.put("totalFilas", resultado.total());
        respuesta.put("usuarios", resultado.usuarios());
        return respuesta;
    }
}
