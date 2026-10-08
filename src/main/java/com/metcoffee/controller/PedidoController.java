package com.metcoffee.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metcoffee.dto.PedidoDTO;
import com.metcoffee.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos/{usuarioId}")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	// GET /api/pedidos/{usuarioId} - Historial de pedidos del usuario
	@GetMapping
	public ResponseEntity<List<PedidoDTO>> historial(@PathVariable String usuarioId) {
		return ResponseEntity.ok(pedidoService.historial(usuarioId));
	}

	// GET /api/pedidos/{usuarioId}/{id} - Detalle de un pedido
	@GetMapping("/{id}")
	public ResponseEntity<PedidoDTO> detalle(@PathVariable String usuarioId, @PathVariable String id) {
		return ResponseEntity.ok(pedidoService.detalle(usuarioId, id));
	}

	// DELETE /api/pedidos/{usuarioId}/{id} - Cancelar (solo si está en Procesando)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancelar(@PathVariable String usuarioId, @PathVariable String id) {
		pedidoService.cancelar(usuarioId, id);
		return ResponseEntity.noContent().build();
	}
}
