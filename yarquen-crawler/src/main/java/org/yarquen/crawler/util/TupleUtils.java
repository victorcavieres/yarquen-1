package org.yarquen.crawler.util;

import java.lang.reflect.Array;

import cascading.tuple.Tuple;

/**
 * Tuple utils.
 * 
 * @author Jorge Riquelme Santana
 * @date 17/08/2012
 * @svn $LastChangedRevision$
 * @svn $LastChangedBy$
 * @svn $LastChangedDate$
 * 
 */
public class TupleUtils {
	@SuppressWarnings("unchecked")
	public static <T> T[] makeObjectArrayFromTuple(Class<T> clazz, Tuple tuple) {
		final T[] result = (T[]) Array.newInstance(clazz, tuple.size());
		for (int i = 0; i < tuple.size(); i++) {
			result[i] = (T) tuple.getObject(i);
		}
		return result;
	}

	public static <T> Tuple makeTupleFrom(T[] values) {
		final Tuple t = new Tuple();
		for (T v : values) {
			t.add(v);
		}
		return t;
	}

	private TupleUtils() {
	}
}
