package org.yarquen.crawler.scorer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.CustomFields;

import bixo.datum.GroupedUrlDatum;
import bixo.operations.BaseScoreGenerator;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
public class CompositeLinkScorer extends BaseScoreGenerator {
	private static Logger LOGGER = LoggerFactory
			.getLogger(CompositeLinkScorer.class);
	private static final long serialVersionUID = 6409954734659276529L;
	private List<BaseScoreGenerator> linkScoreGenerators;

	@Override
	public double generateScore(String domain, String pld, GroupedUrlDatum url) {
		// Since we limit the number of urls to be fetched per loop,
		// check the flag in payload and set skip score if skip flag is set.
		boolean skipped = (Boolean) url
				.getPayloadValue(CustomFields.SKIP_BY_LIMIT_FN);

		if (skipped) {
			LOGGER.debug(
					"aggregated score for grouped link domain={} pld={} url={} is SKIP_SCORE",
					new Object[] { domain, pld, url.getUrl() });
			return SKIP_SCORE;
		} else {
			double score = ((Float) url
					.getPayloadValue(CustomFields.LINKS_SCORE_FN))
					.doubleValue();
			LOGGER.debug(
					"aggregated score for grouped link domain={} pld={} url={} is {}",
					new Object[] { domain, pld, url.getUrl(), score });
			return score;
		}
	}

	@Override
	public double generateScore(String domain, String pld, String url) {
		double score = 0;
		for (BaseScoreGenerator lsg : linkScoreGenerators) {
			final double s = lsg.generateScore(domain, pld, url);
			if (s > 0) {
				score += s;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"aggregated score for link domain={} pld={} url={} is {}",
					new Object[] { domain, pld, url, score });
		}
		return score;
	}

	@Override
	public boolean isGoodDomain(String domain, String pld) {
		for (BaseScoreGenerator scorer : linkScoreGenerators) {
			if (scorer.isGoodDomain(domain, pld)) {
				LOGGER.debug("aggregated IS good domain, domain={} pld={}",
						domain, pld);
				return true;
			}
		}
		LOGGER.debug("aggregated ISN'T good domain, domain={} pld={}", domain,
				pld);
		return false;
	}

	public void setLinkScoreGenerators(
			List<BaseScoreGenerator> linkScoreGenerators) {
		this.linkScoreGenerators = linkScoreGenerators;
	}
}
