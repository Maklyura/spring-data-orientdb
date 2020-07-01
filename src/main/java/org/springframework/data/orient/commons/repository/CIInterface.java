package org.springframework.data.orient.commons.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Interface that entities wich represent the Edge must implement.
 *
 * @author Mariia Klimenko
 */
//@NoRepositoryBean
public interface CIInterface {
  String getId();

  void setId(String id);

}
