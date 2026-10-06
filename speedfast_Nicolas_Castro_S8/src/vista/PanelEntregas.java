package vista;

import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;
import modelo.Entrega;
import modelo.Pedido;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PanelEntregas extends JPanel {

    private static final String SIN_FILTRO = "Todas";
    private static final String POR_PEDIDO = "Por pedido";
    private static final String POR_REPARTIDOR = "Por repartidor";

    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();

    private final JComboBox<ItemCombo> comboPedido = new JComboBox<>();
    private final JComboBox<ItemCombo> comboRepartidor = new JComboBox<>();
    private final JTextField campoFecha = new JTextField(9);
    private final JTextField campoHora = new JTextField(7);

    private final JComboBox<String> comboModoFiltro =
            new JComboBox<>(new String[]{SIN_FILTRO, POR_PEDIDO, POR_REPARTIDOR});
    private final JComboBox<ItemCombo> comboValorFiltro = new JComboBox<>();


    private final Map<Integer, String> textoPedidos = new HashMap<>();
    private final Map<Integer, String> textoRepartidores = new HashMap<>();
    private boolean cargandoCombos = false; // evita refrescos en cascada mientras se llenan los combos

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Pedido", "Repartidor", "Fecha", "Hora"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final java.util.ArrayList<Entrega> entregasMostradas = new java.util.ArrayList<>();

    public PanelEntregas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        formulario.add(new JLabel("Pedido:"));
        formulario.add(comboPedido);
        formulario.add(new JLabel("Repartidor:"));
        formulario.add(comboRepartidor);
        formulario.add(new JLabel("Fecha (aaaa-mm-dd):"));
        formulario.add(campoFecha);
        formulario.add(new JLabel("Hora (hh:mm):"));
        formulario.add(campoHora);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.add(btnRegistrar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.add(new JLabel("Listar entregas:"));
        filtros.add(comboModoFiltro);
        filtros.add(comboValorFiltro);
        JButton btnFiltrar = new JButton("Aplicar filtro");
        filtros.add(btnFiltrar);

        JPanel norte = new JPanel(new GridLayout(3, 1));
        norte.add(formulario);
        norte.add(botones);
        norte.add(filtros);
        add(norte, BorderLayout.NORTH);

        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);


        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnFiltrar.addActionListener(e -> refrescarTabla());
        comboModoFiltro.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarComboFiltro();
                refrescarTabla();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaEnFormulario();
            }
        });

        limpiarFormulario();
        recargarDatos();
    }


    private void registrar() {
        Entrega entrega = construirEntregaValidada(0);
        if (entrega == null) {
            return;
        }
        try {
            entregaDAO.create(entrega);
            Dialogos.info(this, "Entrega #" + entrega.getId() + " registrada.");
            limpiarFormulario();
            refrescarTabla();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar la entrega", ex);
        }
    }

    private void editar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona una entrega de la tabla para editarla.");
            return;
        }
        Entrega entrega = construirEntregaValidada(id);
        if (entrega == null) {
            return;
        }
        try {
            if (entregaDAO.update(entrega)) {
                Dialogos.info(this, "Entrega #" + id + " actualizada.");
            } else {
                Dialogos.datoInvalido(this, "La entrega #" + id + " ya no existe.");
            }
            limpiarFormulario();
            refrescarTabla();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar la entrega", ex);
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona una entrega de la tabla para eliminarla.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Eliminar la entrega #" + id + "?")) {
            return;
        }
        try {
            if (entregaDAO.delete(id)) {
                Dialogos.info(this, "Entrega #" + id + " eliminada.");
            }
            limpiarFormulario();
            refrescarTabla();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar la entrega", ex);
        }
    }


    private Entrega construirEntregaValidada(int id) {
        ItemCombo pedido = (ItemCombo) comboPedido.getSelectedItem();
        ItemCombo repartidor = (ItemCombo) comboRepartidor.getSelectedItem();

        if (pedido == null) {
            Dialogos.datoInvalido(this, "No hay pedidos disponibles. Registra un pedido primero.");
            return null;
        }
        if (repartidor == null) {
            Dialogos.datoInvalido(this, "No hay repartidores disponibles. Registra un repartidor primero.");
            return null;
        }

        String textoFecha = campoFecha.getText().trim();
        String textoHora = campoHora.getText().trim();
        if (textoFecha.isEmpty() || textoHora.isEmpty()) {
            Dialogos.datoInvalido(this, "La fecha y la hora son obligatorias.");
            return null;
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(textoFecha);
        } catch (DateTimeParseException ex) {
            Dialogos.datoInvalido(this, "La fecha debe tener el formato aaaa-mm-dd (ejemplo: 2025-03-27).");
            return null;
        }
        try {
            hora = LocalTime.parse(textoHora);
        } catch (DateTimeParseException ex) {
            Dialogos.datoInvalido(this, "La hora debe tener el formato hh:mm (ejemplo: 14:30).");
            return null;
        }

        Entrega entrega = new Entrega(pedido.getId(), repartidor.getId(), fecha, hora);
        entrega.setId(id);
        return entrega;
    }

    public void recargarDatos() {
        cargandoCombos = true;
        try {
            int pedidoPrevio = idDe(comboPedido);
            int repartidorPrevio = idDe(comboRepartidor);

            List<Pedido> pedidos = pedidoDAO.readAll();
            List<Repartidor> repartidores = repartidorDAO.readAll();

            comboPedido.removeAllItems();
            comboRepartidor.removeAllItems();
            textoPedidos.clear();
            textoRepartidores.clear();

            for (Pedido p : pedidos) {
                String texto = p.getIdPedido() + " - " + p.getDireccionEntrega();
                textoPedidos.put(p.getIdPedido(), texto);
                comboPedido.addItem(new ItemCombo(p.getIdPedido(), texto));
            }
            for (Repartidor r : repartidores) {
                String texto = r.getId() + " - " + r.getNombre();
                textoRepartidores.put(r.getId(), texto);
                comboRepartidor.addItem(new ItemCombo(r.getId(), texto));
            }

            seleccionarPorId(comboPedido, pedidoPrevio);
            seleccionarPorId(comboRepartidor, repartidorPrevio);
            cargarComboFiltro();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "cargar pedidos y repartidores", ex);
        } finally {
            cargandoCombos = false;
        }
        refrescarTabla();
    }


    private void cargarComboFiltro() {
        int previo = idDe(comboValorFiltro);
        comboValorFiltro.removeAllItems();
        String modo = (String) comboModoFiltro.getSelectedItem();
        Map<Integer, String> origen = POR_PEDIDO.equals(modo) ? textoPedidos
                : POR_REPARTIDOR.equals(modo) ? textoRepartidores : null;

        comboValorFiltro.setEnabled(origen != null);
        if (origen != null) {
            origen.forEach((id, texto) -> comboValorFiltro.addItem(new ItemCombo(id, texto)));
            comboValorFiltro.setSelectedIndex(-1);
            seleccionarPorId(comboValorFiltro, previo);
            if (comboValorFiltro.getSelectedIndex() < 0 && comboValorFiltro.getItemCount() > 0) {
                comboValorFiltro.setSelectedIndex(0);
            }
        }
    }

    public void refrescarTabla() {
        modeloTabla.setRowCount(0);
        entregasMostradas.clear();
        try {
            String modo = (String) comboModoFiltro.getSelectedItem();
            ItemCombo valor = (ItemCombo) comboValorFiltro.getSelectedItem();

            List<Entrega> entregas;
            if (POR_PEDIDO.equals(modo) && valor != null) {
                entregas = entregaDAO.readByPedido(valor.getId());
            } else if (POR_REPARTIDOR.equals(modo) && valor != null) {
                entregas = entregaDAO.readByRepartidor(valor.getId());
            } else {
                entregas = entregaDAO.readAll();
            }

            for (Entrega e : entregas) {
                entregasMostradas.add(e);
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        textoPedidos.getOrDefault(e.getIdPedido(), String.valueOf(e.getIdPedido())),
                        textoRepartidores.getOrDefault(e.getIdRepartidor(), String.valueOf(e.getIdRepartidor())),
                        e.getFecha(),
                        e.getHora()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar las entregas", ex);
        }
    }


    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : (int) modeloTabla.getValueAt(fila, 0);
    }

    private void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && fila < entregasMostradas.size()) {
            Entrega e = entregasMostradas.get(fila);
            seleccionarPorId(comboPedido, e.getIdPedido());
            seleccionarPorId(comboRepartidor, e.getIdRepartidor());
            campoFecha.setText(e.getFecha().toString());
            campoHora.setText(e.getHora().withSecond(0).withNano(0).toString());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoFecha.setText(LocalDate.now().toString());
        campoHora.setText(LocalTime.now().withSecond(0).withNano(0).toString());
    }

    private static int idDe(JComboBox<ItemCombo> combo) {
        ItemCombo item = (ItemCombo) combo.getSelectedItem();
        return item == null ? -1 : item.getId();
    }

    private static void seleccionarPorId(JComboBox<ItemCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
