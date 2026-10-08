package com.metcoffee.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metcoffee.dto.LoginRequest;
import com.metcoffee.dto.RegistroRequest;
import com.metcoffee.dto.UsuarioDTO;
import com.metcoffee.dto.AuthResponse;
import com.metcoffee.model.Carrito;
import com.metcoffee.service.CuentaService;
import com.metcoffee.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

	private final CuentaService cuentaService;
	private final JwtService jwtService;

	public CuentaController(CuentaService cuentaService, JwtService jwtService) {
		this.cuentaService = cuentaService;
		this.jwtService = jwtService;
	}

	// POST /api/cuenta/registro - Registrar un cliente nuevo (CU-01-01)
	@PostMapping("/registro")
	public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody RegistroRequest request) {
		UsuarioDTO creado = cuentaService.registrar(request.nombreUsuario(), request.correo(), request.password(),
				request.telefono());
		return new ResponseEntity<>(creado, HttpStatus.CREATED);
	}

	// POST /api/cuenta/login - Iniciar sesión (CU-01-02)
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
		UsuarioDTO usuario = cuentaService.iniciarSesion(request.correo(), request.password());
		return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(usuario), usuario));
	}

	// POST /api/cuenta/{usuarioId}/fusionar-carrito - Fusionar el carrito del visitante al iniciar sesión
	@PostMapping("/{usuarioId}/fusionar-carrito")
	public ResponseEntity<Void> fusionarCarrito(@PathVariable String usuarioId, @RequestBody Carrito visitante) {
		cuentaService.fusionarCarrito(usuarioId, visitante);
		return ResponseEntity.noContent().build();
	}
}
