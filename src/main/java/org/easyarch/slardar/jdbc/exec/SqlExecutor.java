package org.easyarch.slardar.jdbc.exec;/**
 * Description : 
 * Created by YangZH on 16-11-2
 *  上午11:11
 */

import org.easyarch.slardar.jdbc.cfg.ConnConfig;
import org.easyarch.slardar.jdbc.cfg.PoolConfig;
import org.easyarch.slardar.jdbc.handler.IntegerResultSetHandler;
import org.easyarch.slardar.jdbc.handler.ResultSetHandler;
import org.easyarch.slardar.jdbc.pool.DBCPoolFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Description :
 * Created by code4j on 16-11-2
 * 上午11:11
 */

public class SqlExecutor extends AbstractExecutor{

    private Connection conn;

    public SqlExecutor(Connection conn,boolean supportMeta){
        super(supportMeta);
        this.conn = conn;
    }

    /**
     *
     * @param sql
     * @param rshandler
     * @param params
     * @param <T>
     * @return
     */
    public <T> T query(String sql, ResultSetHandler<T> rshandler, Object[] params) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = prepareStatement(conn, sql);
            fillStatement(ps,params);
            rs = ps.executeQuery();
            T result = rshandler.handle(rs);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 提供connection的修改操作
     * @param sql
     * @param params
     * @return
     */
    public int alter(String sql,Object[] params){
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = prepareStatement(conn, sql);
            fillStatement(ps,params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            ConnectionUtils.rollBack(conn);
            return -1;
        }
    }

    public void rollback(){
        ConnectionUtils.close(conn);
    }

    public void close(){
        ConnectionUtils.close(conn);
    }

    /**
     * 提供connection的批量修改操作
     * @param sql
     * @param params
     */
    public void alterBatch(String sql,Object[][]params){
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ConnectionUtils.beginTransaction(conn);
            ps = batchPrepareStatement(conn, sql);
            fillStatement(ps,params);
            ps.executeBatch();
            ConnectionUtils.commit(conn);
            return ;
        } catch (SQLException e) {
            e.printStackTrace();
            ConnectionUtils.rollBack(conn);
        }finally {
            ConnectionUtils.close(rs);
            ConnectionUtils.close(ps);
            ConnectionUtils.close(conn);
        }
    }


    public static void main(String[] args) throws SQLException {
        ConnConfig.config("root", "123456",
                "jdbc:mysql://localhost:3306/shopping?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false", "com.mysql.jdbc.Driver");
        DataSource dataSource = DBCPoolFactory.newCustomDBCPool(PoolConfig.config(200, 50, 5, 3 * 1000L));
        final SqlExecutor executor = new MySqlExecutor(dataSource.getConnection());
        System.out.println(executor);
        ResultSetHandler<Integer> handler = new IntegerResultSetHandler();
        Integer count = executor.query("select count(1) from user ", handler, null);
        System.out.println(count);
//        int result = executor.alter(connection,"insert into user values(?,?,?,?,?)",10,"laisbfdsfk","583110127","13652212569",30);
//        System.out.println("end "+result);
    }
}
