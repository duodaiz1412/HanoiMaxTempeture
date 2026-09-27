import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reducer: Voi moi nam (key), tim gia tri tmax lon nhat.
 *
 * Input:  key = year (Text), values = danh sach tmax (DoubleWritable)
 * Output: key = year (Text), value = max(tmax) (DoubleWritable)
 */
public class MaxTemperatureReducer
        extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {

        double maxTemp = -999999;

        for (DoubleWritable val : values) {
            if (val.get() > maxTemp) {
                maxTemp = val.get();
            }
        }

        context.write(key, new DoubleWritable(maxTemp));
    }
}
