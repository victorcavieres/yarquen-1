package org.yarquen.keyword;

import org.springframework.data.repository.CrudRepository;

/**
 * Keyword repo
 * 
 * @author Jorge Riquelme Santana
 * @date 20/01/2013
 * @version $Id$
 * 
 */
public interface KeywordRepository extends CrudRepository<Keyword, String> {

	Keyword findByName(String kwName);
}
