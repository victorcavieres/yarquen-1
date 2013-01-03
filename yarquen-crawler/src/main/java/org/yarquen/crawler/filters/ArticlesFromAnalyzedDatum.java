package org.yarquen.crawler.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.AnalyzedDatum;
import org.yarquen.crawler.datum.ArticleDatum;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.TupleEntryCollector;

import com.bixolabs.cascading.NullContext;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 *
 */
@SuppressWarnings("serial")
public class ArticlesFromAnalyzedDatum extends BaseOperation<NullContext>
		implements Function<NullContext>
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticlesFromAnalyzedDatum.class);

	public ArticlesFromAnalyzedDatum()
	{
		super(ArticleDatum.FIELDS);
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall)
	{
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
	}

	@Override
	public void prepare(FlowProcess process, OperationCall<NullContext> opCall)
	{
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(process, opCall);
	}

	@Override
	public void operate(FlowProcess process, FunctionCall<NullContext> funcCall)
	{
		final AnalyzedDatum datum = new AnalyzedDatum(funcCall.getArguments()
				.getTuple());
		LOGGER.debug("extracting {} COs from analyzed datum {}",
				datum.getArticles().length, datum.getUrl());
		final TupleEntryCollector collector = funcCall.getOutputCollector();

		for (ArticleDatum co : datum.getArticles())
		{
			LOGGER.debug("adding article {} to collector", co.getUrl());
			collector.add(co.getTuple());
		}
	}
}
