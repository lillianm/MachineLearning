package mapred.hashtagsim_optimize;

import java.io.IOException;

import mapred.util.Tokenizer;
import mapred.util.Stopwords;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class HashtagMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static Stopwords stopwords = new Stopwords();
	/*@Override
	protected void map(LongWritable key, Text value,
			Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		String[] words = Tokenizer.tokenize(line);*/

		/*
		 * Iterate all words, find out all hashtags, then iterate all other non-hashtag 
		 * words and map out.
		 */
		/*
		for (String word : words) 
			if (word.startsWith("#")) 
				for (String word2 : words)
					if (word2.startsWith("#")==false)
						context.write(new Text(word), new Text(word2));
		
		*/

		/*
		 * change the mapper <key, value> from <#hashtag, word>, to <word, #hashtag>
		 */
	@Override
	protected void map(LongWritable key, Text value,
			Context context)
		throws IOException, InterruptedException {
			String line = value.toString();
			String words[] = Tokenizer.tokenize(line);

			for(String word : words){
				if (word.startsWith("#")){
					for(String word2 : words){
						// if( word2.startsWith("#") == false)
						if( word2.startsWith("#") == false)
							context.write( new Text(word2), new Text(word));
					}
				}
			}
		}

	/**
	 * This function is ran before the mapper actually starts processing the
	 * records.
	 */
	// @Override
	// protected void setup(Context context) {
 //        Stopwords stopwords = new Stopwords();
	// }
}


