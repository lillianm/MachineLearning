package mapred.hashtagsim_optimize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;

public class SimilarityMapper extends
		Mapper<LongWritable, Text, Text, IntWritable> {

	/**
	 * input:  word: #hashtag1, count1; #hashtag2, count2 .. 
	 * output: (#hashtag1, #hashtag2), count1*count2; (#hashtag1, #hashtag3), count1*count3; 
	 */
	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		String hashtagVector = line.split("\\s+",2)[1];

		ArrayList<String> hashTags = new ArrayList<String>();
		ArrayList<Integer> tagCounts = new ArrayList<Integer>();
		parseHashtagVector(hashtagVector,hashTags, tagCounts);

		int tagNum = hashTags.size();
		for(int i = 0;i<tagNum-1;i++){
			String hashtag1 = hashTags.get(i);
			int count1 = tagCounts.get(i);
			int hashcode = hashtag1.hashCode();
			for(int j = i+1;j < tagNum; j++){
				String hashtag2 = hashTags.get(j);
				int count2 = tagCounts.get(j);
				
				if (hashcode > hashtag2.hashCode()){
					context.write(new Text(hashtag2 + "\t" + hashtag1), new IntWritable(count1 * count2));
				}
				else{
					context.write(new Text(hashtag1 + "\t" + hashtag2), new IntWritable(count1 * count2));
				}
			}
		}
		hashTags = null;
		tagCounts = null;
	}
	/*
	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		String[] hashtag_featureVector = line.split("\\s+", 2);

		String hashtag1 = hashtag_featureVector[0];
                
		Map<String, Integer> features = parseFeatureVector(hashtag_featureVector[1]);

        for (String hashtag2 : jobFeatures.keySet()) {
            if (context.getConfiguration().getBoolean(hashtag1 + hashtag2, false) || context.getConfiguration().getBoolean(hashtag2 + hashtag1, false) || hashtag1.equals(hashtag2)) {
                continue;
            }
            context.getConfiguration().setBoolean(hashtag1 + hashtag2, true);
            Integer similarity = computeInnerProduct(jobFeatures.get(hashtag2), features);
            if (similarity != 0) {
                context.write(new IntWritable(similarity), new Text(hashtag1 + "\t" + hashtag2));
            }
        }
	}
	*/
	/**
	 * This function is ran before the mapper actually starts processing the
	 * records, so we can use it to setup the job feature vector.
	 * 
	 * Loads the feature vector for hashtag #job into mapper's memory
	 */
	/*@Override
	protected void setup(Context context) {
        jobFeatures = new HashMap<String, Map<String, Integer>> ();
        int count = context.getConfiguration().getInt("count", 0);
        for (int i = 0; i < count; i++) {
            String hashtag = context.getConfiguration().getStrings(i + "")[0];
            String feature = context.getConfiguration().getStrings(i + "")[1];
            Map<String, Integer> jobFeatureVector = parseFeatureVector(feature);
            jobFeatures.put(hashtag, jobFeatureVector);
        }
	}*/

	/**
	 * De-serialize the feature vector into a map
	 * 
	 * @param featureVector
	 *                The format is
	 *                "word1:count1;word2:count2;...;wordN:countN;"
	 * @return A HashMap, with key being each word and value being the
	 *         count.
	 */
	/*
	private Map<String, Integer> parseHashtagVector(String featureVector) {
		Map<String, Integer> featureMap = new HashMap<String, Integer>();
		String[] features = featureVector.split(";");
		for (String feature : features) {
			String[] word_count = feature.split(":");
			featureMap.put(word_count[0],
					Integer.parseInt(word_count[1]));
		}
		return featureMap;
	}*/

	private void parseHashtagVector(String featureVector, ArrayList<String> hashtags, ArrayList<Integer> counts){
		String[] features = featureVector.split(";");
		for(int i=0;i<features.length;i++){
			String[] word_count = features[i].split(":");
			hashtags.add(word_count[0]);
			counts.add(Integer.parseInt(word_count[1]));
		}
	}


	/*
	 * Add a pair of hashtag to the visited set to ensure that one pair is counted only once
	 */
	/*private void addHashtagPair(String tag1, String tag2, Map<String, ArrayList<String>> visited){
		addHashTag(tag1, tag2, visited);
		addHashTag(tag2, tag1, visited);
			
	}

	private void addHashTag(String key, String value, Map<String, ArrayList<String>> visited){
		ArrayList<String> hashtagSet;
			if(visited.get(key) == null)
				hashtagSet = new ArrayList<String>();
			else
				hashtagSet = visited.get(key);
			hashtagSet.add(value);
			visited.put(key, hashtagSet);
	}
*/

}
