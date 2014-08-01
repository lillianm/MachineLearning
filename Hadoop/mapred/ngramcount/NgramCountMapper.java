package mapred.ngramcount;

import java.io.IOException;

import mapred.util.Tokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class NgramCountMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		String[] words = Tokenizer.tokenize(line);
		int n = context.getConfiguration().getInt("ngram",1);
		if(n >=1){
			for(int i=0;i<words.length-n;i++){
				StringBuilder builder = new StringBuilder();
				for(int j=0;j<n;j++){
					if(j>0)
						builder.append(" ");
					builder.append(words[i+j]);
				}
				context.write(new Text(builder.toString()), NullWritable.get());
			}
		}
		/*for (String word : words)
			context.write(new Text(word), NullWritable.get());*/

	}
}
