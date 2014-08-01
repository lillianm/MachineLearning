package mapred.hashtagsim_optimize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableComparable;
public class SimilarityGroupComparator extends WritableComparator{
	public SimilarityGroupComparator(){
		super(Text.class, true);
	}

	@Override
	public int compare(WritableComparable key1, WritableComparable key2){
		
		String newKey1 = key1.toString();
		String newKey2 = key2.toString();
		String[] key1s = newKey1.split("\\t");
		String[] key2s = key2.toString().split("\\t");
		if(key1s[0].hashCode() > key1s[1].hashCode()) {
			newKey1 = key1s[1] + "\t" + key1s[0];
		}
		if(key2s[0].hashCode() > key2s[1].hashCode()) {
			newKey2 = key2s[1] + "\t" + key2s[0];
		}

		//int hashcode1 = key1.toString().split("\\t")[0].hashCode() + key1.toString().split("\\t")[1].hashCode();
		//int hashcode2 = key2.toString().split("\\t")[0].hashCode() + key2.toString().split("\\t")[1].hashCode();
		return newKey1.compareTo(newKey2);
	}
}