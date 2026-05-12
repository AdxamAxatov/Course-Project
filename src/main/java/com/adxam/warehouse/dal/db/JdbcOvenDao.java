package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.entity.Oven;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcOvenDao extends JdbcApplianceDao<Oven> {

    public JdbcOvenDao(Database db) {
        super(db);
    }

    @Override
    protected String table() { return "ovens"; }

    @Override
    protected String selectColumns() {
        return "id, name, weight, price, quantity, power_consumption, capacity";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO ovens (name, weight, price, quantity, power_consumption, capacity) "
             + "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected Oven map(ResultSet rs) throws SQLException {
        return new Oven()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setWeight(rs.getDouble("weight"))
                .setPrice(rs.getLong("price"))
                .setQuantity(rs.getInt("quantity"))
                .setPowerConsumption(rs.getInt("power_consumption"))
                .setCapacity(rs.getDouble("capacity"));
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Oven e) throws SQLException {
        ps.setString(1, e.getName());
        ps.setDouble(2, e.getWeight());
        ps.setLong(3, e.getPrice());
        ps.setInt(4, e.getQuantity());
        ps.setInt(5, e.getPowerConsumption());
        ps.setDouble(6, e.getCapacity());
    }
}
