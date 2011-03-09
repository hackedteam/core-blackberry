package tests.unit;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.record.AudioRecorder;
import blackberry.utils.Utils;

public class UT_Recorder extends TestUnit {

    public UT_Recorder(String name, Tests tests) {
        super(name, tests);
    }

    public boolean run() throws AssertException {
        ChunksHeader();
        ChunksAvailable();

        return true;
    }

    private void ChunksAvailable() throws AssertException {
        //#ifdef DEBUG
        debug.trace("ChunksAvailable");
        //#endif
        final AudioRecorder recorder = new AudioRecorder();

        //#ifdef DEBUG
        debug.info("ChunksAvailable");
        //#endif
        recorder.start();

        for (int i = 1; i <= 10; i++) {
            Utils.sleep(3000);
            //#ifdef DEBUG
            debug.info("getchunk " + i);
            //#endif
            final byte[] chunk = recorder.getAvailable();
            AssertNotNull(chunk, "Null chunk " + i);
            AssertThat(chunk.length > 0, "wrong len chunk " + i);
        }
        recorder.stop();

    }

    private void ChunksHeader() throws AssertException {
        
        //#ifdef DEBUG
        debug.info("-- Recorder Chunks --");
        //#endif

        final byte[] header = new byte[] { 35, 33, 65, 77, 82, 10 };
        final int chunksize = 160;

        final AudioRecorder recorder = new AudioRecorder();

        //#ifdef DEBUG
        debug.info("start");
        //#endif
        recorder.start();
        Utils.sleep(1000);
        //#ifdef DEBUG
        debug.info("getchunk 1");
        //#endif
        byte[] chunk = recorder.getChunk(chunksize);
        AssertNotNull(chunk, "Null chunk 1");
        AssertThat(chunk.length == chunksize, "wrong len chunk 1");
        boolean hasHeader = Utils.equals(chunk, 0, header, 0, header.length);

        AssertThat(hasHeader, "no header");

        Utils.sleep(1000);
        //#ifdef DEBUG
        debug.info("getchunk 2");
        //#endif
        chunk = recorder.getChunk(chunksize);
        AssertNotNull(chunk, "Null chunk 2");
        AssertThat(chunk.length == chunksize, "wrong len chunk 2");
        hasHeader = Utils.equals(chunk, 0, header, 0, header.length);

        AssertThat(!hasHeader, "header");

        //#ifdef DEBUG
        debug.info("stop");
        //#endif
        recorder.stop();
        AssertNotNull(recorder, "Null recorder");
    }

}
