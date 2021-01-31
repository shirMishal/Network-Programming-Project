package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGS.BgsData;
import bgu.spl.net.impl.BGS.BidiProtocolImpl;
import bgu.spl.net.impl.BGS.CommandEncoderDecoder;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {

        BgsData dataLogic = new BgsData(); //one shared object

        Server.threadPerClient(
                Integer.parseInt(args[0]), //port
                () -> new BidiProtocolImpl(dataLogic), //protocol factory
                CommandEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}