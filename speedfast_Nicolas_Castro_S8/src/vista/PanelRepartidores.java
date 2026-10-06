package vista;

import dao.RepartidorDAO;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;


public class PanelRepartidores extends JPanel {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final Runnable alCambiarDatos;
    private final JTextField campoNombre = new JTextField(25);
    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PanelRepartidores(Runnable alCambiarDatos) {
        this.alCambiarDatos = alCambiarDatos;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelSuperior.add(new JLabel("Nombre:"));
        panelSuperior.add(campoNombre);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelSuperior.add(btnRegistrar);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnLimpiar);
        add(panelSuperior, BorderLayout.NORTH);


        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);


        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaEnFormulario();
            }
        });

        refrescarTabla();
    }



    private void registrar() {
        String nombre = validarNombre();
        if (nombre == null) {
            return;
        }
        try {
            Repartidor repartidor = new Repartidor(nombre);
            repartidorDAO.create(repartidor);
            Dialogos.info(this, "Repartidor #" + repartidor.getId() + " (" + nombre + ") registrado.");
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "registrar el repartidor", ex);
        }
    }

    private void editar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona un repartidor de la tabla para editarlo.");
            return;
        }
        String nombre = validarNombre();
        if (nombre == null) {
            return;
        }
        try {
            if (repartidorDAO.update(new Repartidor(id, nombre))) {
                Dialogos.info(this, "Repartidor #" + id + " actualizado.");
            } else {
                Dialogos.datoInvalido(this, "El repartidor #" + id + " ya no existe.");
            }
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "actualizar el repartidor", ex);
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id < 0) {
            Dialogos.datoInvalido(this, "Selecciona un repartidor de la tabla para eliminarlo.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Eliminar al repartidor #" + id + "?")) {
            return;
        }
        try {
            if (repartidorDAO.delete(id)) {
                Dialogos.info(this, "Repartidor #" + id + " eliminado.");
            }
            limpiarFormulario();
            refrescarTodo();
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "eliminar el repartidor", ex);
        }
    }

    private String validarNombre() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            Dialogos.datoInvalido(this, "El nombre del repartidor es obligatorio.");
            return null;
        }
        if (nombre.length() > LARGO_MAXIMO_NOMBRE) {
            Dialogos.datoInvalido(this, "El nombre no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres.");
            return null;
        }
        if (!nombre.matches("[\\p{L} .'-]+")) {
            Dialogos.datoInvalido(this, "El nombre solo puede contener letras, espacios, puntos, apóstrofes o guiones.");
            return null;
        }
        return nombre;
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : (int) modeloTabla.getValueAt(fila, 0);
    }

    private void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            campoNombre.setText((String) modeloTabla.getValueAt(fila, 1));
        }
    }

    private void limpiarFormulario() {
        campoNombre.setText("");
        tabla.clearSelection();
        campoNombre.requestFocus();
    }

    private void refrescarTodo() {
        refrescarTabla();
        alCambiarDatos.run();
    }


    public void refrescarTabla() {
        modeloTabla.setRowCount(0);
        try {
            for (Repartidor r : repartidorDAO.readAll()) {
                modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
            }
        } catch (SQLException ex) {
            Dialogos.errorSQL(this, "consultar los repartidores", ex);
        }
    }
}
