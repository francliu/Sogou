package com.sogou.cm.pa.pagecluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.web.selector.urllib.URLUtils;

public class ClusterPredictor {
	HashMap<String, ArrayList<ClusterInfo>> site_clusters;
	HashMap<String, ArrayList<ClusterInfo>> domain_clusters;
	
	PageSegmentation		htmlContentHandler		= new PageSegmentation();
	Parser					parser					= new Parser();
	String level;
	String site;
	long page_parse_time = 0;
	long predict_time = 0;
	
	
	public ClusterPredictor(String site_cluster) throws IOException {
		site_clusters = new HashMap<String, ArrayList<ClusterInfo>>();
		domain_clusters = new HashMap<String, ArrayList<ClusterInfo>>();
		parser.setContentHandler(htmlContentHandler);
		BufferedReader reader = new BufferedReader(new FileReader(new File(site_cluster)));
		
		String line;

		while ((line = reader.readLine()) != null) {
			ClusterInfo ci = new ClusterInfo();
			ci.fromSimpleString(line);
		//	System.out.println(ci.level);
			if (ci.level.equalsIgnoreCase("site")) {
				if (site_clusters.containsKey(ci.site)) {
					site_clusters.get(ci.site).add(ci);
				} else {
					ArrayList<ClusterInfo> temp = new ArrayList<ClusterInfo>();
					temp.add(ci);
					site_clusters.put(ci.site, temp);
				}
			} else {
				if (domain_clusters.containsKey(ci.site)) {
					domain_clusters.get(ci.site).add(ci);
				} else {
					ArrayList<ClusterInfo> temp = new ArrayList<ClusterInfo>();
					temp.add(ci);
					domain_clusters.put(ci.site, temp);
				}
			}
		}

		reader.close();
		/*
		for (ArrayList<ClusterInfo> cis: site_clusters.values()) {
			for (ClusterInfo ci: cis) {
				System.out.println(ci.key + "\t" + ci.level + "\t" + ci.site + "\t" + ci.limit);
			}
		}
		*/
	}
	
	public ClusterPredictor(ArrayList<ClusterInfo> cis) throws IOException {
		site_clusters = new HashMap<String, ArrayList<ClusterInfo>>();
		domain_clusters = new HashMap<String, ArrayList<ClusterInfo>>();
		parser.setContentHandler(htmlContentHandler);

		for (ClusterInfo ci: cis) {
			if (ci.level.equalsIgnoreCase("site")) {
				if (site_clusters.containsKey(ci.site)) {
					site_clusters.get(ci.site).add(ci);
				} else {
					ArrayList<ClusterInfo> temp = new ArrayList<ClusterInfo>();
					temp.add(ci);
					site_clusters.put(ci.site, temp);
				}
			} else {
				if (domain_clusters.containsKey(ci.site)) {
					domain_clusters.get(ci.site).add(ci);
				} else {
					ArrayList<ClusterInfo> temp = new ArrayList<ClusterInfo>();
					temp.add(ci);
					domain_clusters.put(ci.site, temp);
				}
			}
		}
	}
	
	public String predict(String url, String html) throws IOException, SAXException {
		this.site = null;
		level = null;
		long b = System.currentTimeMillis();
		htmlContentHandler.setHtmlCode(html);
		parser.parse(new InputSource(new StringReader(html)));
		page_parse_time += System.currentTimeMillis()-b;
		HtmlPage html_page = htmlContentHandler.html_page;		
		html_page.url = url;
		
		
	//	for(String xpath2: html_page.xpath2info.keySet()) {
	//		System.out.println(xpath2);
	//	}
		String site = URLUtils.getDomainWithoutPort(url);
		String domain = URLUtils.getMainDomain(url);
		if (site == null || domain == null) {
			return null;
		}
		ArrayList<ClusterInfo> temp = null;
		if (site_clusters.containsKey(site)) {
			temp = site_clusters.get(site);
			level = "site";
			this.site = site;
		} else if (domain_clusters.containsKey(domain)) {
			temp = domain_clusters.get(domain);
			level = "domain";
			this.site = domain;
		}
		if (temp == null) {
			return null;
		}
		double min_dist = 1.0;
		ClusterInfo min_ci = null;
		 b = System.currentTimeMillis();
		for (ClusterInfo ci: temp) {
			double dist = ci.getDist(html_page);
			if (dist < ci.limit+0.2 && dist < min_dist) {
				min_dist = dist;
				min_ci = ci;
			}
		}
		predict_time += System.currentTimeMillis()-b;
	//	System.out.println(min_dist);
		if (min_dist < 0.5) {
			return min_ci.key;
		} else {
			return null;
		}
		
	}
	
	public String predict(HtmlPage html_page) throws IOException, SAXException {

	//	for(String xpath2: html_page.xpath2info.keySet()) {
	//		System.out.println(xpath2);
	//	}
		String site = URLUtils.getDomainWithoutPort(html_page.url);
		String domain = URLUtils.getMainDomain(html_page.url);
		if (site == null || domain == null) {
			return null;
		}
		ArrayList<ClusterInfo> temp = null;
		if (site_clusters.containsKey(site)) {
			temp = site_clusters.get(site);
			level = "site";
			this.site = site;
		} else if (domain_clusters.containsKey(domain)) {
			temp = domain_clusters.get(domain);
			level = "domain";
			this.site = domain;
		}
		if (temp == null) {
			return null;
		}
		double min_dist = 1.0;
		ClusterInfo min_ci = null;
		for (ClusterInfo ci: temp) {
			double dist = ci.getDist(html_page);
			if (dist < ci.limit+0.2 && dist < min_dist) {
				min_dist = dist;
				min_ci = ci;
			}
		}
	//	System.out.println(min_dist);
		if (min_dist < 0.5) {
			return min_ci.key;
		} else {
			return null;
		}
		
	}
}
