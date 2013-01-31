package org.yarquen.web.enricher;

import java.beans.PropertyEditorSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.account.Skill;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;

/**
 * PropertyEditorSupport for skills
 * 
 * @author Jorge Riquelme Santana
 * @date 30/01/2013
 * @version $Id$
 * 
 */
public final class SkillPropertyEditorSupport extends PropertyEditorSupport {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SkillPropertyEditorSupport.class);

	private CategoryService categoryService;

	public SkillPropertyEditorSupport(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@Override
	public void setAsText(String skillAsText) throws IllegalArgumentException {
		LOGGER.trace("converting {} to a Skill object", skillAsText);
		try {
			final int levelSeparationPosition = skillAsText.lastIndexOf('.');

			// level
			int level = 0;
			// branch
			String branchCode = null;

			if (levelSeparationPosition != -1) {
				final String levelSz = skillAsText
						.substring(levelSeparationPosition + 1);
				// FIXME: nooooooo
				if (levelSz.equals("1") || levelSz.equals("2")
						|| levelSz.equals("3")) {
					level = Integer.valueOf(levelSz);
					branchCode = skillAsText.substring(0,
							levelSeparationPosition);
				} else {
					branchCode = skillAsText;
				}
			} else {
				branchCode = skillAsText;
			}

			// category branch
			final CategoryBranch branch = CategoryBranch
					.incompleteFromCode(branchCode);
			categoryService.completeCategoryBranchNodeNames(branch);

			// skill
			LOGGER.trace("skill obj constructed, level={} branch={}", level,
					branchCode);
			final Skill skill = new Skill();
			skill.setLevel(level);
			skill.setCategoryBranch(branch);
			setValue(skill);
		} catch (RuntimeException ex) {
			LOGGER.error("I couldn't convert '" + skillAsText
					+ "' to an Skill object :(", ex);
		}
	}

	@Override
	public String getAsText() {
		final Skill skill = (Skill) getValue();
		return skill.getAsText();
	}
}