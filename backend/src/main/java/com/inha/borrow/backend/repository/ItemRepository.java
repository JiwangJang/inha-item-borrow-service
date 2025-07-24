package com.inha.borrow.backend.repository;
import com.inha.borrow.backend.domain.Item;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository {
    private final DataSource dataSource;

    public ItemRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Item save(Item item) {
        String sql = "insert into item(name,location,password,delete_reason,price,state) values(?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getLocation());
            pstmt.setString(3, item.getPassword());
            pstmt.setString(4, item.getDeleteReason());
            pstmt.setInt(5, item.getPrice());
            pstmt.setString(6, item.getState());

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                item.setId(rs.getInt(1));
            } else {
                throw new SQLException("ID 조회 실패");
            }
            return item;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public List<Item> findAll() {
        String sql = "select * from item";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            List<Item> items = new ArrayList<>();
            while(rs.next()){
                Item itemss = new Item();
                itemss.setId(rs.getInt("id"));
                itemss.setName(rs.getString("name"));
                itemss.setLocation(rs.getString("location"));
                itemss.setPassword(rs.getString("password"));
                itemss.setDeleteReason(rs.getString("delete_reason"));
                itemss.setPrice(rs.getInt("price"));
                itemss.setState(rs.getString("state"));
                items.add(itemss);
            }
            return items;
        }
            catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally{
                close(conn, pstmt, rs);
            }
        }
    public Optional<Item> findById(int id) {
        String sql = "select * from item where id= ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1,id);
            rs = pstmt.executeQuery();
            if(rs.next()){
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setLocation(rs.getString("location"));
                item.setPassword(rs.getString("password"));
                item.setDeleteReason(rs.getString("delete_reason"));
                item.setPrice(rs.getInt("price"));
                item.setState(rs.getString("state"));
                return Optional.of(item);
            }else return Optional.empty();

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally{
            close(conn, pstmt, rs);
        }
    }
    public boolean deleteItem(int id, String deleteReason){
        String sql = "update item set state='delete', delete_reason = ? where id =?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, deleteReason);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            close(conn, pstmt, rs);
        }


    }
    public boolean updateItem(Item item, int id){
        String sql = "update item set name = ?, location = ?, password = ?, delete_reason = ?, price = ?, state = ? where id =?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getLocation());
            pstmt.setString(3,item.getPassword());
            pstmt.setString(4,item.getDeleteReason());
            pstmt.setInt(5,item.getPrice());
            pstmt.setString(6,item.getState());
            pstmt.setInt(7,id);
            pstmt.executeUpdate();
            return true;

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            close(conn, pstmt, rs);
        }
    }
    public void deleteAll() {
        String sql = "DELETE FROM item";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs){
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }



}

