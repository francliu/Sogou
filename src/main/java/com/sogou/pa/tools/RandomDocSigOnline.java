package com.sogou.pa.tools;
import java.io.IOException;

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


public class RandomDocSigOnline extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception
	{
		ToolRunner.run(new RandomDocSigOnline(), args);
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
			
			job.setJarByClass(RandomDocSigOnline.class);
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
			String[] tmp =  line.split("\\|");
			if(tmp.length>=24)
			{
				int one = tmp[2].charAt(2)-'0';
				int two = tmp[1].charAt(2)-'0';
				int turn = 1<<1;
				//System.out.println(onevale+"|"+(two&turn));
				if((one&turn)==2||(two&turn)==2)
				{
					//System.out.println(tmp[0].split("\\s")[0]);
					context.write(new Text(tmp[0].split("\\s")[0]+"|"+tmp[23]), new Text("1"));
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
			context.write(key, new Text(Long.toString(count)));
		}
	}
}
