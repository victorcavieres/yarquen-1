package org.yarquen.validation;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Bean validation utilities
 * 
 * @author Jorge Riquelme Santana
 * @date 07/12/2011
 * @version $Id$
 * 
 */
public class ValidationUtils {
	public static <T> Set<String> getConstraintsMessages(
			Set<ConstraintViolation<T>> violations) {
		final Set<String> msgs = new HashSet<String>(violations.size());
		for (ConstraintViolation<T> v : violations) {
			msgs.add(v.getPropertyPath() + " is " + v.getInvalidValue()
					+ " and " + v.getMessage());
		}
		return msgs;
	}

	public static <T> String getConstraintsMessagesAsError(
			Set<ConstraintViolation<T>> violations) {
		final Set<String> messages = getConstraintsMessages(violations);
		final StringBuilder buffer = new StringBuilder("[");
		for (String msg : messages) {
			buffer.append(msg);
			buffer.append(", ");
		}
		buffer.delete(buffer.length() - 2, buffer.length());
		buffer.append("]");
		return buffer.toString();
	}
}