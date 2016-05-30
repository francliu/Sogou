package com.sogou.cm.pa.multipage.maincontent;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import com.sogou.cm.pa.maincontent.Element;
import com.sogou.cm.pa.maincontent.RuleBasedMainContentExtractor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class XpathStat {
	String xpath;
	String url;
	String title;
//	HashSet<String> num_xpathes;
	HashMap<String, Long> numXpath2code;
	int mc_num;
	int num;
	StringBuffer text;
	Long hash_code;
	int area;
	int img_iframe_area;
	boolean is_video;
	boolean has_download;
	String sep1 = "j7k8t";
	String sep2 = "k7d3u";
	String sep3 = "b9h7t";
	public XpathStat() {
//		num_xpathes = new HashSet<String>();
		xpath = "";
		url = "";
		title = "";
		numXpath2code = new HashMap<String, Long>();
		num = 0;
		mc_num = 0;
		hash_code = 0l;
		text = new StringBuffer();
		area = 0;
		img_iframe_area = 0;
		is_video = false;
		has_download = false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(xpath + sep1);
		//sb.append(url + sep1);
		sb.append(mc_num + sep1);
		sb.append(num + sep1);
		sb.append(hash_code + sep1);
		Iterator iter = numXpath2code.entrySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Entry entry = (Entry)iter.next();
			String xpath = (String)entry.getKey();
			Long code = (Long) entry.getValue();
			if (i == 0) {
				sb.append(xpath + sep3 + code);
			} else {
				sb.append(sep2 + xpath + sep3 + code);
			}
			++i;
		}
		
		return sb.toString();
	}
	
	public void fromString(String s) {
		String[] segs = s.split(this.sep1);
		if (segs.length != 5) {
			return;
		}
		this.xpath = segs[0];
	//	this.url = segs[1];
		this.mc_num = Integer.valueOf(segs[1]);
		this.num = Integer.valueOf(segs[2]);
		this.hash_code = Long.valueOf(segs[3]);
		String[] segs2 = segs[4].split(sep2);
		for (String t: segs2) {
			String[] segs3 = t.split(sep3);
			if (segs3.length != 2) {
				continue;
			}
			String xpath = segs3[0];
			Long code = Long.valueOf(segs3[1]);
			numXpath2code.put(xpath, code);
		}
	}
}

class PageStat {
	String url;
	String title;
	HashMap<Integer, ArrayList<XpathStat> > xses;
	String sep1 = "h9m5r";
	String sep2 = "w7v4d";
	PageStat() {
		url = "";
		title = "";
		xses = new HashMap<Integer, ArrayList<XpathStat> >();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		sb.append(sep1);
		sb.append(title);
		Iterator iter = xses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry)iter.next();
			Integer block_type = (Integer)entry.getKey();
			ArrayList<XpathStat> type_xses = (ArrayList<XpathStat>)entry.getValue();
			if (type_xses.size() > 0) {
				sb.append(sep1 + block_type.toString());
				for (XpathStat xs: type_xses) {
					sb.append(sep2 + xs.toString());
				}
			}
		}
		return sb.toString();
	}
	
	public void fromString(String s) {
		String[] segs = s.split(sep1);
		if (segs.length < 2) {
			return;
		}
		this.url = segs[0];
		this.title = segs[1];
		for (int i = 2; i < segs.length; ++i) {
			String[] segs2 = segs[i].split(sep2);
			if (segs2.length > 1) {
				Integer block_type = Integer.valueOf(segs2[0]);
				ArrayList<XpathStat> type_xses = new ArrayList<XpathStat>();
				for (int j = 1; j < segs2.length; ++j) {
					XpathStat xs = new XpathStat();
					xs.fromString(segs2[j]);
					type_xses.add(xs);
				}
				this.xses.put(block_type, type_xses);
			}
		}
	}
}

public class PageScaner {
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
	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, NoSuchAlgorithmException {
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("C:\\Users\\sunjian\\Downloads\\t_url.txt")));
		String line;
		HashMap<String, String> url2cluster = new HashMap<String, String>();
		while ((line = reader2.readLine()) != null) {
			String[] segs = line.split("\t");
		//	System.out.println(line + '\t' + segs.length);
			url2cluster.put(segs[0], segs[1]);
		}
		reader2.close();
		HashMap<String, ArrayList<PageStat>> cluster2maintcontent = new HashMap<String, ArrayList<PageStat>>();
		HashMap<String, HashMap<String, ArrayList<String> > > cluster2urlmc = new HashMap<String, HashMap<String, ArrayList<String> > >();
		
		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages0");
		FileSplit file_split = new FileSplit(new Path("C:\\Users\\sunjian\\Downloads\\pages0"), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));

		
		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
		codepageDetectorProxy.add(new ByteOrderMarkDetector());
		// this.codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());

		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
		XPathFactory xpathFac = XPathFactory.newInstance();
		XPath xpath = xpathFac.newXPath();
		
		XpathNodeCounter		htmlContentHandler		= new XpathNodeCounter();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		MessageDigest md;
		byte[] dig_id;

		md = MessageDigest.getInstance("md5");

		dig_id = new byte[16];
		Random ran = new Random();
		
		
		int progress = 0;
		while (reader.nextKeyValue()) {
			progress++;
			if (progress % 1000 == 0)
				System.err.println("Progress: " + progress);
			BytesWritable docId = (BytesWritable) reader.getCurrentKey();
			OriginPageWritable opw = (OriginPageWritable) reader.getCurrentValue();
			if (opw == null)
				continue;
			
		//	System.out.println(opw.url);

			if (opw.url.toString().length() > 256) {
				continue;
			}
			
			Attribute err_reason = opw.getAttribute("Error-Reason");
			if (err_reason != null) {
				continue;
			}
			if (opw.body.getLength() > 0) {
				int rc = 0;
				try {

					Attribute originSiteAttr = opw.getAttribute("Original-Size");
					if (originSiteAttr == null)
						throw new IOException("NULL Original-Size");
					int originalSize = Integer.parseInt(originSiteAttr.val.toString());
					if (originalSize > 4096 * 1024) {
						rc = 1;
						throw new Exception("error");
					}
					Inflater decompresser = new Inflater();
					decompresser.setInput(opw.body.getBytes(), 0, opw.body.getLength());
					int resultLength=0;
					try {
						resultLength = decompresser.inflate(output);
					} catch (DataFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (resultLength != originalSize) {
						rc = 2;
						throw new Exception("error");
					}
					decompresser.end();

					String htmlPage = new String(output, 0, resultLength, "UTF-8");

					try {
						//System.out.println(opw.url.toString());
					//	if (!opw.url.toString().equalsIgnoreCase("http://ask.ci123.com/questions/show/584606/"))
						//	continue;
					//	if (opw.url.toString().equals("http://www.job222.com/Company_View.asp?ComId=5488"))
					//		System.out.println(htmlPage);
					//	System.out.println(opw.url.toString());
						List<Element> blocks = main_content.extractAllBlock(opw.url.toString(), htmlPage);
						/*
						System.out.println(opw.url);
						for (Element e: blocks) {
							System.out.println(e.block_type + "\t" + e.full_xpath);
						}
						System.out.println("===============================================");
						*/
						String title = main_content.html_page.title;
						if (title == null || title.length() == 0) {
							title = String.valueOf(ran.nextLong());
						}
						//StringBuffer s = new StringBuffer(opw.url.toString());
						
						HashMap<Integer, HashMap<String, XpathStat>> type_xpathes = new HashMap<Integer, HashMap<String, XpathStat>>();
						HashSet<String> xpath_set = new HashSet<String>();
						for (int cnt = 0; cnt < blocks.size(); ++cnt) {
							Element e = blocks.get(cnt);
			//			for (Element e: blocks) {
							int type = e.block_type;
							if (e.is_maincontent) {
								type = 1;
								if (e.block_type != 1 && e.block_type != 0) {
									e.is_maincontent = false;
									cnt--;
								}
							}
							HashMap<String, XpathStat> xpathes = type_xpathes.get(type);
							if (xpathes == null) {
								xpathes = new HashMap<String, XpathStat>();
								type_xpathes.put(type, xpathes);
							}
							if (e.is_maincontent && e.block_type != 1 && e.block_type != 0) {
								type = e.block_type;
								xpathes = type_xpathes.get(type);
								if (xpathes == null) {
									xpathes = new HashMap<String, XpathStat>();
									type_xpathes.put(type, xpathes);
								}
							}
					//		System.out.println(e.full_xpath);
							md.reset();
							String text = e.text.toString();
							Long docid= new Long(0);
							xpath_set.add(e.full_xpath);
							/*
							if (e.full_xpath.equals("/html/body/div[@id='blog-163-com-main' and @class='nb-wrap wsy']/div[@class='nb-are nb-cnt']/div[@class='wkg']/div[@id='blog-163-com-container' and @class='c wc h clearfix']/div[@id='-3' and @class='nb-mdl lcr m-3']/div[@class='nb-mc lcr']/div[@class='c cc lcr nb-jsc']/div[@class='nbw-ryt ztag clearfix']/div[@class='left']/div[@class='lcnt bdwr bds0 bdc0']/div[@class='mcnt ztag']/div[@class='snl']")) {
								System.out.println("h: " + text + "\t" + e.width + "\t" + e.height);
							}
							*/
						//	System.out.println(e.full_xpath + "\t" + text + "\t" + e.is_video);
							if (text.length() > 0 && e.width*e.height>e.img_iframe_area*2 && e.width*e.height<800000 && !e.is_video && !e.has_download) {
								/*
								if (text.length() < 200) {
									String lcs = Utils.getLCS(text, title);
									if (lcs.length() >= 5) {
										CharSequence lcs2 = lcs;
										CharSequence cs = "";
	
										text = text.replace(lcs2, cs);


									}
								}
								*/
								text = text.replaceAll("[0-9.]+", "0");
								
								

								
								md.update(text.getBytes("utf8"));
								try {
									md.digest(dig_id, 0, 16);
									for (int j = 0; j < dig_id.length; ++j) {
										docid=docid<<8;
										docid += dig_id[j]&0xff;
									}
								} catch (DigestException ee) {
									docid = Long.valueOf(e.text.toString().length());
									// TODO Auto-generated catch block
									ee.printStackTrace();
								}
							} else {
								docid = ran.nextLong();
							}
							/*
							if (opw.url.toString().startsWith("http://news.cntv.cn")
									&& e.full_xpath.equalsIgnoreCase("/html/body/div[@class='bg_center_v_tile']/div[@class='bg_top_h_tile']/div[@class='bg_top_owner']/div[@class='bg_bottom_h_tile']/div[@id='page_body']/div[@class='column_wrapper lc_wi']/div[@class='col_fl col_w637']/div[@class='md md01 md_zhengwen']")) {
								System.out.println(e.full_xpath + "\t" + e.width*e.height + "\t" + e.img_iframe_area +"\t" +  text + "\t" + docid);
							}
							*/
						//	System.out.println(e.full_xpath + "\t" + text + "\t" + docid);

							XpathStat xs = xpathes.get(e.full_xpath);
							if (xs != null) {
								xs.mc_num++;
								xs.area += e.width*e.height;
								xs.img_iframe_area += e.img_iframe_area;
								if (e.is_video) {
									xs.is_video = true;
								}
								if (e.has_download) {
									xs.has_download = true;
								}
								xs.numXpath2code.put(e.num_xpath, docid);
								xs.text.append(text);
							//	xs.num_xpathes.add(e.num_xpath);
							} else {
								xs = new XpathStat();
								xs.xpath = e.full_xpath;
								xs.area = e.width*e.height;
								xs.img_iframe_area = e.img_iframe_area;
								xs.mc_num = 1;
								if (e.is_video) {
									xs.is_video = true;
								}
								if (e.has_download) {
									xs.has_download = true;
								}
								//xs.num_xpathes.add(e.num_xpath);
								xs.numXpath2code.put(e.num_xpath, docid);
								xs.text.append(text);
								xpathes.put(e.full_xpath, xs);
							}
						}
						htmlContentHandler.setXpath(xpath_set);
						parser.parse(new InputSource(new StringReader(htmlPage)));
						PageStat ps = new PageStat();
						ps.url = opw.url.toString();
						ps.title = title;
						for (Integer block_type: type_xpathes.keySet()) {
							HashMap<String, XpathStat> xpathes = type_xpathes.get(block_type);
							ArrayList<XpathStat> xses = new ArrayList<XpathStat>();
							for (String xp: xpathes.keySet()) {
								int num = htmlContentHandler.xpath2num.get(xp);
								XpathStat xs = xpathes.get(xp);
								xs.num = num;
								md.reset();
								String text = xs.text.toString();
								
								Long docid= new Long(0);
								if (text.length() > 0 && xs.area>xs.img_iframe_area*2 && xs.area<800000 && !xs.is_video && !xs.has_download) {
									md.update(text.getBytes("utf8"));
									try {
										md.digest(dig_id, 0, 16);
										for (int j = 0; j < dig_id.length; ++j) {
											docid=docid<<8;
											docid += dig_id[j]&0xff;
										}
									} catch (DigestException ee) {
										docid = Long.valueOf(xs.text.toString().length());
										// TODO Auto-generated catch block
										ee.printStackTrace();
									}
								} else {
									docid = ran.nextLong();
								}
								xs.hash_code = docid;
								xs.text.setLength(0);
								xs.text.trimToSize();
								xses.add(xs);
							}
							ps.xses.put(block_type, xses);
						}


						String cluster = url2cluster.get(opw.url.toString());
						/*
						PageStat ps2 = new PageStat();
						ps2.fromString(ps.toString());
						ps = ps2;
						*/
						if (cluster != null) {
							ArrayList<PageStat> val = cluster2maintcontent.get(cluster);
							if (val == null) {
								val = new ArrayList<PageStat>();
								val.add(ps);
								cluster2maintcontent.put(cluster, val);
							} else {
								val.add(ps);
							}
							
					//		HashMap<String, ArrayList<String>> url2mc = new HashMap<String, ArrayList<String>>();
							
							
						}
						
					//	System.out.println(opw.url.toString());
					//	System.out.println(opw.url);
					} catch (Exception e) {
						
					}
				} catch (Exception e) {
					
				}
			}

			//System.out.println(opw.url.toString() + "\t" + opw.body.getLength());
		}
		
		
		for (String key: cluster2maintcontent.keySet()) {
			ArrayList<PageStat> mc = cluster2maintcontent.get(key);
			for (PageStat ps: mc) {
				System.out.println(ps.url);
				for (Integer block_type: ps.xses.keySet()) {
					for (XpathStat xs: ps.xses.get(block_type)) {
						System.out.println(xs.xpath + "\t" +block_type + "\t" +  xs.mc_num + "\t" + xs.num);
						for (String num_xpath: xs.numXpath2code.keySet()) {
					//		System.out.println(num_xpath);
						}
					}
				}

			}
		}
		
		
		
		for (String key: cluster2maintcontent.keySet()) {
			ArrayList<PageStat>  pses = cluster2maintcontent.get(key);
			//HashMap<String, ArrayList<XpathStat> > xpath2stat = new HashMap<String, ArrayList<XpathStat> >();
		//	
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

		//	
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
						//if (mc_num < 3*xses.size() && mc_num*2 <= num) {
				//		if (((block_type == 1 && mc_num < 3*xses.size()) || block_type != 1) && mc_num*2 <= num) {

						if (((block_type == 1 && (mc_num < 4*xses.size() || mc_num*3<num) && mc_num < 5*xses.size()) || block_type != 1) && (mc_num < num || block_type != 14) && mc_num*2 <= num) {
					//		System.out.println(key + "\t" + xp + "\t" + xses.size());
							if (block_type == 14) {
								System.out.println("hidden:  " + mc_num + "\t" + num);
							}
							for (String num_xpath: numXpath2num.keySet()) {
								int n = numXpath2num.get(num_xpath);
								if (n*3>all) {
						//			System.out.println(num_xpath + "\t" + n);
									int uniq_size = numXpath2code.get(num_xpath).size();
							//		System.out.println(key + "\t" + num_xpath + "\t" + n + "\t" + uniq_size);
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
											//			System.out.println("here.");
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
						//	System.out.println("hereeeee:  "+ xp + "\t" + uniq_page_num + "\t" + codes.size());
							if (!isRepeat(uniq_page_num, codes.size()) || block_type != 1) {
							//	System.out.println("here22222:  "+ xp);
								if (xses.size()*3 > all) {
							//		System.out.println("here:  "+ xp);
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
													//		System.out.println("here.");
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
									//	System.out.println(hx.class_xpath);
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
			//		System.out.println("aaaaaaaa\t" + xp + "\t" + num);
					if (num*3>all) {
						boolean flag = false;
						for (HtmlXpath hx_out: candidate_mces) {
							if (hx_out.class_xpath.startsWith(xp)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
						//	System.out.println("aaaaaaaa\t" + key + "\t" + xp);
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
						hx.type = block_type;
						hx.class_xpath = url;
						candidate_mces.add(hx);
						//System.out.println(key + "\t" + url);
					}
				}
				for (HtmlXpath hx_out: candidate_mces) {
					out.append("\t"+hx_out.toString());
				}
			}

			System.out.println(out);
		}
		
	}
}
