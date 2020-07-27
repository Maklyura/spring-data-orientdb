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
public interface EdgeInterface extends CIInterface {
  String getId();
  void setId(String id);
  String getIn();
  String getOut();
  void setIn(String in);
  void setOut(String out);
}
