package com.sogou.cm.pa.multipage.maincontent;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
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
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRMaincontentGetter  extends Configured implements Tool {

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
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			boolean is_webkit = false;
			if (fname.indexOf("webkit_maincontent") >= 0) {
				String[] segs = value.toString().split("\t");
				String clusterid = segs[0];
				for (int i = 1; i < segs.length; ++i) {
					HtmlXpath hx = new HtmlXpath();
					hx.fromString(segs[i]);
					String xpath = "";
					if (hx.use_num) {
						xpath = hx.num_xpath;
					} else {
						xpath = hx.class_xpath;
					}
					//System.out.println(hx.type + "\t" + xpath);
					context.write(new Text(clusterid), new Text("0\t" + hx.type + "\t" + xpath));
				}
				if (segs.length < 2) {
					context.write(new Text(clusterid), new Text("0\t"));
				}
			} else {
				String[] segs = value.toString().split("\t");
				String clusterid = segs[0];
				if (segs.length == 2) {
					context.write(new Text(clusterid), new Text("1\t" + segs[1]));
				}
				if (segs.length < 4) {
					return;
				}
				
				context.write(new Text(clusterid), new Text("1\t" + segs[1] + "\t" + segs[2] + "\t" + segs[3]));
			}

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		MultipleOutputs<Text, Text> mos;
		ArrayList<String> tieba_mcs = new ArrayList<String>();
		ArrayList<String> tieba_reply_tips = new ArrayList<String>();
		
		ArrayList<String> wenwen_mcs = new ArrayList<String>();
		
		//fix a tagsoup bug
		ArrayList<String> bbs_mcs = new ArrayList<String>();
	
		ArrayList<String> wenku_mcs = new ArrayList<String>();
		
	//	ArrayList<String> doc88_mcs = new ArrayList<String>();
		
		ArrayList<String> docin_mcs = new ArrayList<String>();
		
		//ArrayList<String> xici_mcs = new ArrayList<String>();
		
		ArrayList<String> focus_recommands = new ArrayList<String>();
		
		protected void setup(Context context) throws IOException {
			mos = new MultipleOutputs<Text, Text>(context);
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container clearfix']/div[@class='left_section']/div[@class='core']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post']/div[@class='d_post_content_main']");
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container clearfix']/div[@class='left_section']/div[@class='core']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post']/div[@class='d_author']");
			
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container clearfix']/div[@class='left_section']/div[@class='core']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder']/div[@class='d_post_content_main']");
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container clearfix']/div[@class='left_section']/div[@class='core']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder']/div[@class='d_author']");
			
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder l_post_bright']/div[@class='d_post_content_main d_post_content_firstfloor']");
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder l_post_bright']/div[@class='d_author']");

			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright noborder']/div[@class='d_post_content_main d_post_content_firstfloor']");
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright noborder']/div[@class='d_author']");

			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright']/div[@class='d_post_content_main']");
			tieba_mcs.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright']/div[@class='d_author']");
			
			tieba_mcs.add("/html/body/div[@class='l_container']/div[@class='l_core']/div[@class='p_postlist']/div[@class='l_post']/div[@class='p_post']");
			
			tieba_reply_tips.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright noborder']/div[@class='d_post_content_main d_post_content_firstfloor']/div[@class='core_reply j_lzl_wrapper']/div[@class='core_reply_tail']");
			tieba_reply_tips.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder l_post_bright']/div[@class='d_post_content_main d_post_content_firstfloor']/div[@class='core_reply j_lzl_wrapper']/div[@class='core_reply_tail']");
			tieba_reply_tips.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright noborder']/div[@class='d_post_content_main d_post_content_firstfloor']/div[@class='thread_recommend']");
			tieba_reply_tips.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post noborder l_post_bright']/div[@class='d_post_content_main d_post_content_firstfloor']/div[@class='thread_recommend']");
			tieba_reply_tips.add("/html/body/div[starts-with(@class, 'wrap')]/div[starts-with(@class, 'wrap')]/div[@id='container' and @class='l_container']/div[@class='content clearfix']/div[@id='pb_content' and @class='pb_content clearfix']/div[@class='left_section']/div[@id='j_p_postlist' and @class='p_postlist']/div[@class='l_post l_post_bright']/div[@class='d_post_content_main']/div[@class='core_reply j_lzl_wrapper']/div[@class='core_reply_tail']");
			
			wenwen_mcs.add("/html/body/div[@id='s_page']/div[@id='s_main']/div[@class='container']/div[@class='column1']");
			bbs_mcs.add("/html/body/div[@id='wp' and @class='wp']/div[@id='ct' and @class='wp cl']/table/tbody/tr/td[@class='plc']/div[@class='pct']");
			bbs_mcs.add("/html/body/table/tbody/tr/td[@class='plc']/div[@class='pct']/div[@class='pcb']/div[@class='t_fsz']/table/tr");
		
			wenku_mcs.add("/html/body/div[@id='doc' and @class='page']/div[@id='bd']/div[@class='bd-wrap']/div[@class='body']/div[@class='main']/div[@class='mod doc-main']/div[@class='inner']");
		
		//	doc88_mcs.add("/html/body/div[@class='center-frame']/div[@class='frm-right']/div[@class='doc-detail']");
		//	doc88_mcs.add("/html/body/div[@class='center-frame']/div[@class='frm-left']");
			
			docin_mcs.add("/html/body/div[@id='wapper-end' and @class='wapper clear']/div[@id='player-end' and @class='grid gridPad clear']/div[@class='doc-player doc-noads-player']");
			docin_mcs.add("/html/body/div[@id='wapper-end' and @class='wapper clear']/div[@id='commonts-end' and @class='grid clear']/div[@class='side-column']/div[@id='documentinfo' and @class='doc-info clear doc-noads-info']");
			docin_mcs.add("/html/body/div[@id='page']/div[@class='page_wrap']/div[@class='page_body clear']/div[@class='main']/div[@class='doc_main']/div[@class='doc_reader_mod']");
		//	xici_mcs.add("/html/body/div[1]/div[3]");
			
			focus_recommands.add("/html/body/div[@class='luntan']/div[@class='louzhu area']/div[@class='right']/div[@class='box_r']/div[@id='box_content' and @class='box_content']/div[@class='container']");
			focus_recommands.add("/html/body/div[@class='luntan']/div[@class='louzhu area']/div[@class='right']/div[@class='box_r']/div[@id='box_content' and @class='box_content']/div[@class='ad_bbs_block']");

		}
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String clusterid = key.toString();
			HashSet<String> xpathes = new HashSet<String>();
			int webkit_mc_num = 0;
			HashMap<String, ArrayList<Integer> > xpath2type = new HashMap<String, ArrayList<Integer> >();
			HashSet<String> urls = new HashSet<String>();
			HashMap<String, HashMap<String, Integer> > xpath2num = new HashMap<String, HashMap<String, Integer> >();
			int webkit_crawl_fail_num = 0;
			int webkit_crawl_succ_num = 0;
			for (Text val: values) {
				String s = val.toString();
				if (s.startsWith("0") && s.length() > 4) {
					String[] segs = s.split("\t");
					if (segs.length == 3) {
						xpathes.add(segs[2]);
						Integer type = Integer.valueOf(segs[1]);
						if (type == 1) {
							++webkit_mc_num;
						}
		//				System.out.println("hh: "+ type + "\t" + segs[2]);
						ArrayList<Integer> types = xpath2type.get(segs[2]);
						if (types == null) {
							types = new ArrayList<Integer>();
							types.add(type);
							xpath2type.put(segs[2], types);
						} else {
							types.add(type);
						}
					}
					
				} else if (s.startsWith("1")) {
					String[] segs = s.substring(2).split("\t");
					if (segs.length == 1) {
						webkit_crawl_fail_num++;
						continue;
					}
					if (segs.length != 3) {
						continue;
					}
					webkit_crawl_succ_num++;
					String xpath = segs[1];
					String match_xpath = segs[2];
					urls.add(segs[0]);
					HashMap<String, Integer> temp = xpath2num.get(xpath);
					if (temp != null) {
						Integer num = temp.get(match_xpath);
						if (num != null) {
							temp.put(match_xpath, num+1);
						} else {
							temp.put(match_xpath, 1);
						}
					} else {
						temp = new HashMap<String, Integer>();
						temp.put(match_xpath, 1);
						xpath2num.put(xpath, temp);
					}
				}
			}
			if (webkit_crawl_succ_num < webkit_crawl_fail_num && webkit_crawl_succ_num < 3) {
				return;
			}
			HashMap<Integer, ArrayList<String> > type2xpathes = new HashMap<Integer, ArrayList<String> >();
			StringBuffer sb = new StringBuffer();
			sb.append(clusterid);
			ArrayList<String> main_xpathes = new ArrayList<String>();
			for (String xpath: xpathes) {
				if (clusterid.startsWith("site www.letv.com")) {
					if (xpath.equals("/html/body/div[@class='column le_other']/div[@class='column_right']/div[@class='Info']")) {
						main_xpathes.add("/html/body/div[@class='column le_other']/div[@class='column_right']/div[@class='Info j-exposure-stat']");
					//	continue;
					}
					if (xpath.equals("/html/body/div[@class='layout_play']/div[@class='column le_path']")) {
						continue;
					}
				}
				if (clusterid.startsWith("site v.youku.com")) {
					if (xpath.equals("/html/body/div[@class='window']/div[@class='screen']/div[@class='s_body']/div[starts-with(@class, 's_main layout_')]/div[@class='mainCol']/div[starts-with(@id, 'vpactionv')]/div[starts-with(@id, 'vpactionv')]/div[@class='yk-interact']/div[starts-with(@id, 'vpvideoinfov')]")) {
						main_xpathes.add("/html/body/div[@class='window']/div[@class='screen']/div[@class='s_body']/div[starts-with(@class, 's_main layout_')]/div[@class='mainCol']/div[starts-with(@id, 'vpactionv')]/div[starts-with(@id, 'vpactionv')]/div[@class='yk-interact scroll-paction']/div[starts-with(@id, 'vpvideoinfov')]");
					}
				}
				HashMap<String, Integer> temp = xpath2num.get(xpath);
				if (temp != null) {
					String max_xpath = "";
					Integer max = -1;
					Iterator iter = temp.entrySet().iterator();
					int cnt = 0;
					while (iter.hasNext()) {
						Entry entry = (Entry) iter.next();
						String k = (String) entry.getKey();
						Integer val = (Integer) entry.getValue();
						cnt += val;
						if (val > max) {
							max = val;
							max_xpath = k;
						}
					}
					if (max > 0 && max*3 > cnt) {
						ArrayList<Integer> types = xpath2type.get(xpath);
						if (types != null && types.size() > 0) {
							for (Integer type: types) {
								if (type != 1) {
				//					System.out.println(type + "\t" + max_xpath);
									ArrayList<String> type_xpathes = type2xpathes.get(type);
									if (type_xpathes == null) {
										type_xpathes = new ArrayList<String>();
										type2xpathes.put(type, type_xpathes);
									}
									type_xpathes.add(max_xpath);
								} else {
									boolean flag = false;
									for (String bbs_xpath: bbs_mcs) {
										if (max_xpath.startsWith(bbs_xpath)) {
											main_xpathes.addAll(bbs_mcs);
											flag = true;
											break;
										}
									}
									if (!flag) {
										main_xpathes.add(max_xpath);
										
										//fix a tagsoup bug
										if (max_xpath.startsWith("/html/body/div[@id='wp' and @class='wp']/div[@id='ct' and @class='wp cl']")) {
											main_xpathes.addAll(bbs_mcs);
										}
									}
									String spider_xpath = xpath.replaceAll("/tbody", "");
									if (!spider_xpath.equals(max_xpath)) {
										int num = 0;
										for (String url: urls) {
											if (url.indexOf("bbs")>=0 && url.indexOf("thread")>=0) {
												++num;
											}
										}
										if (num >= urls.size()) {
											main_xpathes.add(spider_xpath);
										}
									}
									
								}
							}
						}

					//	sb.append("\t" + max_xpath);
					}
				}
			}
			if (clusterid.indexOf("tieba.baidu.com") >= 0) {
				ArrayList<String> temp = new ArrayList<String>();
				for (String xpath: main_xpathes) {
					boolean flag = false;
					for (String tieba_mc: tieba_mcs) {
						if (tieba_mc.startsWith(xpath)) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						temp.add(xpath);
					}
				}
				main_xpathes.clear();
				main_xpathes.addAll(temp);
				main_xpathes.addAll(tieba_mcs);
				
				ArrayList<String> reply_tips = type2xpathes.get(5);
				if (reply_tips == null) {
					reply_tips = new ArrayList<String>();
				}
				temp = new ArrayList<String>();
				for (String reply_tip: reply_tips) {
					boolean flag = false;
					for (String tieba_rt: tieba_reply_tips) {
						if (tieba_rt.startsWith(reply_tip)) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						temp.add(reply_tip);
					}
				}
				reply_tips.clear();
				reply_tips.addAll(temp);
				reply_tips.addAll(tieba_reply_tips);
				type2xpathes.put(5, reply_tips);
				
			}
			if (clusterid.indexOf("wenwen.sogou.com") >= 0) {
				main_xpathes.addAll(wenwen_mcs);
			}
			if (clusterid.indexOf("focus.cn") >= 0) {
				ArrayList<String> recommands = type2xpathes.get(7);
				if (recommands == null) {
					recommands = new ArrayList<String>();
				}
				recommands.addAll(focus_recommands);
				type2xpathes.put(7, recommands);
			}
			
			int wenku_cnt = 0;
			int docin_cnt = 0;
			for (String url: urls) {
			//	System.out.println("url: " + url);
				if (url.startsWith("http://wenku.baidu.com/view")) {
					wenku_cnt++;
				}
				if (url.startsWith("http://www.docin.com/p-")) {
					docin_cnt++;
				}
			}
		//	System.out.println("cnt: " + docin_cnt + "\t" + urls.size());
			if (wenku_cnt == urls.size() && urls.size() >= 1) {
				main_xpathes.addAll(wenku_mcs);
			}
			if (docin_cnt == urls.size() && urls.size() >= 1) {
				main_xpathes.addAll(docin_mcs);
			}
			
			type2xpathes.put(1, main_xpathes);
			for (Integer type: type2xpathes.keySet()) {
				
				ArrayList<String> type_xpathes = type2xpathes.get(type);
		//		System.out.println("here" + type + type_xpathes.size());
				if (type_xpathes.size() > 1) {
					while (true) {
						int cnt = 0;
						for (int i = 0; i < type_xpathes.size()-1; ++i) {
							if (type_xpathes.get(i).length() > type_xpathes.get(i+1).length()) {
								++cnt;
								String temp = type_xpathes.get(i);
								type_xpathes.set(i, type_xpathes.get(i+1));
								type_xpathes.set(i+1, temp);
							}
						}
						if (cnt == 0) {
							break;
						}
						cnt = 0;
						for (int i = type_xpathes.size()-2;i>=0;--i) {
							if (type_xpathes.get(i).length() > type_xpathes.get(i+1).length()) {
								++cnt;
								String temp = type_xpathes.get(i);
								type_xpathes.set(i, type_xpathes.get(i+1));
								type_xpathes.set(i+1, temp);
							}
						}
					}
				}
				ArrayList<String> mx_temp = new ArrayList<String>();
				if (type_xpathes.size() > 0) {
				//	sb.append("\t" + type_xpathes.get(0));
					mx_temp.add(type_xpathes.get(0));
				}

				for (int i = 1; i < type_xpathes.size(); ++i) {
					String xp = type_xpathes.get(i);
					int j = 0;
					for (; j < i; ++j) {
						String temp = type_xpathes.get(j);
						if (xp.equals(temp) || xp.startsWith(temp+"/")) {
							break;
						}
					}
					if (j == i) {
					//	sb.append("\t" + xp);
						mx_temp.add(xp);
					}
				}
				if (mx_temp.size() == 1) {
					String mx = mx_temp.get(0);
					if (mx.equals("/html") || mx.equals("/html/body")) {
						mx_temp.clear();
						type2xpathes.put(type, mx_temp);
						continue;
					}
				}
				type2xpathes.put(type, mx_temp);
			}
			

			main_xpathes = type2xpathes.get(1);
			if (main_xpathes.size() > 0 || webkit_mc_num == 0) {
				for (Integer type: type2xpathes.keySet()) {
					ArrayList<String> type_xpathes = type2xpathes.get(type);
					if (type_xpathes.size() > 0) {
						for (String xpath: type_xpathes) {
							sb.append("\t" + xpath + "\t" + type);
						}
					}
				}
				mos.write(new Text(sb.toString()), null, "maincontent/part");
			} else {
				mos.write(key, null, "badcluster/part");
			}
			
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
			mos.close();
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
		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool tool = new MRMaincontentGetter();
		ToolRunner.run(tool, args);
	}

}
