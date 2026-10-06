package modelo;

public class PedidoFactory {

    private PedidoFactory() {
    }

    public static Pedido crear(int id, String direccion, String tipo, String estado) {
        Pedido pedido;
        if ("COMIDA".equalsIgnoreCase(tipo)) {
            pedido = new PedidoComida(id, "", direccion, 0, false);
        } else if ("ENCOMIENDA".equalsIgnoreCase(tipo)) {
            pedido = new PedidoEncomienda(id, "", direccion, 0, 0);
        } else if ("EXPRESS".equalsIgnoreCase(tipo)) {
            pedido = new PedidoExpress(id, "", direccion, 0);
        } else {
            throw new IllegalArgumentException("Tipo de pedido no válido: " + tipo);
        }
        pedido.setEstado(estado);
        return pedido;
    }
}

