package org.yarquen.web.lucene;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {@link IndexBuilder} itest
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@IfProfileValue(name = "test-groups", value = "itests")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/index-builder-context.xml" })
public class IndexBuilderTest
{
	@Resource
	private IndexBuilder indexBuilder;
	
	@Test
	public void text() throws IOException
	{
		indexBuilder.createIndex();
	}
}
