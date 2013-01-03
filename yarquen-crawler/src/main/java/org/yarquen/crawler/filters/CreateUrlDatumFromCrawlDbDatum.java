package org.yarquen.crawler.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.CustomFields;
import org.yarquen.crawler.datum.CrawlDbDatum;

import bixo.datum.UrlDatum;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.operation.filter.Limit;
import cascading.operation.filter.Limit.Context;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class CreateUrlDatumFromCrawlDbDatum extends
		BaseOperation<Limit.Context> implements Function<Limit.Context>
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CreateUrlDatumFromCrawlDbDatum.class);
	private long limit = 0;

	public CreateUrlDatumFromCrawlDbDatum(long limit)
	{
		super(UrlDatum.FIELDS);

		this.limit = limit;
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<Limit.Context> operationCall)
	{
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
	}

	@Override
	public void operate(FlowProcess flowProcess,
			FunctionCall<Limit.Context> funcCall)
	{
		final CrawlDbDatum datum = new CrawlDbDatum(funcCall.getArguments());

		final UrlDatum urlDatum = new UrlDatum(datum.getUrl());
		urlDatum.setPayloadValue(CustomFields.PAGE_SCORE_FN,
				datum.getPageScore());
		urlDatum.setPayloadValue(CustomFields.LINKS_SCORE_FN,
				datum.getLinksScore());
		urlDatum.setPayloadValue(CustomFields.STATUS_FN, datum.getLastStatus()
				.toString());
		urlDatum.setPayloadValue(CustomFields.SKIP_BY_LIMIT_FN, funcCall
				.getContext().increment());

		funcCall.getOutputCollector().add(urlDatum.getTuple());
	}

	@Override
	public void prepare(FlowProcess flowProcess,
			OperationCall<Limit.Context> operationCall)
	{
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(flowProcess, operationCall);

		Context context = new Context();

		operationCall.setContext(context);

		HadoopFlowProcess process = (HadoopFlowProcess) flowProcess;

		int numTasks = 0;

		if (process.isMapper())
		{
			numTasks = process.getCurrentNumMappers();
		}
		else
		{
			numTasks = process.getCurrentNumReducers();
		}

		int taskNum = process.getCurrentTaskNum();

		context.limit = (long) Math.floor((double) limit / (double) numTasks);

		long remainingLimit = limit % numTasks;

		// evenly divide limits across tasks
		context.limit += taskNum < remainingLimit ? 1 : 0;
	}
}