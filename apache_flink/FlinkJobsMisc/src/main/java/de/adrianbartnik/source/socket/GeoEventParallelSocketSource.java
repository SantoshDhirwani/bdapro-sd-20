package de.adrianbartnik.source.socket;

import de.adrianbartnik.job.data.GeoEvent;
import de.adrianbartnik.source.AbstractSource;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.StreamSource;

import java.io.Serializable;
import java.util.List;

public class GeoEventParallelSocketSource extends AbstractSource<GeoEvent> implements Serializable {

    private static final String OPERATOR_NAME = "GeoEventParallelSocketSource";

    private final List<String> hostnames;
    private final List<Integer> ports;

    public GeoEventParallelSocketSource(List<String> hostnames, List<Integer> ports, int parallelism) {
        super(parallelism);
        this.hostnames = hostnames;
        this.ports = ports;
    }

    @Override
    public DataStream<GeoEvent> createSource(String[] arguments, StreamExecutionEnvironment executionEnvironment) {

        TypeInformation<GeoEvent> typeInformation = TypeInformation.of(new TypeHint<GeoEvent>() {});

        PersonSocketSourceFunction function = new PersonSocketSourceFunction(hostnames, ports);

        return new DataStreamSource<>(executionEnvironment,
                typeInformation,
                new StreamSource<>(function),
                true,
                OPERATOR_NAME)
                .setParallelism(parallelism);
    }

    public class PersonSocketSourceFunction extends AbstractSocketSourceFunction<GeoEvent> {

        PersonSocketSourceFunction(List<String> hostnames, List<Integer> ports) {
            super(hostnames, ports);
        }

        @Override
        protected GeoEvent stringToRecord(String record) {
            String[] split = record.split(",");
            return new GeoEvent(
                    Long.valueOf(split[0]),
                    Long.valueOf(split[1]),
                    split[3]);
        }

        @Override
        protected String getStartCommand() {
            return getRuntimeContext().getIndexOfThisSubtask() + ":persons\n";
        }
    }
}
