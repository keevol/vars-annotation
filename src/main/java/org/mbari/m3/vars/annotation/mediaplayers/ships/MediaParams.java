package org.mbari.m3.vars.annotation.mediaplayers.ships;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Brian Schlining
 * @since 2017-12-21T10:01:00
 */
public class MediaParams {
    private final Long sequenceNumber;
    private final String cameraId;
    private final String videoSequenceName;
    private final Instant startTimestamp;
    private final String videoName;
    private final URI uri;
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public MediaParams(String cameraId, Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.cameraId = cameraId;

        videoSequenceName = cameraId + " " + sequenceNumber;
        startTimestamp = Instant.now();
        videoName = videoSequenceName + " " +
                df.format(Instant.now().atZone(ZoneId.of("UTC")));
        String uriName = videoSequenceName.replaceAll("\\s+", "_");
        try {
            uri = new URI("urn:rtva:org.mbari:" + uriName);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getVideoSequenceName() {
        return videoSequenceName;
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
    }

    public String getVideoName() {
        return videoName;
    }

    public URI getUri() {
        return uri;
    }

}
