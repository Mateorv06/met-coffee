package com.metcoffee.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metcoffee.dto.DireccionDTO;
import com.metcoffee.service.DomicilioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/domicilios/{usuarioId}")
public class DomicilioController {

	private final DomicilioService domicilioService;

	public DomicilioController(DomicilioService domicilioService) {
		this.domicilioService = domicilioService;
	}

	// GET /api/domicilios/{usuarioId} - Listar domicilios del usuario
	@GetMapping
	public ResponseEntity<List<DireccionDTO>> listar(@PathVariable String usuarioId) {
		return ResponseEntity.ok(domicilioService.listar(usuarioId));
	}

	// POST /api/domicilios/{usuarioId} - Crear domicilio
	@PostMapping
	public ResponseEntity<DireccionDTO> guardar(@PathVariable String usuarioId, @Valid @RequestBody DireccionDTO dto) {
		return new ResponseEntity<>(domicilioService.guardar(usuarioId, dto), HttpStatus.CREATED);
	}

	// PUT /api/domicilios/{usuarioId}/{id} - Actualizar domicilio
	@PutMapping("/{id}")
	public ResponseEntity<DireccionDTO> actualizar(@PathVariable String usuarioId, @PathVariable String id,
			@Valid @RequestBody DireccionDTO dto) {
		return ResponseEntity.ok(domicilioService.actualizar(usuarioId, id, dto));
	}

	// PUT /api/domicilios/{usuarioId}/{id}/principal - Marcar como domicilio principal
	@PutMapping("/{id}/principal")
	public ResponseEntity<Void> seleccionarPrincipal(@PathVariable String usuarioId, @PathVariable String id) {
		domicilioService.seleccionarPrincipal(usuarioId, id);
		return ResponseEntity.noContent().build();
	}
}
