package com.sogou.pa.tools;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class UrlCount extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception
	{
		ToolRunner.run(new UrlCount(), args);
	}
	
	public int run(String[] args) throws Exception {
		try {
			String hadoopInput = args[0];
			String hadoopOutput = args[1];
			int reduceCount = Integer.parseInt(args[2]);
			
			Configuration conf = getConf();	
			Job job = Job.getInstance(conf);
			job.setJobName("Test......");
			job.setMapperClass(Map.class);
			job.setReducerClass(Reduce.class);
			
			job.setJarByClass(UrlCount.class);
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(Text.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setNumReduceTasks(reduceCount);
			
			FileInputFormat.setInputPaths(job, new Path(hadoopInput));
			FileOutputFormat.setOutputPath(job, new Path(hadoopOutput));
			if (job.waitForCompletion(true) && job.isSuccessful())
			{
				return 0;
			}
			return 2;
		}catch(Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	
	public static class Map extends Mapper<Object, Text, Text, Text>
	{

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
	        
	        Pattern p = Pattern.compile("^urllist:+([^\\\n]+)");
		    Matcher m = p.matcher(line);
		    if(m.find()) {
		    	String[] urls = m.group(1).split("\\s"); 
		    	for(int i=0;i<urls.length;i++)
		    	{
		    		if(i==0)
		    		{
		    			String[] tmp = urls[i].split("#");
		    			//System.out.println(tmp[tmp.length-1]);
		    			context.write(new Text(tmp[tmp.length-1]), new Text("1"));
		    		}
		    		else if(urls[i].toString().length()>=2)
		    		{
		    			context.write(new Text(urls[i]), new Text("1"));
		    			//System.out.println(urls[i]);
		    		}
		    	}
		    } 
		}
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text>
	{
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException
		{
			long count = 0;
			for (Text value : values) {
				count++;
			}
			if(count>1000)context.write(key, new Text(Long.toString(count)));
		}
	}
}
