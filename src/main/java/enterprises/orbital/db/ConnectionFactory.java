package enterprises.orbital.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Common JPA style connection factory for entity managers. This class supports multiple persistence units.
 */
public class ConnectionFactory {
  private static final Logger                   log                  = Logger.getLogger(ConnectionFactory.class.getName());
  // Maintain at most one factory for each persistence unit
  private static Map<String, ConnectionFactory> connectionFactoryMap = new HashMap<String, ConnectionFactory>();
  // Name of persistence unit
  private String                                persistenceUnit;
  // Factory for this unit
  private EntityManagerFactory                  emf;
  // Maintain one entity manager per thread per factory
  private ThreadLocal<EntityManager>            managerPool          = new ThreadLocal<EntityManager>() {
                                                                       @Override
                                                                       protected EntityManager initialValue() {
                                                                         return emf.createEntityManager();
                                                                       }
                                                                     };

  /**
   * Construct a new connection factory. Can only be done via static method.
   * 
   * @param pu
   *          persistence unit for this factory.
   */
  private ConnectionFactory(String pu) {
    persistenceUnit = pu;
    emf = Persistence.createEntityManagerFactory(persistenceUnit);
  }

  public static ConnectionFactory getFactory(String persistenceUnit) {
    synchronized (connectionFactoryMap) {
      ConnectionFactory result = connectionFactoryMap.get(persistenceUnit);
      if (result == null) {
        result = new ConnectionFactory(persistenceUnit);
        connectionFactoryMap.put(persistenceUnit, result);
      }
      return result;
    }
  }

  public EntityManager getEntityManager() {
    return managerPool.get();
  }

  public boolean begin(boolean join) {
    if (getEntityManager().getTransaction().isActive()) return true;
    getEntityManager().getTransaction().begin();
    return false;
  }

  public void commit(boolean join) {
    if (!join) {
      getEntityManager().getTransaction().commit();
    }
  }

  public void rollback(boolean join) {
    try {
      if (!join) {
        getEntityManager().getTransaction().rollback();
      }
    } catch (RuntimeException e) {
      log.warning("Cannot rollback: " + e);
    }
  }

  public void close(boolean join) {
    if (!join) {
      getEntityManager().close();
      managerPool.remove();
    }
  }

  public interface RunInTransaction<T> {
    public T run() throws Exception;
  }

  public interface RunInVoidTransaction {
    public void run() throws Exception;
  }

  public <T extends Object> T runTransaction(RunInTransaction<T> runner) throws IOException, ExecutionException {
    boolean joined = false;
    try {
      joined = begin(joined);
      T result = runner.run();
      commit(joined);
      return result;
    } catch (Exception e) {
      rollback(joined);
      if (e instanceof ExecutionException)
        throw (ExecutionException) e;
      else
        throw new IOException(e);
    } finally {
      close(joined);
    }
  }

  public void runTransaction(RunInVoidTransaction runner) throws IOException, ExecutionException {
    boolean joined = false;
    try {
      joined = begin(joined);
      runner.run();
      commit(joined);
    } catch (Exception e) {
      rollback(joined);
      if (e instanceof ExecutionException)
        throw (ExecutionException) e;
      else
        throw new IOException(e);
    } finally {
      close(joined);
    }
  }

}
