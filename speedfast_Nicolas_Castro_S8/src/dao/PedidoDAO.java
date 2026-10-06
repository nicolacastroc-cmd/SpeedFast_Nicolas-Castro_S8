package dao;

import modelo.Pedido;
import modelo.PedidoFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {


    public void create(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (direccion, tipo, estado) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, pedido.getDireccionEntrega());
            statement.setString(2, pedido.getTipo().toUpperCase());
            statement.setString(3, pedido.getEstado());
            statement.executeUpdate();

            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setIdPedido(claves.getInt(1));
                }
            }
        }

    }

    public List<Pedido> readAll() throws SQLException {
        return readAll(null, null);
    }


    public List<Pedido> readAll(String estado, String tipo) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, direccion, tipo, estado FROM pedido WHERE 1=1");
        List<String> parametros = new ArrayList<>();

        if (estado != null && !estado.isEmpty()) {
            sql.append(" AND estado = ?");
            parametros.add(estado);
        }
        if (tipo != null && !tipo.isEmpty()) {
            sql.append(" AND tipo = ?");
            parametros.add(tipo);
        }
        sql.append(" ORDER BY id");

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                statement.setString(i + 1, parametros.get(i));
            }

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    pedidos.add(PedidoFactory.crear(
                            resultado.getInt("id"),
                            resultado.getString("direccion"),
                            resultado.getString("tipo"),
                            resultado.getString("estado")));
                }
            }
        }
        return pedidos;
    }

    public boolean update(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedido SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, pedido.getDireccionEntrega());
            statement.setString(2, pedido.getTipo().toUpperCase());
            statement.setString(3, pedido.getEstado());
            statement.setInt(4, pedido.getIdPedido());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pedido WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}