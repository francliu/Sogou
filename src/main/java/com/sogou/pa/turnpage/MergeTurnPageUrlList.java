package com.sogou.pa.turnpage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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


public class MergeTurnPageUrlList extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("run start time:"+df.format(new Date()));
		ToolRunner.run(new MergeTurnPageUrlList(), args);
		Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		long start_time = date1.getTime();
		System.out.println("run end time:"+df.format(new Date()));
		long end_time = date2.getTime();
        long interval_time = end_time - start_time > 0 ? end_time - start_time:0;
        System.out.println("total time:"+interval_time/1000/60+"min");
	}
	
	public int run(String[] args) throws Exception {
		try {
			
			String PreInput = args[0];
			String CurrentInput = args[1];
			String MergeTurnOutput = args[2];
			int reduceCount = Integer.parseInt(args[3]);
			Configuration conf = getConf();	
			Job job = Job.getInstance(conf);
			job.setJobName("Test......");
			job.setMapperClass(Map.class);
			job.setReducerClass(Reduce.class);
			
			job.setJarByClass(MergeTurnPageUrlList.class);
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(Text.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setNumReduceTasks(reduceCount);
			FileInputFormat.addInputPath(job, new Path(PreInput));
			FileInputFormat.addInputPath(job, new Path(CurrentInput));
			FileOutputFormat.setOutputPath(job, new Path(MergeTurnOutput));
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
		public void map(Object key, Text value, Context context) 
				throws IOException, InterruptedException
		{ 
			String line = value.toString();
			String[] tmp =  line.split("\\s");
			StringBuffer values = new StringBuffer();
			for(int i=1;i<tmp.length;i++)
	        {
	        	if(tmp[i].trim().length()>=1)
	        	{
	        		values.append(tmp[i].trim()+"|");
	        	}
	        }
			if(tmp.length>=3)
			{
				context.write(new Text(tmp[0]), new Text(values.toString()));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text>
	{
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException
		{
			int pageNum=-1;
			for (Text value : values) {
				String turnpage = value.toString();
				String[] tmp =  turnpage.split("\\|");
				System.out.print(turnpage);
		        for(int i=0;i<tmp.length;i++)
		        {
		        	System.out.println(i+"个"+tmp[i]);
		        }
				if(Integer.parseInt(tmp[0])==1)
				{
					pageNum = Integer.parseInt(tmp[1]);
				}
			}
			if(pageNum>1)
			{
				context.write(new Text(key), new Text(" 1 "+String.valueOf(pageNum)));
			}
		}
	}
}
