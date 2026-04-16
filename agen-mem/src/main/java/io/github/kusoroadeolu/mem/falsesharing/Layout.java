import io.github.kusoroadeolu.mem.falsesharing.PaddedCounter;
import org.openjdk.jol.info.ClassLayout;

void main(){
    PaddedCounter counter = new PaddedCounter();
    System.out.println(ClassLayout.parseInstance(counter).toPrintable());
}

/*
*
* Benchmark                      (impl)   Mode  Cnt         Score         Error  Units
FalseSharingBench.q           COUNTER  thrpt   21  29831274.577 ± 6431467.958  ops/s
FalseSharingBench.q:a         COUNTER  thrpt   21  15738622.692 ± 4727742.728  ops/s
FalseSharingBench.q:b         COUNTER  thrpt   21  14092651.884 ± 2193883.084  ops/s
FalseSharingBench.q    PADDED_COUNTER  thrpt   21  43825153.846 ± 7025444.558  ops/s
FalseSharingBench.q:a  PADDED_COUNTER  thrpt   21  25560916.838 ± 3509834.789  ops/s
FalseSharingBench.q:b  PADDED_COUNTER  thrpt   21  18264237.009 ± 4296462.745  ops/s
* */