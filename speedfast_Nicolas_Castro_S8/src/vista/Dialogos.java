package vista;

import javax.swing.*;
import java.awt.Component;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;


public final class Dialogos {

    private Dialogos() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SpeedFast", JOptionPane.INFORMATION_MESSAGE);
    }


    public static void datoInvalido(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }


    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }


    public static void errorSQL(Component padre, String accion, SQLException ex) {
        String detalle;
        if (ex instanceof SQLIntegrityConstraintViolationException) {
            detalle = "El registro está relacionado con otros datos (por ejemplo, entregas).\n"
                    + "Elimina o modifica primero los registros asociados.";
        } else if (ex.getSQLState() != null && ex.getSQLState().startsWith("08")) {
            detalle = "No hay conexión con la base de datos.\n"
                    + "Revisa que MySQL esté encendido y que ConexionDB tenga los datos correctos.";
        } else {
            detalle = ex.getMessage();
        }
        JOptionPane.showMessageDialog(padre, "No se pudo " + accion + ":\n" + detalle,
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
