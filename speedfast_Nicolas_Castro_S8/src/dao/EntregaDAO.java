package dao;

import modelo.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class EntregaDAO {

    private static final String SELECT_BASE =
            "SELECT id, id_pedido, id_repartidor, fecha, hora FROM entrega";


    public void create(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entrega (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, entrega.getIdPedido());
            statement.setInt(2, entrega.getIdRepartidor());
            statement.setDate(3, Date.valueOf(entrega.getFecha()));
            statement.setTime(4, Time.valueOf(entrega.getHora()));
            statement.executeUpdate();

            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    entrega.setId(claves.getInt(1));
                }
            }
        }
    }


    public List<Entrega> readAll() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY id", null);
    }

    public List<Entrega> readByPedido(int idPedido) throws SQLException {
        return consultar(SELECT_BASE + " WHERE id_pedido = ? ORDER BY id", idPedido);
    }

    public List<Entrega> readByRepartidor(int idRepartidor) throws SQLException {
        return consultar(SELECT_BASE + " WHERE id_repartidor = ? ORDER BY id", idRepartidor);
    }


    public boolean update(Entrega entrega) throws SQLException {
        String sql = "UPDATE entrega SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, entrega.getIdPedido());
            statement.setInt(2, entrega.getIdRepartidor());
            statement.setDate(3, Date.valueOf(entrega.getFecha()));
            statement.setTime(4, Time.valueOf(entrega.getHora()));
            statement.setInt(5, entrega.getId());
            return statement.executeUpdate() > 0;
        }
    }


    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM entrega WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Ejecuta un SELECT con un único parámetro entero opcional y arma la lista de entregas.
    private List<Entrega> consultar(String sql, Integer parametro) throws SQLException {
        List<Entrega> entregas = new ArrayList<>();

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            if (parametro != null) {
                statement.setInt(1, parametro);
            }

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Entrega entrega = new Entrega(
                            resultado.getInt("id_pedido"),
                            resultado.getInt("id_repartidor"),
                            resultado.getDate("fecha").toLocalDate(),
                            resultado.getTime("hora").toLocalTime());
                    entrega.setId(resultado.getInt("id"));
                    entregas.add(entrega);
                }
            }
        }
        return entregas;
    }
}
