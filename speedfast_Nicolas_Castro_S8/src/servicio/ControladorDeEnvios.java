package servicio;

import interfaces.Rastreable;
import modelo.Pedido;
import java.util.ArrayList;
import java.util.List;

public class ControladorDeEnvios implements Rastreable {

    private final List<Pedido> historialEntregas;

    public ControladorDeEnvios() {
        historialEntregas = new ArrayList<>();
    }


    public void despacharPedido(Pedido pedido) {
        pedido.despachar();
        registrarEnHistorial(pedido);
    }




    public void cancelarPedido(Pedido pedido) {
        pedido.cancelar();
        registrarEnHistorial(pedido);
    }



    private synchronized void registrarEnHistorial(Pedido pedido) {
        if (!historialEntregas.contains(pedido)) {
            historialEntregas.add(pedido);
        }
    }


    @Override
    public synchronized void verHistorial() {
        System.out.println("\nHISTORIAL DE ENTREGAS ");
        if (historialEntregas.isEmpty()) {
            System.out.println("No hay pedidos registrados todavía.");
            return;
        }
        for (Pedido pedido : historialEntregas) {
            pedido.mostrarResumen();
        }
    }
}

