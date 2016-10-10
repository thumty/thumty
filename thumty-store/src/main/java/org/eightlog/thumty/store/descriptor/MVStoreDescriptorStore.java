package org.eightlog.thumty.store.descriptor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.eightlog.thumty.common.mvstore.AsyncMVMap;
import org.eightlog.thumty.common.mvstore.AsyncMVStore;
import org.eightlog.thumty.store.NotFoundException;
import org.h2.mvstore.MVStore;

import java.io.Serializable;
import java.util.Objects;

/**
 * MVStore back-ended descriptor store.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class MVStoreDescriptorStore implements DescriptorStore {

    private final AsyncMVMap<String, Entry> table;

    public static Future<DescriptorStore> create(AsyncMVStore mvStore, String table) {
        return mvStore.<String, Entry>getMap(table).map(MVStoreDescriptorStore::new);
    }

    public MVStoreDescriptorStore(AsyncMVMap<String, Entry> table) {
        this.table = table;
    }

    public MVStoreDescriptorStore(Vertx vertx, MVStore mvStore, String table) {
        Objects.requireNonNull(vertx, "vertx");
        Objects.requireNonNull(mvStore, "mvStore");
        Objects.requireNonNull(table, "table");

        this.table = new AsyncMVMap<>(vertx, mvStore, mvStore.openMap(table));
    }

    @Override
    public void read(String id, Handler<AsyncResult<Descriptor>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        table.get(id).compose(entry -> {
            if (entry != null) {
                return Future.succeededFuture(entry.getDescriptor());
            } else {
                return Future.failedFuture(new NotFoundException("Descriptor with id = " + id + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    @Override
    public void create(String id, Descriptor descriptor, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(handler, "handler");

        table.get(id).compose(entry ->{
            if (entry != null) {
                long numberOfReferences = entry.getNumberOfReferences() + 1;
                return table.put(id, new Entry(descriptor, numberOfReferences)).map(numberOfReferences);
            } else {
                long numberOfReferences = 1;
                return table.put(id, new Entry(descriptor, numberOfReferences)).map(numberOfReferences);
            }
        }).setHandler(handler);
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        table.get(id).compose(entry -> {
           if (entry != null) {
               if (entry.getNumberOfReferences() == 1) {
                   return table.remove(id).map(0L);
               } else {
                   return table.put(id, new Entry(entry.getDescriptor(), entry.getNumberOfReferences() - 1))
                           .map(entry.getNumberOfReferences() - 1);
               }
           } else {
               return Future.succeededFuture(-1L);
           }
        }).setHandler(handler);
    }

    @Override
    public void exists(String id, Handler<AsyncResult<Boolean>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        table.get(id).compose(entry -> Future.succeededFuture(entry != null)).setHandler(handler);
    }

    @Override
    public void duplicate(String id, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        table.get(id).compose(entry ->{
            long numberOfReferences;
            if (entry != null) {
                return table.put(id, new Entry(entry.getDescriptor(), numberOfReferences = entry.getNumberOfReferences() + 1))
                        .map(numberOfReferences);
            } else {
                return Future.failedFuture(new NotFoundException("Descriptor with id = " + id + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    private static class Entry implements Serializable {
        private final Descriptor descriptor;
        private final long numberOfReferences;

        public Entry(Descriptor descriptor, long numberOfReferences) {
            this.descriptor = descriptor;
            this.numberOfReferences = numberOfReferences;
        }

        public Descriptor getDescriptor() {
            return descriptor;
        }

        public long getNumberOfReferences() {
            return numberOfReferences;
        }
    }
}
