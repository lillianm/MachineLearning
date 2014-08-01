package mapred.hashtagsim_optimize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class HashtagReducer extends Reducer<Text, Text, Text, Text> {

	static int CHUNK = 300000;

	/*
	 * Override Reduce Function
	 */
	@Override
	protected void reduce(Text key, Iterable<Text> value,
		Context context)
	throws IOException, InterruptedException{
		/*
		 * get each word count
		 */
		
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for( Text tag : value ){
			String hashtag = tag.toString();
			Integer count = counts.get(hashtag);
			if (count == null)
				count = 0;
			count++;
			counts.put(hashtag, count);
		}
		/*
		 * word\t hashtag1: count1; hashtag2: count2 ...
		 * #hashtag: freq sequence will be chunked if exceeding predefined size 
		 * all pairs of Chunks will output using a different key 
		 */
		ArrayList<String> buf = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		if(counts.entrySet().size() >1){
			int count = 0;
            int chunkcount = 0;
            int overflow = 0;
			for( Map.Entry<String, Integer> e: counts.entrySet()){
				count++;
				builder.append(e.getKey() + ":" + e.getValue() + ";");

				if(count % CHUNK == 0 && count >= CHUNK ){
                    chunkcount ++;
                    if(chunkcount > 2){
                        overflow = 1;
                        break;
                    }
					buf.add(builder.toString());
					builder = new StringBuilder();	
				}
			}
            if(overflow == 0){
			if(count % CHUNK != 0 && count > CHUNK){
				buf.add(builder.toString());
			}
			if(count < CHUNK){
				context.write(key, new Text(builder.toString()));
			}
			else{
				if(count == CHUNK){
					context.write(key, new Text(buf.get(0)));
				}
				else{
					int index = 0;
					for(int i=0;i < buf.size()-1;i++){
						for(int j = i+1;j< buf.size();j++){
							context.write(new Text(key), new Text(buf.get(i)+buf.get(j)));
							index++;
						}
					}	
				}
			}	
		}
        }
	}
}
