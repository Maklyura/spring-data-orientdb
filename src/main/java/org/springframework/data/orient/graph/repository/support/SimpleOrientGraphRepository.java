package org.springframework.data.orient.graph.repository.support;

import com.orientechnologies.orient.core.id.ORecordId;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.repository.OrientRepository;
import org.springframework.data.orient.commons.repository.OrientSource;
import org.springframework.data.orient.commons.repository.support.*;
import org.springframework.data.orient.commons.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.RuntimeException;

import org.springframework.data.orient.commons.repository.EdgeInterface;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.id.ORID;

/**
 * Default implementation of the {@link org.springframework.data.repository.PagingAndSortingRepository} interface for OrientDB.
 *
 * @param <T> the type of the entity to handle
 *
 * @author Dzmitry_Naskou
 */
@Repository
@Transactional(readOnly = true)
public class SimpleOrientGraphRepository<T extends EdgeInterface> extends SimpleOrientRepository<T> {
  protected final OrientOperations<T> operations;

  /**
   * The domain class.
   */
  protected final Class<T> domainClass;

  protected final String source;

  /**
   * The repository interface.
   */
  protected final Class<?> repositoryInterface;

  private final OrientStrategy<T> strategy;

  /**
   * Instantiates a new {@link SimpleOrientRepository} from the given {@link OrientOperations} and domain class.
   *
   * @param operations          the orinet operations
   * @param domainClass         the domain class
   * @param repositoryInterface the target repository interface
   */
  public SimpleOrientGraphRepository(OrientOperations operations,
                                     Class<T> domainClass,
                                     Class<?> repositoryInterface) {
  //  super(operations, domainClass, repositoryInterface,
        //  new SimpleOrientStrategy<T>(operations, domainClass));
    this(operations, domainClass, repositoryInterface,
         new SimpleOrientStrategy<T>(operations, domainClass));
  }

  /**
   * Instantiates a new simple orient repository.
   *
   * @param operations          the operations
   * @param domainClass         the domain class
   * @param cluster             the cluster
   * @param repositoryInterface the repository interface
   */
  public SimpleOrientGraphRepository(OrientOperations operations,
                                     Class<T> domainClass,
                                     String cluster,
                                     Class<?> repositoryInterface) {
  //  super(operations, domainClass, repositoryInterface,
         //new ClusteredOrientStrategy<T>(operations, cluster));
    this(operations, domainClass, repositoryInterface,
         new ClusteredOrientStrategy<T>(operations, cluster));
  }

  /**
   * Instantiates a new simple orient repository.
   *
   * @param operations          the operations
   * @param domainClass         the domain class
   * @param repositoryInterface the repository interface
   * @param strategy            the strategy
   */
  private SimpleOrientGraphRepository(OrientOperations operations,
                                      Class<T> domainClass,
                                      Class<?> repositoryInterface,
                                      OrientStrategy<T> strategy) {

    super(operations, domainClass, repositoryInterface, strategy);
    this.operations = operations;
    this.domainClass = domainClass;
    this.repositoryInterface = repositoryInterface;
    this.strategy = strategy;
    this.source = strategy.getSource();
  }


  /* (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#save(S)
   */
  @Override
  @Transactional(readOnly = false)
  public <S extends T> S save(S entity) {

    if (entity.getOut() == null  || entity.getIn() == null)
      throw new IllegalArgumentException
      ("In or out properties are null");


    try {
      new ORecordId(entity.getOut());
      new ORecordId(entity.getIn());
    } catch( Exception e) {
      throw new IllegalArgumentException
      ("In or out properties are invalid. " +
      "Correct type is java.util.String. " +
      "Correct form is \"#<Cluster id>:<Record id>\"");
    }

    try {
      ORID fromRid = new ORecordId(entity.getOut());
      ORID toRid = new ORecordId(entity.getIn());
      String edgeClass = new String(entity.getClass().getSimpleName());
      Object[] args = new Object[2];

      args[0] = fromRid;
      args[1] = toRid;
      S result = operations.<S>objectCommand
      ("CREATE EDGE " + edgeClass + " UPSERT FROM ? TO ?",
        args);

      return (S)result;
    } catch (Exception e) {
        throw new RuntimeException (e);
    }

  }

  @Override
  public <S extends T> Iterable<S> saveAll(Iterable<S> iterable) {
    List<S> result = new ArrayList<>();
    for (S s : iterable) {
      result.add(this.save(s));
    }
    return result;
  }


  /* (non-Javadoc)
   * @see org.springframework.data.orient.repository.OrientRepository#save(java.lang.Object, java.lang.String)
   */
  @Override
  @Transactional(readOnly = false)
  public <S extends T> S save(S entity, String cluster) {
    return this.save(entity);
  }

  /* (non-Javadoc)
   * @see org.springframework.data.orient.repository.OrientRepository#findAll()
   */

  @Override
  public void deleteById(String id) {
    this.delete(id);
  }

  /* (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#delete(java.io.Serializable)
   */
  @Transactional(readOnly = false)
  public void delete(String id) {
    //operations.delete(new ORecordId(id));
    operations.command ("DELETE EDGE WHERE @rid = ?", new ORecordId(id));
  }

  /* (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
   */
  @Transactional(readOnly = false)
  public void delete(T entity) {
    //operations.delete(entity);
    operations.command ("DELETE EDGE WHERE @rid = ?", new ORecordId(entity.getId()));
  }

  @Override
  public void deleteAll(Iterable<? extends T> iterable) {
    for (T t : iterable) {
      delete(t);
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Iterable)
   */
  @Transactional(readOnly = false)
  public void delete(Iterable<? extends T> entities) {
    for (T entity : entities) {
      delete(entity);
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#deleteAll()
   */
  @Transactional(readOnly = false)
  public void deleteAll() {
    for (T entity : findAll()) {
      delete(entity);
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.data.orient.repository.OrientRepository#deleteAll(java.lang.String)
   */
  @Override
  public void deleteAll(String cluster) {
    for (T entity : findAll(cluster)) {
      delete(entity);
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.data.orient.repository.OrientRepository#deleteAll(java.lang.Class)
   */
  @Override
  public void deleteAll(Class<? extends T> domainClass) {
    for (T entity : findAll(domainClass)) {
      delete(entity);
    }
  }


}
