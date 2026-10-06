package main;

import dao.ConexionDB;
import vista.VentanaPrincipal;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                ConexionDB.probarConexion();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos speedfast_db:\n" + ex.getMessage()
                                + "\n\nLa aplicación se abrirá, pero las operaciones fallarán hasta que la conexión funcione.",
                        "Error de conexión", JOptionPane.WARNING_MESSAGE);
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
