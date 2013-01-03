package org.yarquen.crawler;

import com.bixolabs.cascading.BaseDatum;

/**
 * Custom tuple fields
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
public class CustomFields
{
	public static final String PAGE_SCORE_FN = BaseDatum.fieldName(
			CustomFields.class, "pagescore");

	public static final String LINKS_SCORE_FN = BaseDatum.fieldName(
			CustomFields.class, "linksscore");

	public static final String STATUS_FN = BaseDatum.fieldName(
			CustomFields.class, "status");

	public static final String SKIP_BY_LIMIT_FN = BaseDatum.fieldName(
			CustomFields.class, "limited");
}
