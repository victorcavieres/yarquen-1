package org.yarquen.topic;

import java.util.ArrayList;
import java.util.List;

public class Category extends Topic{
	private List<Topic> narrower;

	public List<Topic> getNarrower() {
		if(narrower==null)
			narrower=new ArrayList<Topic>();
		return narrower;
	}

	public void setNarrower(List<Topic> narrower) {
		this.narrower = narrower;
	}

	@Override
	public String toString() {
		return super.toString()+"Category [narrower=" + narrower + "]";
	}
}
