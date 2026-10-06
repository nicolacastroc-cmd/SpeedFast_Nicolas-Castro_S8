package modelo;

public class PedidoEncomienda extends Pedido {
    private double pesoKg;
    private double distanciaKm;

    public PedidoEncomienda(int idPedido, String cliente, String direccionEntrega,
                            double pesoKg, double distanciaKm) {
        super(idPedido, cliente, direccionEntrega);
        this.pesoKg = pesoKg;
        this.distanciaKm = distanciaKm;
    }

    @Override
    public void calcularTiempoEntrega() {
        tiempoEntregaEstimado = distanciaKm * 3;
        if (pesoKg > 10) {
            tiempoEntregaEstimado += (pesoKg - 10);
        }
    }

    @Override
    public void asignarRepartidor() {
        repartidorAsignado = "En camion (asignación automatica)";
        System.out.println("   [Encomienda] Se asignó automáticamente un repartidor en furgón.");
    }

    @Override
    public String getTipo() {
        return "Encomienda";
    }
}
