package bgu.spl.net.impl.BGSServer;
import bgu.spl.net.impl.BGS.BgsData;
import bgu.spl.net.impl.BGS.BidiProtocolImpl;
import bgu.spl.net.impl.BGS.CommandEncoderDecoder;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        BgsData data = new BgsData(); //one shared object

        Server.reactor(
                Integer.parseInt(args[1]),  //num of threads
                Integer.parseInt(args[0]), //port
                () -> new BidiProtocolImpl(data), //protocol factory
                CommandEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}