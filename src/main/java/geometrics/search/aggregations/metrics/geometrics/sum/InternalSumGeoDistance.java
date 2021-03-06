package geometrics.search.aggregations.metrics.geometrics.sum;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.format.ValueFormatter;
import org.elasticsearch.search.aggregations.support.format.ValueFormatterStreams;

public class InternalSumGeoDistance extends InternalNumericMetricsAggregation.SingleValue implements Sum {
	
	public final static Type TYPE = new Type("sum-geo-distance");
	
	private double sum;
	
	InternalSumGeoDistance() {} // for serialization

	public InternalSumGeoDistance(String name, double sum, ValueFormatter formatter, List<PipelineAggregator> pipelineAggregators,
			Map<String, Object> metaData) {
		super(name, pipelineAggregators, metaData);
		this.sum = sum;
		this.valueFormatter = formatter;
	}
	
	public double value() {
		return getValue();
	}

	public double getValue() {
		return sum;
	}

	@Override
	public Type type() {
		return TYPE;
	}

	@Override
	public InternalAggregation doReduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
		double sum = 0;
		for (InternalAggregation aggregation : aggregations) {
			sum += ((InternalSumGeoDistance) aggregation).sum;
		}
		return new InternalSumGeoDistance(getName(), sum, valueFormatter, pipelineAggregators(), getMetaData());
	}

	@Override
	public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
		builder.field(CommonFields.VALUE, sum);
		if (!(valueFormatter instanceof ValueFormatter.Raw)) {
			builder.field(CommonFields.VALUE_AS_STRING, valueFormatter.format(getValue()));
		}
		return builder;
	}

	@Override
	protected void doWriteTo(StreamOutput out) throws IOException {
		ValueFormatterStreams.writeOptional(valueFormatter, out);
		out.writeDouble(sum);
	}

	@Override
	protected void doReadFrom(StreamInput in) throws IOException {
		valueFormatter = ValueFormatterStreams.readOptional(in);
		sum = in.readDouble();
	}

}
