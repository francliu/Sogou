package com.sogou.pa.turnpage;
import java.io.ByteArrayInputStream;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;
import com.sogou.web.selector.urllib.URLUtils;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.web.selector.offsum.OffsumPageInputFormatV2;
import com.sogou.web.selector.offsum.OffsumPageWritableV2;
import com.sogou.web.selector.offsum.OffsumPageWritableV2.ContentItem;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;
import com.sogou.web.selector.urllib.URLUtils;

import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
public class TurnPageRecognizeOffsumPageV3 extends Configured implements Tool {
	/**
	 * @param args
	 * @throws Exception
	 */

	
	private static class ScanMapper extends Mapper<BytesWritable, OffsumPageWritableV2, Text, Text> {
		private HtmlContentDomTree		htmlContentHandler;
		Parser					parser;
		byte[] output = new byte[4096 * 1024 + 1];
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
//			System.out.println(((FileSplit) context.getInputSplit()).getPath());
			    htmlContentHandler		= new HtmlContentDomTree();
				parser = new Parser();
				parser.setContentHandler(htmlContentHandler);
				codepageDetectorProxy.add(new ByteOrderMarkDetector());
				// this.codepageDetectorProxy.add(new ParsingDetector(false));
				codepageDetectorProxy.add(JChardetFacade.getInstance());
		}
		@Override
		protected void map(BytesWritable key, OffsumPageWritableV2 value, Context context) throws IOException, InterruptedException {
			if (key == null)
				return;
			if (value == null) {
				return;
			}
			String site = URLUtils.getDomainWithoutPort(value.url.toString());
			String domain = URLUtils.getMainDomain(value.url.toString());
			if (domain == null || site == null || domain.length() == 0 || site.length() == 0) {
				return;
			}
			int originalSize = 0;

			for(ContentItem ci : value.contentItems){
				if (ci.type.equals("snapshot") == false)
				{
					context.getCounter("debug", "is not snapshot page").increment(1);
					continue;
				}
				else
				{
					context.getCounter("debug", "is snapshot page").increment(1);
				}
				originalSize = ci.originalSize;
				if (ci.content.length > 0) {
					if(value.url.toString().toLowerCase().matches(".*?\\.txt$"))
					{
						context.getCounter("debug", "txt page").increment(1);
						continue;
					}
					if(value.url.toString().toLowerCase().matches(".*?\\.pdf$"))
					{
						context.getCounter("debug", "pdf page").increment(1);
						continue;
					}
					int rc = 0;
					try {
						if (originalSize > 4096 * 1024) {
							rc = 1;
							context.getCounter("debug", "originalSize > 4096 * 1024").increment(1);
							continue;
						}
						Inflater decompresser = new Inflater();
						decompresser.setInput(ci.content);
						int resultLength=0;
						try {
							resultLength = decompresser.inflate(output);
						} catch (DataFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (resultLength != originalSize) {
							rc = 2;
							context.getCounter("debug", "resultLength isnotequeal originalSize").increment(1);
							continue;
						}
						decompresser.end();
						if(resultLength>1024*256)
						{
							context.getCounter("debug", "resultLength > 1024*256").increment(1);
							continue;
						}
						int len = resultLength < 50000 ? resultLength : 50000;
						ByteArrayInputStream in = new ByteArrayInputStream(output);
						Charset charset = codepageDetectorProxy.detectCodepage(in, len);
						if(charset.toString().compareTo("void")==0)
						{
							context.getCounter("debug", "charset isnotnormal page").increment(1);
							continue;
						}
						
						String htmlPage = new String(output, 0, resultLength, charset);
						int i = 0;
						while (true) {
							int j = htmlPage.indexOf("\n", i);
							if (j < 0 || j + 2 >= htmlPage.length()) {
								i = -1;
								break;
							}
							if (htmlPage.charAt(j+1) == '\n') {
								i = j+2;
								break;
							} else if (htmlPage.charAt(j+1) == '\r' && htmlPage.charAt(j+2) == '\n') {
								i = j+3;
								break;
							} else {
								i = j+1;
							}
						}
						if ( i < 0) {
							context.getCounter("debug", "strage page").increment(1);
							continue;
						}
//						htmlPage = htmlPage.substring(i);
						try {
							context.getCounter("debug", "normal page").increment(1);
							String url = value.url.toString();
							System.out.println("url:"+url);
							htmlPage = htmlPage.toLowerCase();
							if(htmlPage.contains("</html>")
									||htmlPage.contains("</body>")
									||htmlPage.contains("</head>")
									||htmlPage.contains("<html")
									||htmlPage.contains("<link")
									||htmlPage.contains("<ul")
									||htmlPage.contains("<li")
									||htmlPage.contains("</ul>")
									||htmlPage.contains("html>")
									||htmlPage.contains("<body")
									||htmlPage.contains("<head")
									||htmlPage.contains("<div")
									||htmlPage.contains("</div>")
									||htmlPage.contains("<script")
									||htmlPage.contains("</script>"))
							{
								context.getCounter("debug", "normal html page").increment(1);
								parser.parse(new InputSource(new StringReader(htmlPage)));
								if(HtmlContentDomTree.is_abnormal_page==false)
								{
									context.getCounter("debug", "no_abnormal_page html page").increment(1);
								}
								else
								{
									context.getCounter("debug", "is_abnormal_page html page").increment(1);
								}
								boolean trunPage = htmlContentHandler.IsTurnPage();
								if(HtmlContentDomTree.IsFristPage!=1&&!trunPage)
								{
									trunPage= TagContentTool.IsTurnByUrl(url);
								}
								int pagination=TagContentTool.getPagination(url);
								if(trunPage)
								{
									context.write(new Text(url), new Text(" 1 "+String.valueOf(pagination)));
								}
								else
								{
									context.write(new Text(url), new Text(" 0 "+String.valueOf(pagination)));
								}
							}
							else
							{
								context.write(new Text(url), new Text(" 2  0 "));
							}
							htmlContentHandler.clear();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
//							System.out.println(value.url);
							e.printStackTrace();
							context.getCounter("SCANNER", "Exception").increment(1);
						}
					} catch (Throwable e) {
//						System.out.println(value.url);
						e.printStackTrace();
						context.getCounter("SCANNER", "ErrorContent:" + rc).increment(1);
					}

				}
			}
		}
	}

	private static class ScanReducer extends
	Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			System.out.println("cluster key: " + key.toString());
			for (Text val: values) {
				context.write(key, val);
			}
		}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();	
		Job job = Job.getInstance(conf);

		job.setJarByClass(this.getClass());

		job.setMapperClass(ScanMapper.class);
		job.setReducerClass(ScanReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

	//	job.setPartitionerClass(ScanPartitioner.class);

		job.setInputFormatClass(OffsumPageInputFormatV2.class);
	//	job.setOutputFormatClass(OriginPageOutputFormat.class);
	//	job.setOutputFormatClass(GBKOutputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
	//	LazyOutputFormat.setOutputFormatClass(job, GBKOutputFormat.class);

		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool pageScanner = new TurnPageRecognizeOffsumPageV3();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("run start time:"+df.format(new Date()));
		ToolRunner.run(pageScanner, args);
		Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		long start_time = date1.getTime();
		System.out.println("run end time:"+df.format(new Date()));
		long end_time = date2.getTime();
        long interval_time = end_time - start_time > 0 ? end_time - start_time:0;
        System.out.println("total time:"+interval_time/1000/60+"min");
	}

}
