package mapred.hashtagsim_optimize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;

public class SimilarityReducer extends Reducer<Text, IntWritable, Text, IntWritable> {


	@Override
	protected void reduce(Text key, Iterable<IntWritable> value,
			Context context)
			throws IOException, InterruptedException{
		int sum = 0;
		for( IntWritable c : value ){
			sum += Integer.parseInt(c.toString());
		}
		context.write(key, new IntWritable(sum));
	}


}
