package mapred.ngramcount;

import java.io.IOException;
import mapred.job.Optimizedjob;
import mapred.util.SimpleParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

public class Driver {

	public static void main(String args[]) throws Exception {
		SimpleParser parser = new SimpleParser(args);

		String input = parser.get("input");
		String output = parser.get("output");
		int ngram = parser.getInt("n");
		getJobFeatureVector(input, output,ngram);

	}

	private static void getJobFeatureVector(String input, String output, int ngram)
			throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		conf.setInt("ngram",ngram);
		Optimizedjob job = new Optimizedjob(conf, input, output,
				"Compute NGram Count");

		job.setClasses(NgramCountMapper.class, NgramCountReducer.class, null,null);
		job.setMapOutputClasses(Text.class, NullWritable.class);

		job.run();
	}	
}
