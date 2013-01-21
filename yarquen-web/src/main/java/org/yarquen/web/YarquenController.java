package org.yarquen.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 30/10/2012
 * @version $Id$
 * 
 */
@Controller
public class YarquenController
{

	@RequestMapping("/")
	public String home()
	{
		return "home";
	}

	@RequestMapping("/jobs")
	public ModelMap vetsHandler()
	{
		final List<CrawlerJob> jobs = new ArrayList<CrawlerJob>();

		for (int i = 0; i < 5; i++)
		{
			final CrawlerJob job = new CrawlerJob();
			job.setName("CRW-" + i);
			jobs.add(job);
		}
		return new ModelMap("jobs", jobs);
	}
	
	
}
