package org.yarquen.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Category bean validation tests
 * 
 * @author Jorge Riquelme Santana
 * @date 21/01/2013
 * @version $Id$
 * 
 */
public class SubCategoryBeanValidationTest {
	private Validator validator;

	@Before
	public void setup() {
		final ValidatorFactory factory = Validation
				.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testEmptyCode() {
		final SubCategory sc = new SubCategory("", "asdf");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(2, violations.size());
		for (ConstraintViolation<SubCategory> constraintViolation : violations) {
			Assert.assertEquals("code", constraintViolation.getPropertyPath()
					.toString());
		}
	}

	@Test
	public void testEmptyName() {
		final SubCategory sc = new SubCategory("1", "");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(1, violations.size());
		for (ConstraintViolation<SubCategory> constraintViolation : violations) {
			Assert.assertEquals("name", constraintViolation.getPropertyPath()
					.toString());
		}
	}

	@Test
	public void testInvalidCode() {
		final SubCategory sc = new SubCategory("s.2", "asdf");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(1, violations.size());
		for (ConstraintViolation<SubCategory> constraintViolation : violations) {
			Assert.assertEquals("code", constraintViolation.getPropertyPath()
					.toString());
		}
	}

	@Test
	public void testInvalidSubcategories() {
		final SubCategory sc = new SubCategory("2", "asdf");

		final List<SubCategory> subCategories = new ArrayList<SubCategory>(2);
		subCategories.add(new SubCategory("12.3", "asdf"));
		subCategories.add(new SubCategory("4,56", "asdf"));
		sc.setSubCategories(subCategories);

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(2, violations.size());
	}

	@Test
	public void testNullCode() {
		final SubCategory sc = new SubCategory(null, "asdf");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(1, violations.size());
		for (ConstraintViolation<SubCategory> constraintViolation : violations) {
			Assert.assertEquals("code", constraintViolation.getPropertyPath()
					.toString());
		}
	}

	@Test
	public void testNullName() {
		final SubCategory sc = new SubCategory("1", null);

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(1, violations.size());
		for (ConstraintViolation<SubCategory> constraintViolation : violations) {
			Assert.assertEquals("name", constraintViolation.getPropertyPath()
					.toString());
		}
	}

	@Test
	public void testValidCode() {
		final SubCategory sc = new SubCategory("douh2", "asdf");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(0, violations.size());
	}

	@Test
	public void testValidNumericCode() {
		final SubCategory sc = new SubCategory("2", "asdf");

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(0, violations.size());
	}

	@Test
	public void testValidSubcategories() {
		final SubCategory sc = new SubCategory("2", "asdf");

		final List<SubCategory> subCategories = new ArrayList<SubCategory>(2);
		subCategories.add(new SubCategory("123", "asdf"));
		subCategories.add(new SubCategory("456", "asdf"));
		sc.setSubCategories(subCategories);

		final Set<ConstraintViolation<SubCategory>> violations = validator
				.validate(sc, Default.class);
		Assert.assertEquals(0, violations.size());
	}
}
