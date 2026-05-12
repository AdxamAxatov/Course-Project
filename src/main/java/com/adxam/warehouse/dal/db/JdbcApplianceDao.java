package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.criteria.SearchCriteria;
import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.MutableApplianceDao;
import com.adxam.warehouse.entity.Appliance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class JdbcApplianceDao<A extends Appliance<A>> implements MutableApplianceDao<A> {
    protected final Database db;

    protected JdbcApplianceDao(Database db) {
        this.db = db;
    }

    protected abstract String table();
    protected abstract String selectColumns();
    protected abstract String insertSql();
    protected abstract A map(ResultSet rs) throws SQLException;
    protected abstract void bindInsert(PreparedStatement ps, A entity) throws SQLException;

    @Override
    public List<A> find(SearchCriteria<A> criteria) throws DaoException {
        String sql = "SELECT " + selectColumns() + " FROM " + table();
        try {
            return db.query(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    List<A> list = new ArrayList<>();
                    while (rs.next()) {
                        A entity = map(rs);
                        if (criteria.test(entity)) list.add(entity);
                    }
                    return list;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException | SQLException e) {
            throw new DaoException(unwrap(e));
        }
    }

    @Override
    public A insert(A entity) throws DaoException {
        try {
            return db.inTransaction(c -> {
                try (PreparedStatement ps = c.prepareStatement(insertSql(), Statement.RETURN_GENERATED_KEYS)) {
                    bindInsert(ps, entity);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getLong(1));
                    }
                }
                return entity;
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean deleteById(long id) throws DaoException {
        try {
            return db.inTransaction(c -> {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + table() + " WHERE id = ?")) {
                    ps.setLong(1, id);
                    return ps.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Throwable unwrap(Throwable t) {
        return (t instanceof RuntimeException re && re.getCause() != null) ? re.getCause() : t;
    }
}
