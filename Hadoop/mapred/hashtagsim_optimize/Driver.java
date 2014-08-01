package mapred.hashtagsim_optimize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import mapred.hashtagsim_optimize.SimilarityGroupComparator;
import mapred.job.Optimizedjob;
import mapred.util.FileUtil;
import mapred.util.InputLines;
import mapred.util.SimpleParser;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class Driver {
	
	public static void main(String args[]) throws Exception {
		SimpleParser parser = new SimpleParser(args);

		String input = parser.get("input");
		String output = parser.get("output");
		String tmpdir = parser.get("tmpdir");

		// getJobFeatureVector(input, tmpdir + "/job_feature_vector");
		//
		// String jobFeatureVector = loadJobFeatureVector(tmpdir
		// + "/job_feature_vector");
		//
		// System.out.println("Job feature vector: " +
		// jobFeatureVector);
		//
		getHashtagFeatureVector(input, tmpdir + "/feature_vector");

		Map<String, String> allFeatureVectors = loadAllFeatureVector(tmpdir
				+ "/feature_vector");
        
        getHashtagSimilarities(allFeatureVectors, tmpdir
                               + "/feature_vector", output);
	}

	/**
	 * Computes the word cooccurrence counts for hashtag #job
	 * 
	 * @param input
	 *                The directory of input files. It can be local
	 *                directory, such as "data/", "/home/ubuntu/data/", or
	 *                Amazon S3 directory, such as "s3n://myawesomedata/"
	 * @param output
	 *                Same format as input
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private static void getJobFeatureVector(String input, String output)
			throws IOException, ClassNotFoundException,
			InterruptedException {
		Optimizedjob job = new Optimizedjob(new Configuration(), input,
				output, "Get feature vector for hashtag #Job");

		job.setClasses(JobMapper.class, JobReducer.class, null,null);
		job.setMapOutputClasses(Text.class, Text.class);
		job.setReduceJobs(1);

		job.run();
	}

	/**
	 * Loads the computed word cooccurrence count for hashtag #job from
	 * disk.
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	private static String loadJobFeatureVector(String dir)
			throws IOException {
		// Since there'll be only 1 reducer that process the key "#job",
		// result
		// will be saved in the first result file, i.e., part-r-00000
		String job_featureVector = FileUtil.load(dir + "/part-r-00000");

		// The feature vector looks like
		// "#job word1:count1;word2:count2;..."
		String featureVector = job_featureVector.split("\\s+", 2)[1];
		return featureVector;
	}

	/**
	 * Same as getJobFeatureVector, but this one actually computes feature
	 * vector for all hashtags.
	 * 
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	private static void getHashtagFeatureVector(String input, String output)
			throws Exception {
		Optimizedjob job = new Optimizedjob(new Configuration(), input,
				output, "Get feature vector for all hashtags");
		job.setClasses(HashtagMapper.class, HashtagReducer.class, HashtagCombiner.class, null);
		job.setMapOutputClasses(Text.class, Text.class);
		job.run();
	}

	/**
	 * Loads the computed word cooccurrence count for all hashtags from
	 * disk.
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	private static Map<String, String> loadAllFeatureVector(String dir)
			throws IOException {
		Map<String, String> featureVectors = new HashMap<String, String>();
		// Since there'll be only 1 reducer that process the hashtag
		// key,
		// result
		// will be saved in the first result file, i.e., part-r-00000
		InputLines inputLines = FileUtil.loadLines(dir
				+ "/part-r-00000");

		Iterator<String> iterator = inputLines.iterator();
		// The feature vector looks like
		// "#hashtag word1:count1;word2:count2;..."
		while (iterator.hasNext()) {
			String str = iterator.next();
			String hashtag = str.split("\\s+", 2)[0];
			String featureVector = str.split("\\s+", 2)[1];
			featureVectors.put(hashtag, featureVector);
		}
		return featureVectors;
	}

    /**
	 * When we have feature vector for both #job and all other hashtags, we
	 * can use them to compute inner products. The problem is how to share
	 * the feature vector for #job with all the mappers. Here we're using
	 * the "Configuration" as the sharing mechanism, since the configuration
	 * object is dispatched to all mappers at the beginning and used to
	 * setup the mappers.
	 *
	 * @param jobFeatureVector
	 * @param input
	 * @param output
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private static void getHashtagSimilarities(Map<String, String> allFeatureVectors, String input, String output)
    throws IOException, ClassNotFoundException,
    InterruptedException {
		// Share the feature vector of #job to all mappers.
		Configuration conf = new Configuration();
        
		Optimizedjob job = new Optimizedjob(conf, input, output,
                                            "Get similarities between all other hashtags");
		job.setClasses(SimilarityMapper.class, SimilarityReducer.class, SimilarityReducer.class, null);
		job.setMapOutputClasses(Text.class, IntWritable.class);
		job.run();
	}
   
}
