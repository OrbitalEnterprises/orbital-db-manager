package enterprises.orbital.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import enterprises.orbital.base.PersistentProperty;
import enterprises.orbital.base.PersistentPropertyProvider;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;

/**
 * Database backed implementation of PersistentPropertyProvider. You my choose to use this class by registering this provider with PersistentProperty at
 * startup.
 */
public class DBPropertyProvider implements PersistentPropertyProvider {
  private static final Logger log = Logger.getLogger(DBPropertyProvider.class.getName());
  private ConnectionFactory   factory;

  /**
   * Instantiate database backed property provider.
   * 
   * @param persistenceUnit
   *          the persistence unit describing the connection where persisted properties will be stored.
   */
  public DBPropertyProvider(String persistenceUnit) {
    factory = ConnectionFactory.getFactory(persistenceUnit);
  }

  @Override
  public List<PersistentProperty> retrieveAll() {
    List<DBProperty> raw = dbRetrieveAll();
    List<PersistentProperty> results = new ArrayList<PersistentProperty>();
    for (DBProperty next : raw) {
      results.add(next.toProperty());
    }
    return results;
  }

  @Override
  public PersistentProperty get(String key) {
    DBProperty raw = dbGet(key);
    return raw == null ? null : raw.toProperty();
  }

  @Override
  public String set(String key, String value) {
    return dbSet(key, value);
  }

  @Override
  public String remove(String key) {
    return dbRemove(key);
  }

  private List<DBProperty> dbRetrieveAll() {
    try {
      return factory.runTransaction(new RunInTransaction<List<DBProperty>>() {
        @Override
        public List<DBProperty> run() throws Exception {
          TypedQuery<DBProperty> getter = factory.getEntityManager().createNamedQuery("DBProperty.all", DBProperty.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return Collections.emptyList();
  }

  private DBProperty dbGet(final String key) {
    try {
      return factory.runTransaction(new RunInTransaction<DBProperty>() {
        @Override
        public DBProperty run() throws Exception {
          TypedQuery<DBProperty> getter = factory.getEntityManager().createNamedQuery("DBProperty.byPropName", DBProperty.class);
          getter.setParameter("propname", key);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            return null;
          }
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  private String dbSet(final String key, final String value) {
    try {
      return factory.runTransaction(new RunInTransaction<String>() {
        @Override
        public String run() throws Exception {
          DBProperty prev = dbGet(key);
          String result = prev == null ? null : prev.getPropertyValue();
          if (prev == null)
            prev = new DBProperty(key, value);
          else
            prev.propertyValue = value;
          // Not using the value after this call, so we don't assign to prev
          factory.getEntityManager().merge(prev);
          return result;
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  private String dbRemove(final String key) {
    try {
      return factory.runTransaction(new RunInTransaction<String>() {
        @Override
        public String run() throws Exception {
          DBProperty prev = dbGet(key);
          String result = prev == null ? null : prev.getPropertyValue();
          if (prev != null) factory.getEntityManager().remove(prev);
          return result;
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

}
