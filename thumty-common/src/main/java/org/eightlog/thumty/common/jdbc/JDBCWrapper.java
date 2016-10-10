package org.eightlog.thumty.common.jdbc;

import io.vertx.core.Future;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.TransactionIsolation;
import io.vertx.ext.sql.UpdateResult;

import java.util.function.Function;

/**
 * JDBCWrapper simplifies calls to JDBCClient, wraps methods with {@link Future}.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCWrapper {

    private JDBCClient client;

    /**
     * Construct new JDBCWrapper
     *
     * @param client the jdbc client
     */
    public JDBCWrapper(JDBCClient client) {
        this.client = client;
    }

    /**
     * Construct new JDBCWrapper
     *
     * @param client the jdbc client
     * @return a jdbc wrapper instance
     */
    public static JDBCWrapper create(JDBCClient client) {
        return new JDBCWrapper(client);
    }

    /**
     * Calls SQL update query
     *
     * @param query  the sql query
     * @param params the query params
     * @return an update query result future
     */
    public Future<UpdateResult> update(String query, Object... params) {
        return begin(tx -> tx.update(query, params).compose(tx::commit));
    }

    /**
     * Calls SQL query
     *
     * @param query  the sql query
     * @param params the query params
     * @return a query result future
     */
    public Future<ResultSet> query(String query, Object... params) {
        return begin(tx -> tx.query(query, params));
    }

    /**
     * Executes SQL query
     *
     * @param query the query expression
     * @return a execute result future
     */
    public Future<Void> execute(String query) {
        return begin(tx -> tx.execute(query).compose(tx::commit));
    }

    /**
     * Starts new transaction
     *
     * @param transactional the transaction body
     * @param <T>           the transaction return type
     * @return a transaction result future
     */
    public <T> Future<T> begin(Function<JDBCTransaction, Future<T>> transactional) {
        Future<T> future = Future.future();

        connection().compose(c -> c.setAutoCommit(false, r0 -> {
            if (r0.succeeded()) {
                try {
                    transactional.apply(new JDBCTransaction(c)).setHandler(r1 -> {
                        try {
                            if (r1.succeeded()) {
                                future.complete(r1.result());
                            } else {
                                future.fail(r1.cause());
                            }
                        } finally {
                            c.close();
                        }
                    });
                } catch (Throwable t) {
                    c.close();
                    future.fail(t);
                }
            } else {
                c.close();
                future.fail(r0.cause());
            }
        }), future);

        return future;
    }

    /**
     * Starts new transaction with given isolation level
     *
     * @param isolation     the transaction isolation
     * @param transactional the transaction body
     * @param <T>           the transaction return type
     * @return a transaction result future
     */
    public <T> Future<T> begin(TransactionIsolation isolation, Function<JDBCTransaction, Future<T>> transactional) {
        Future<T> future = Future.future();

        connection().compose(c -> c.setAutoCommit(false, r0 -> {
            if (r0.succeeded()) {
                c.setTransactionIsolation(isolation, r1 -> {
                    if (r1.succeeded()) {
                        try {
                            transactional.apply(new JDBCTransaction(c)).setHandler(r2 -> {
                                try {
                                    if (r2.succeeded()) {
                                        future.complete(r2.result());
                                    } else {
                                        future.fail(r2.cause());
                                    }
                                } finally {
                                    c.close();
                                }
                            });
                        } catch (Throwable t) {
                            c.close();
                            future.fail(t);
                        }
                    } else {
                        c.close();
                        future.fail(r1.cause());
                    }
                });
            } else {
                c.close();
                future.fail(r0.cause());
            }
        }), future);

        return future;
    }

    private Future<SQLConnection> connection() {
        Future<SQLConnection> future = Future.future();
        client.getConnection(future.completer());
        return future;
    }
}
