import io.github.kusoradeolu.agen.expr.queues.SPSCQueue;
import org.openjdk.jol.info.ClassLayout;

void main(){
    SPSCQueue<?> queue = new SPSCQueue<>(5);
    System.out.println(ClassLayout.parseInstance(queue).toPrintable());
}