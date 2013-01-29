package org.yarquen.validation;

import java.util.Set;

/**
 * Exception thrown when the validation process of a JSR-303 anotated bean
 * fails.
 * 
 * @author Jorge Riquelme Santana
 * @date 01/09/2010
 * @version $Id$
 * 
 */
public final class BeanValidationException extends RuntimeException {
	private static final long serialVersionUID = 8711287514784916502L;

	private final Object bean;
	private Set<String> violationsMessages;

	public BeanValidationException(Object bean, Set<String> violationsMessages) {
		this.bean = bean;
		this.violationsMessages = violationsMessages;
	}

	/**
	 * Returns the anotated object that originates the exception on the
	 * validation process.
	 * 
	 * @return bean.
	 */
	public Object getBean() {
		return bean;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}

	@Override
	public String getMessage() {
		final StringBuilder buffer = new StringBuilder("[");
		for (String v : violationsMessages) {
			buffer.append(v);
			buffer.append(", ");
		}
		buffer.delete(buffer.length() - 2, buffer.length());
		buffer.append("]");
		return buffer.toString();
	}

	public Set<String> getViolationsMessages() {
		return violationsMessages;
	}
}