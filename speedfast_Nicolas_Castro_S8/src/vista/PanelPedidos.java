package vista;

import dao.PedidoDAO;
import modelo.Pedido;
import modelo.PedidoFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelPedidos extends JPanel {

    private static final int LARGO_MAXIMO_DIRECCION = 150;
    private static final String TODOS = "TODOS";
    private static final String[] TIPOS = {"COMIDA", "ENCOMIENDA", "EXPRESS"};
    private static final String[] ESTADOS = {"PENDIENTE", "EN_REPARTO", "ENTREGADO"};

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final Runnable alCambiarDatos;

    private final JTextField campoDireccion = new JTextField(25);
    private final JComboBox<String> comboTipo = new JComboBox<>(TIPOS);
    private final JComboBox<String> comboEstado = new JComboBox<>(ESTADOS);
    private final JComboBox<String> filtroEstado = new JComboBox<>(conTodos(ESTADOS));
    private final JComboBox<String> filtroTipo = new JComboBox<>(conTodos(TIPOS));

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Dirección", "Tipo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PanelPedidos(Runnable alCambiarDatos) {
        this.alCambiarDatos = alCambiarDatos;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        formulario.add(new JLabel("Dirección:"));
        formulario.add(campoDireccion);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(comboTipo);
        formulario.add(new JLabel("Estado:"));
        formulario.add(comboEstado);

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
        filtros.add(new JLabel("Filtrar por estado:"));
        filtros.add(filtroEstado);
        filtros.add(new JLabel("Filtrar por tipo:"));
        filtros.add(filtroTipo);
        JButton btnFiltrar = new JButton("Aplicar filtros");
        JButton btnQuitarFiltros = new JButton("Quitar filtros");
        filtros.add(btnFiltrar);
        filtros.add(btnQuitarFiltros);

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
        btnQuitarFiltros.addActionListener(e -> {
            filtroEstado.setSelectedIndex(0);
            filtroTipo.setSelectedIndex(0);
            refrescarTabla();
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaEnFormulario();
            }
        });

        refrescarTabla();
    }



    private void registrar() {
        String direccion = validarDireccion();
        if (direccion == null) {
            return;
        }
        try {
            Pedido pedido = PedidoFactory.crear(0, direccion,
                    (String) comboTipo.getSelectedItem(), (String) comboEstado.getSelectedItem());
            pedidoDAO.create(pedido);
            Dialogos.info(this, "Pedido #" + pedido.getIdPedido() + " registrado.");
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar el pedido", ex);
        }
    }

    private void editar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona un pedido de la tabla para editarlo.");
            return;
        }
        String direccion = validarDireccion();
        if (direccion == null) {
            return;
        }
        try {
            Pedido pedido = PedidoFactory.crear(id, direccion,
                    (String) comboTipo.getSelectedItem(), (String) comboEstado.getSelectedItem());
            if (pedidoDAO.update(pedido)) {
                Dialogos.info(this, "Pedido #" + id + " actualizado.");
            } else {
                Dialogos.datoInvalido(this, "El pedido #" + id + " ya no existe.");
            }
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar el pedido", ex);
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona un pedido de la tabla para eliminarlo.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Eliminar el pedido #" + id + "?")) {
            return;
        }
        try {
            if (pedidoDAO.delete(id)) {
                Dialogos.info(this, "Pedido #" + id + " eliminado.");
            }
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar el pedido", ex);
        }
    }

    private String validarDireccion() {
        String direccion = campoDireccion.getText().trim();
        if (direccion.isEmpty()) {
            Dialogos.datoInvalido(this, "La dirección del pedido es obligatoria.");
            return null;
        }
        if (direccion.length() < 5) {
            Dialogos.datoInvalido(this, "La dirección es demasiado corta (mínimo 5 caracteres).");
            return null;
        }
        if (direccion.length() > LARGO_MAXIMO_DIRECCION) {
            Dialogos.datoInvalido(this, "La dirección no puede superar los " + LARGO_MAXIMO_DIRECCION + " caracteres.");
            return null;
        }
        return direccion;
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : (int) modeloTabla.getValueAt(fila, 0);
    }

    private void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            campoDireccion.setText((String) modeloTabla.getValueAt(fila, 1));
            comboTipo.setSelectedItem(modeloTabla.getValueAt(fila, 2));
            comboEstado.setSelectedItem(modeloTabla.getValueAt(fila, 3));
        }
    }

    private void limpiarFormulario() {
        campoDireccion.setText("");
        comboTipo.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
        tabla.clearSelection();
        campoDireccion.requestFocus();
    }

    private void refrescarTodo() {
        refrescarTabla();
        alCambiarDatos.run();
    }


    public void refrescarTabla() {
        modeloTabla.setRowCount(0);
        String estado = TODOS.equals(filtroEstado.getSelectedItem()) ? null : (String) filtroEstado.getSelectedItem();
        String tipo = TODOS.equals(filtroTipo.getSelectedItem()) ? null : (String) filtroTipo.getSelectedItem();
        try {
            for (Pedido p : pedidoDAO.readAll(estado, tipo)) {
                modeloTabla.addRow(new Object[]{
                        p.getIdPedido(), p.getDireccionEntrega(), p.getTipo().toUpperCase(), p.getEstado()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los pedidos", ex);
        }
    }

    private static String[] conTodos(String[] opciones) {
        String[] resultado = new String[opciones.length + 1];
        resultado[0] = TODOS;
        System.arraycopy(opciones, 0, resultado, 1, opciones.length);
        return resultado;
    }
}