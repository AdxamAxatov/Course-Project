package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.entity.Laptop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcLaptopDao extends JdbcApplianceDao<Laptop> {

    public JdbcLaptopDao(Database db) {
        super(db);
    }

    @Override
    protected String table() { return "laptops"; }

    @Override
    protected String selectColumns() {
        return "id, name, weight, price, quantity, os, cpu, battery_capacity";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO laptops (name, weight, price, quantity, os, cpu, battery_capacity) "
             + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected Laptop map(ResultSet rs) throws SQLException {
        return new Laptop()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setWeight(rs.getDouble("weight"))
                .setPrice(rs.getLong("price"))
                .setQuantity(rs.getInt("quantity"))
                .setOs(rs.getString("os"))
                .setCpu(rs.getString("cpu"))
                .setBatteryCapacity(rs.getInt("battery_capacity"));
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Laptop e) throws SQLException {
        ps.setString(1, e.getName());
        ps.setDouble(2, e.getWeight());
        ps.setLong(3, e.getPrice());
        ps.setInt(4, e.getQuantity());
        ps.setString(5, e.getOs());
        ps.setString(6, e.getCpu());
        ps.setInt(7, e.getBatteryCapacity());
    }
}
