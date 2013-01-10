package org.yarquen.article;

import org.springframework.data.repository.CrudRepository;

/**
 * Article repository
 * 
 * @author Jorge Riquelme Santana
 * @date 09/01/2013
 * @version $Id$
 * 
 */
public interface ArticleRepository extends CrudRepository<Article, Long>
{
	Article findByUrl(String url);
}
