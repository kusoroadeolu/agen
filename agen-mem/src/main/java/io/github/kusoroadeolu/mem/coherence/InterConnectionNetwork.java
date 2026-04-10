package io.github.kusoroadeolu.mem.coherence;

public class InterConnectionNetwork {
    private final Bus<AckMessage> bus;

    private InterConnectionNetwork(){
        this.bus = new Bus<>();
    }

    public static InterConnectionNetwork getInstance(){
        return InterConnHolder.NETWORK;
    }

    public void poll(){
        bus.poll();
    }

    public AckMessage peek(){
        return bus.peek();
    }

    public boolean offer(AckMessage ackMessage){
        return bus.offer(ackMessage);
    }

    public Object readFromMainMemory(){
        return MultiProcessorChip.chip().memory();
    }




    private static class InterConnHolder{
        private final static InterConnectionNetwork NETWORK = new InterConnectionNetwork();
    }
}
