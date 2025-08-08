package europeana.rnd.sparql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

/**
 * Represents the status of an Europeana dataset in the FTP server and in
 * Virtuoso
 *
 */
public class Dataset {

    /**
     * Dataset state indicating what the update should do with it
     */
    public enum State {
        UP_TO_DATE, OUTDATED, CORRUPT, MISSING, TO_REMOVE
    }

    private static final Logger LOG = LogManager.getLogger(Dataset.class);

    String id;
    Instant timestampFtp;
    Instant timestampSparql;
    State state;

    /**
     * Create a new data set
     * @param id of the dataset
     */
    public Dataset(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestampFtp() {
        return timestampFtp;
    }

    public void setTimestampFtp(Instant instant) {
        this.timestampFtp = instant;
    }

    public Instant getTimestampSparql() {
        return timestampSparql;
    }

    public void setTimestampSparql(Instant timestampSparql) {
        this.timestampSparql = timestampSparql;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Dataset dataset = (Dataset) obj;
        return id.equals(dataset.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Update the state of this dataset by comparing it to the same dataset known in sparql
     * @param dsAtSparql same dataset found in SPARQL
     */
    public void updateState(Dataset dsAtSparql) {
        if (dsAtSparql == null) {
            state = State.MISSING;
        } else {
            setTimestampSparql(dsAtSparql.getTimestampSparql());
            if (timestampSparql == null || dsAtSparql.getState() == State.CORRUPT)
                state = State.CORRUPT;
            else if (timestampFtp.isAfter(timestampSparql))
                state = State.OUTDATED;
            else
                state = State.UP_TO_DATE;
            LOG.trace("Dataset {}: timestamp = {}, state = {}", dsAtSparql.getId(), dsAtSparql.getTimestampSparql(), dsAtSparql.getState());
        }
    }

    public boolean isCorruptAtSparql() {
        return timestampSparql == null;
    }

    public boolean isOutdatedAtSparql() {
        return false;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String toString() {
        return getId();
    }

}
