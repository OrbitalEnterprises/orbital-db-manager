package enterprises.orbital.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import enterprises.orbital.base.PersistentProperty;

/**
 * Generic persistent properties class. If you choose to use this class (which can be done by registering the associated provider), then a table called
 * "orbital_properties" will be used to persistently store properties in the database.
 */
@Entity
@Table(name = "orbital_properties")
@NamedQueries({
    @NamedQuery(name = "DBProperty.all", query = "SELECT c FROM DBProperty c"),
    @NamedQuery(name = "DBProperty.byPropName", query = "SELECT c FROM DBProperty c where c.propertyName = :propname"),
})
public class DBProperty {
  @Id
  String propertyName;
  String propertyValue;

  public DBProperty() {}

  public DBProperty(String propertyName, String propertyValue) {
    super();
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getPropertyValue() {
    return propertyValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
    result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DBProperty other = (DBProperty) obj;
    if (propertyName == null) {
      if (other.propertyName != null) return false;
    } else if (!propertyName.equals(other.propertyName)) return false;
    if (propertyValue == null) {
      if (other.propertyValue != null) return false;
    } else if (!propertyValue.equals(other.propertyValue)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "DBProperty [propertyName=" + propertyName + ", propertyValue=" + propertyValue + "]";
  }

  public PersistentProperty toProperty() {
    return new PersistentProperty(propertyName, propertyValue);
  }

}
