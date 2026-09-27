import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Mapper: Doc tung dong CSV, trich xuat (year, tmax).
 *
 * Input CSV format:
 *   year,month,day,tmax
 *   Index: 0     1    2    3
 *
 * Output: key = year (Text), value = tmax (DoubleWritable)
 */
public class MaxTemperatureMapper
        extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim();

        // Bo qua header
        if (line.startsWith("year,") || line.isEmpty()) {
            return;
        }

        String[] fields = line.split(",", -1);

        // Can it nhat 4 cot (index 0 = year, index 3 = tmax)
        if (fields.length < 4) {
            return;
        }

        String year = fields[0].trim();
        String tmaxStr = fields[3].trim();

        // Bo qua dong khong co gia tri tmax
        if (tmaxStr.isEmpty()) {
            return;
        }

        try {
            double tmax = Double.parseDouble(tmaxStr);
            context.write(new Text(year), new DoubleWritable(tmax));
        } catch (NumberFormatException e) {
            // Bo qua dong loi
        }
    }
}
