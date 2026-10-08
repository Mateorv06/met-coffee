package com.metcoffee.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metcoffee.dto.ConfirmarCompraRequest;
import com.metcoffee.dto.PedidoDTO;
import com.metcoffee.service.CompraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

	private final CompraService compraService;

	public CompraController(CompraService compraService) {
		this.compraService = compraService;
	}

	// POST /api/compras/{usuarioId} - Confirmar la compra del carrito (pago simulado + pedido + stock)
	@PostMapping("/{usuarioId}")
	public ResponseEntity<PedidoDTO> confirmar(@PathVariable String usuarioId,
			@Valid @RequestBody ConfirmarCompraRequest request) {
		return new ResponseEntity<>(compraService.confirmar(usuarioId, request.direccionId()), HttpStatus.CREATED);
	}
}
