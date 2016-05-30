package com.sogou.cm.pa.multipage.maincontent;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRClusterWebkitMaincontentGetter  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashSet<String> domains = new HashSet<String>();
		protected void setup(Context context) throws IOException,
		InterruptedException {

		}
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String s = value.toString();
			int pos = s.indexOf('\t');
			String clusterid = s.substring(0,pos);
			String pageinfo = s.substring(pos+1);
			context.write(new Text(clusterid), new Text(pageinfo));
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public static boolean isRepeat(int n, int uniq) {
			if (n <= 2) {
				return false;
			} else if (n <= 4) {
				if (uniq <= 1) {
					return true;
				} else {
					return false;
				}
			} else if (n <= 7) {
				if (uniq <= 2) {
					return true;
				} else {
					return false;
				}
			} else {
				if (uniq*3<=n) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String clusterid = key.toString();
			ArrayList<PageStat>  pses = new ArrayList<PageStat>();
			StringBuffer sb = new StringBuffer();
			for (Text val: values) {
				PageStat ps = new PageStat();
				ps.fromString(val.toString());
				sb.append(ps.url + "\t");
				pses.add(ps);
			}
			if (pses.size() < 3) {
				return;
			}
	//		for (String key: cluster2maintcontent.keySet()) {
	//			ArrayList<PageStat>  pses = cluster2maintcontent.get(key);

				//HashMap<String, ArrayList<XpathStat> > xpath2stat = new HashMap<String, ArrayList<XpathStat> >();
				HashMap<Integer, HashMap<String, ArrayList<XpathStat> > > type_xpath2stat = new HashMap<Integer, HashMap<String, ArrayList<XpathStat> > >();
				for (PageStat ps: pses) {
					for (Integer block_type: ps.xses.keySet()) {
						HashMap<String, ArrayList<XpathStat> > xpath2stat = type_xpath2stat.get(block_type);
						if (xpath2stat == null) {
							xpath2stat = new HashMap<String, ArrayList<XpathStat> >();
							type_xpath2stat.put(block_type, xpath2stat);
						}
						for (XpathStat xs: ps.xses.get(block_type)) {

							xs.url = ps.url;
							xs.title = ps.title;
							ArrayList<XpathStat> temp = xpath2stat.get(xs.xpath);
							if (temp != null) {
								temp.add(xs);
							} else {
								temp = new ArrayList<XpathStat>();
								temp.add(xs);
								xpath2stat.put(xs.xpath, temp);
							}
						}
					}

				}
				
				int all = pses.size();
				StringBuffer out = new StringBuffer();
				out.append(key);
				for (Integer block_type: type_xpath2stat.keySet()) {
					HashMap<String, ArrayList<XpathStat> > xpath2stat = type_xpath2stat.get(block_type);
					HashSet<HtmlXpath> candidate_mces = new HashSet<HtmlXpath>();
					HashMap<String, HashSet<String>> otherxpath2url = new HashMap<String, HashSet<String>>();
					
					for (String xp: xpath2stat.keySet()) {

						ArrayList<XpathStat> xses = xpath2stat.get(xp);
						HashSet<String> urls = new HashSet<String>();
						HashSet<String> titles = new HashSet<String>();
					//	if (xses.size()*3 > all) {
							int mc_num = 0;
							int num = 0;
							HashMap<String, Integer> numXpath2num = new HashMap<String, Integer>();
							HashMap<String, HashSet<Long> > numXpath2code = new HashMap<String, HashSet<Long> >();
							HashSet<Long> codes = new HashSet<Long>();
							for (XpathStat xs: xses) {
								urls.add(xs.url);
								titles.add(xs.title);
								mc_num += xs.mc_num;
								num += xs.num;
								codes.add(xs.hash_code);
								for (String numXpath: xs.numXpath2code.keySet()) {
									Integer t = numXpath2num.get(numXpath);
									if (t != null) {
										numXpath2num.put(numXpath, t+1);
										numXpath2code.get(numXpath).add(xs.numXpath2code.get(numXpath));
									} else {
										numXpath2num.put(numXpath, 1);
										HashSet<Long> temp = new HashSet<Long>();
										temp.add(xs.numXpath2code.get(numXpath));
										numXpath2code.put(numXpath, temp);
									}
								}
							}
							int uniq_page_num = titles.size();
							if (((block_type == 1 && (mc_num < 4*xses.size() || mc_num*3<num) && mc_num < 5*xses.size()) || block_type != 1) && (mc_num < num || block_type != 14) && mc_num*2 <= num) {
						//		System.out.println(key + "\t" + xp + "\t" + xses.size());
								for (String num_xpath: numXpath2num.keySet()) {
									int n = numXpath2num.get(num_xpath);
									if (n*3>all) {
							//			System.out.println(num_xpath + "\t" + n);
										int uniq_size = numXpath2code.get(num_xpath).size();
										int temp_n = n;
										if (temp_n > uniq_page_num) {
											temp_n = uniq_page_num;
										}
										if (!isRepeat(temp_n, uniq_size) || block_type != 1) {
											HtmlXpath hx = new HtmlXpath();
											hx.class_xpath = xp;
											hx.use_num = true;
											hx.type = block_type;
											hx.num_xpath = num_xpath;
											Iterator<HtmlXpath> iter = candidate_mces.iterator();
											int t = 0;
											int depth = hx.class_xpath.split("/").length;
											while (iter.hasNext()) {
												HtmlXpath temp = iter.next();
												int temp_depth = temp.class_xpath.split("/").length;
												if (temp.class_xpath.length() <= hx.class_xpath.length() && temp.use_num == false && depth > temp_depth) {
													if ((hx.class_xpath.startsWith(temp.class_xpath) && temp.class_xpath.endsWith("]"))
															|| (!temp.class_xpath.endsWith("]") && hx.class_xpath.startsWith(temp.class_xpath) && !hx.class_xpath.startsWith(temp.class_xpath+"["))) {

													//	if (hx.class_xpath.startsWith(temp.class_xpath)) {
															System.out.println("here.");
															break;
														}
												}
												++t;
											}
											if (t >= candidate_mces.size()) {
												candidate_mces.add(hx);
											}
											//out.append("\t" + hx.toString());
										} else {
								//			System.out.println(key + "\t" + num_xpath + "\t" + n + "\t" + uniq_size);
										}
									}
								}
							} else {

								if (!isRepeat(uniq_page_num, codes.size()) || block_type != 1) {

									if (xses.size()*3 > all) {

										HtmlXpath hx = new HtmlXpath();
										hx.class_xpath = xp;
										hx.use_num = false;
										hx.type = block_type;
										int t = 0;
										Iterator<HtmlXpath> iter = candidate_mces.iterator();
										boolean flag = false;
										int depth = hx.class_xpath.split("/").length;
										while (iter.hasNext()) {
											HtmlXpath temp = iter.next();
											int temp_depth = temp.class_xpath.split("/").length;
											if (temp.class_xpath.length() <= hx.class_xpath.length() && temp.use_num == false && temp_depth < depth) {
												//		if (!xpath_pairs.contains(temp.class_xpath + "\t" + hx.class_xpath)) {
												if ((hx.class_xpath.startsWith(temp.class_xpath) && temp.class_xpath.endsWith("]"))
														|| (!temp.class_xpath.endsWith("]") && hx.class_xpath.startsWith(temp.class_xpath) && !hx.class_xpath.startsWith(temp.class_xpath+"["))) {
															//	System.out.println("here.");
													break;
												}
												//	}
											} else if (depth < temp_depth) {
												//	if (!xpath_pairs.contains(hx.class_xpath+"\t"+temp.class_xpath)) {
												if ((temp.class_xpath.startsWith(hx.class_xpath) && hx.class_xpath.endsWith("]"))
														|| (!hx.class_xpath.endsWith("]") && temp.class_xpath.startsWith(hx.class_xpath) && !temp.class_xpath.startsWith(hx.class_xpath+"["))) {

									//			if (temp.class_xpath.startsWith(hx.class_xpath)) {
													iter.remove();
															System.out.println("here2." + "\t" + hx.class_xpath + "\t" + temp.class_xpath);
													flag = true;
													//	break;
												}
												//	}
											}
											++t;
										}
										if (t >= candidate_mces.size() || flag) {
											candidate_mces.add(hx);
										}
									} else {
										int pos = xp.lastIndexOf("/");
										if (pos > 0) {
											String prefix = xp.substring(0, pos);
											HashSet<String> t = otherxpath2url.get(prefix);
											if (t != null) {
												for (String url: urls) {
													t.add(url);
												}
											} else {
												t = new HashSet<String>();
												for (String url: urls) {
													t.add(url);
												}
												otherxpath2url.put(prefix, t);
											}
											
											t = otherxpath2url.get(xp);
											if (t != null) {
												for (String url: urls) {
													t.add(url);
												}
											} else {
												t = new HashSet<String>();
												for (String url: urls) {
													t.add(url);
												}
												otherxpath2url.put(xp, t);
											}
										}
									}
								//	System.out.println(hx.toString());
									//out.append("\t" + hx.toString());
								} else {
							//		System.out.println(key + "\t" + xp + "\t" + xses.size() + "\t" + codes.size());
								}
							}
				//		}
					}
					ArrayList<String> other_mc = new ArrayList<String>();
					for (String xp: otherxpath2url.keySet()) {
						int num = otherxpath2url.get(xp).size();
						if (num*3>all) {
							boolean flag = false;
							for (HtmlXpath hx_out: candidate_mces) {
								if (hx_out.class_xpath.startsWith(xp)) {
									flag = true;
									break;
								}
							}
							if (!flag) {
							//	System.out.println(key + "\t" + xp);
								other_mc.add(xp);
							}
						}
					}
					HashSet<String> not_mc = new HashSet<String>();
					for (int i = 0; i < other_mc.size(); ++i) {
						for (int j = 0; j < i; ++j) {
							String s1 = other_mc.get(i);
							String s2 = other_mc.get(j);
							if (s1.length() < s2.length() && s2.startsWith(s1)) {
								not_mc.add(s1);
							} else if (s1.length() > s2.length() && s1.startsWith(s2)) {
								not_mc.add(s2);
							}
						}
					}
					for (String url: other_mc) {
						if (!not_mc.contains(url)) {
							HtmlXpath hx = new HtmlXpath();
							hx.use_num = false;
							hx.class_xpath = url;
							hx.type = block_type;
							candidate_mces.add(hx);
							//System.out.println(key + "\t" + url);
						}
					}
					for (HtmlXpath hx_out: candidate_mces) {
						out.append("\t"+hx_out.toString());
					}
				}

				context.write(new Text(out.toString()), null);
			//	System.out.println(out);
		//	}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleIndexUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool tool = new MRClusterWebkitMaincontentGetter();
		ToolRunner.run(tool, args);
	}

}
