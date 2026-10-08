package com.metcoffee.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.metcoffee.model.Rol;
import com.metcoffee.model.Usuario;
import com.metcoffee.model.Carrito;
import com.metcoffee.model.Direccion;
import com.metcoffee.model.ItemCarrito;
import com.metcoffee.model.Producto;
import com.metcoffee.repository.CarritoRepository;
import com.metcoffee.repository.DireccionRepository;
import com.metcoffee.repository.PagoRepository;
import com.metcoffee.repository.PedidoRepository;
import com.metcoffee.repository.ProductoRepository;
import com.metcoffee.repository.MovimientoInventarioRepository;
import com.metcoffee.repository.UsuarioRepository;
import com.metcoffee.service.CompraService;
import com.metcoffee.service.InventarioService;
import com.metcoffee.service.PasarelaPago;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.data.mongodb.uri=${MONGODB_TEST_URI:mongodb://127.0.0.1:27018/metcoffee_test?replicaSet=rs0}"
})
@EnabledIfSystemProperty(named = "integration", matches = "true")
class MongoIntegrationTest {

	@Autowired
	UsuarioRepository usuarios;

	@MockBean
	PasarelaPago pasarelaPago;

	@Autowired
	CompraService compraService;

	@Autowired
	CarritoRepository carritos;

	@Autowired
	DireccionRepository direcciones;

	@Autowired
	PagoRepository pagos;

	@Autowired
	PedidoRepository pedidos;

	@Autowired
	ProductoRepository productos;

	@Autowired
	MovimientoInventarioRepository movimientos;

	@SpyBean
	InventarioService inventario;

	@Test
	void guardaYLeeUnDocumentoRealDesdeMongo() {
		String id = UUID.randomUUID().toString();
		Usuario guardado = usuarios.save(new Usuario(id, "mongo_test", id + "@test.local", "hash", "000", Rol.CLIENTE));

		assertEquals(id, usuarios.findById(guardado.getId()).orElseThrow().getId());
		usuarios.deleteById(id);
	}

	@Test
	void revierteLaCompraCompletaSiFallaUnDescuento() {
		String usuario = "rollback-" + UUID.randomUUID();
		String direccion = "direccion-" + UUID.randomUUID();
		String productoUno = "producto-" + UUID.randomUUID();
		String productoDos = "producto-" + UUID.randomUUID();
		long pagosAntes = pagos.count();
		productos.save(new Producto(productoUno, "Cafe uno", 10, 500));
		productos.save(new Producto(productoDos, "Cafe dos", 10, 500));
		direcciones.save(new Direccion(direccion, usuario, "Casa", "Ana", "Calle 1", "300", true));
		Carrito carrito = new Carrito("carrito-" + UUID.randomUUID(), usuario);
		carrito.setItems(List.of(new ItemCarrito(productoUno, 1, "Grano", 125, 5, null),
				new ItemCarrito(productoDos, 1, "Grano", 125, 5, null)));
		carritos.save(carrito);
		when(pasarelaPago.procesar(anyString(), anyDouble())).thenReturn(true);
		AtomicInteger descuentos = new AtomicInteger();
		doAnswer(invocation -> {
			if (descuentos.incrementAndGet() == 2) {
				throw new IllegalStateException("Fallo de inventario simulado");
			}
			return invocation.callRealMethod();
		}).when(inventario).descontar(any(Producto.class), anyDouble(), anyString());

		org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
				() -> compraService.confirmar(usuario, direccion));

		assertTrue(pedidos.findByUsuarioIdOrderByFechaDesc(usuario).isEmpty());
		assertEquals(500, productos.findById(productoUno).orElseThrow().getStockGramos());
		assertEquals(500, productos.findById(productoDos).orElseThrow().getStockGramos());
		assertEquals(pagosAntes, pagos.count(), "el pago aprobado también debe revertirse");
		assertTrue(movimientos.findByProductoIdOrderByFechaDesc(productoUno).isEmpty());
		assertTrue(movimientos.findByProductoIdOrderByFechaDesc(productoDos).isEmpty());
		assertEquals(2, carritos.findByUsuarioId(usuario).orElseThrow().getItems().size());
	}
}
