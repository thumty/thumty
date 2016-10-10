package org.eightlog.thumty.common.jdbc;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class, that holds SQL connection and wraps calls with {@link Future}
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCTransaction {

    private final SQLConnection connection;

    /**
     * Construct new transaction
     *
     * @param connection the sql connection
     */
    public JDBCTransaction(SQLConnection connection) {
        this.connection = connection;
    }

    /**
     * Executes SQL
     *
     * @param query the sql
     * @return a result future
     */
    public Future<Void> execute(String query) {
        Future<Void> future = Future.future();
        try {
            connection.execute(query, future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Execute update sql
     *
     * @param query  the sql
     * @param params the sql params
     * @return an update result future
     */
    public Future<UpdateResult> update(String query, Object... params) {
        Future<UpdateResult> future = Future.future();
        try {
            connection.updateWithParams(query, params(params), future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Execute query sql
     *
     * @param query  the sql
     * @param params the sql params
     * @return an query result future
     */
    public Future<ResultSet> query(String query, Object... params) {
        Future<ResultSet> future = Future.future();
        try {
            connection.queryWithParams(query, params(params), future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Execute batch sql
     *
     * @param sql the collection of sql
     * @return an batch result future
     */
    public Future<List<Integer>> batch(List<String> sql) {
        Future<List<Integer>> future = Future.future();
        try {
            connection.batch(sql, future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Commits transaction, passing input
     *
     * @param t   the input value
     * @param <T> the type of input
     * @return a input future
     */
    public <T> Future<T> commit(T t) {
        return commit().map(t);
    }

    /**
     * Commits transaction
     *
     * @return a result future
     */
    public Future<Void> commit() {
        Future<Void> future = Future.future();
        try {
            connection.commit(future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Rollbacks transaction
     *
     * @return a result future
     */
    public Future<Void> rollback() {
        Future<Void> future = Future.future();
        try {
            connection.rollback(future.completer());
        } catch (Throwable t) {
            future.fail(t);
        }
        return future;
    }

    /**
     * Rollbacks transaction, passing input as result
     *
     * @param t   the input
     * @param <T> the input type
     * @return a input future
     */
    public <T> Future<T> rollback(T t) {
        return rollback().map(t);
    }

    private JsonArray params(Object... params) {
        return new JsonArray(Arrays.asList(params));
    }
}
