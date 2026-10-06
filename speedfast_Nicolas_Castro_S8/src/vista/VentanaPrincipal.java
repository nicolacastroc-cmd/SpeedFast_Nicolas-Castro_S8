package vista;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final PanelEntregas panelEntregas = new PanelEntregas();
    private final PanelRepartidores panelRepartidores = new PanelRepartidores(panelEntregas::recargarDatos);
    private final PanelPedidos panelPedidos = new PanelPedidos(panelEntregas::recargarDatos);

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de pedidos, repartidores y entregas");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 480);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Repartidores", panelRepartidores);
        pestanas.addTab("Pedidos", panelPedidos);
        pestanas.addTab("Entregas", panelEntregas);

        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelEntregas) {
                panelEntregas.recargarDatos();
            }
        });

        add(pestanas, BorderLayout.CENTER);
    }
}
