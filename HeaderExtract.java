package net.floodlightcontroller.headerextract;
//import java.io.IOException;
//import java.net.UnknownHostException;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
import java.util.Map;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
//import org.projectfloodlight.openflow.protocol.OFMatch;
import org.projectfloodlight.openflow.protocol.OFMessage;
//import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
//import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
//import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
//import org.projectfloodlight.openflow.protocol.action.OFActionStripVlan;
import org.projectfloodlight.openflow.protocol.action.OFActions;
//import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
//import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
//import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
//import org.projectfloodlight.openflow.protocol.match.Match.Builder;
import org.projectfloodlight.openflow.protocol.match.MatchField;
//import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
//import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
//import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
//import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
//import org.projectfloodlight.openflow.types.VlanVid;
//import org.projectfloodlight.openflow.util.HexString;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
//import net.floodlightcontroller.packet.ARP;
//import net.floodlightcontroller.packet.BasePacket;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
//import net.floodlightcontroller.packet.UDP;
//import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

/**
 * Reactive module to grant access to data in another agency's network registered in the Registered Agency List (RAL)
 *
 * @author S Chaudhry

 */


public class HeaderExtract implements IFloodlightModule, IOFMessageListener {

	public final int DEFAULT_CACHE_SIZE = 10;
	protected IFloodlightProviderService floodlightProvider;
	//private IStaticFlowEntryPusherService flowPusher;
	private static String RALServerIP;
	private static int  RALServerPort;

	@Override
	public String getName() {
		return "CrossAgencyTalker";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
		public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
		////return false;
	}

	//This is where we pull fields from the packet-in
	/**
	  * The controller will invoke the receive()
	  * function automatically when an OFMessage is
	  * received from a switch if the module
	  * implements the IOFMessageListener interface
	  * and is registered with the controller as an
	  * IOFMessageListener
	  * https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343547/How+to+use+OpenFlowJ-Loxigen
	  **/
	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

	   switch (msg.getType()) {
	      case PACKET_IN:
	    	  /* Retrieve the de-serialized packet in message */
			  Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		     /* Various getters and setters are exposed in Ethernet */
		     //MacAddress srcMac = eth.getSourceMACAddress();
		     //VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());

		     /*
		      * Check the ethertype of the Ethernet frame and retrieve the appropriate payload.
		      * Note the shallow equality check. EthType caches and reuses instances for valid types.
		      */
			  if (eth.getEtherType() == EthType.IPv4)
			  {
				  /* We got an IPv4 packet; get the payload from Ethernet */
				  IPv4 ipv4 = (IPv4) eth.getPayload();

				  /* Various getters and setters are exposed in IPv4 */
				  //byte[] ipOptions = ipv4.getOptions();
				  IPv4Address dstIp = ipv4.getDestinationAddress();

		         //Check IP protocol
		         if (ipv4.getProtocol() == IpProtocol.TCP)
		         {
		             /* We got a TCP packet; get the payload from IPv4 */
		             TCP tcp = (TCP) ipv4.getPayload();

		             /* Various getters and setters are exposed in TCP */
		             TransportPort srcPort = tcp.getSourcePort();
		             TransportPort dstPort = tcp.getDestinationPort();

			         //short flags = tcp.getFlags();

		             System.out.println("$$$$$-Get the Source IP -$$$$$ " + ipv4.getSourceAddress().toString());
		             System.out.println("$$$$$-Get the Destination IP -$$$$$ "+dstIp.toString());
		             System.out.println("$$$$$-Get the TCP Source Port-$$$$$ "+srcPort.toString());
		             System.out.println("$$$$$-Get the TCP Destination Port-$$$$$ "+ dstPort.toString());

		             //assume all TCP traffic is blocked
		             //if destination ip address is part of RAL, then forward as normal
		             //else block
		             //example:- h4 is listening on TCP and host h3 is listening on TCP but h3 is not in RAL,
		             //so, wget request h1->h4 will be forwarded while wget request h2->h3 will be blocked

		     		RALClient2 client = null;
		     		String destIpAddress = ipv4.getDestinationAddress().toString();
		     		String srcIpAddress = ipv4.getSourceAddress().toString();

		       		//client = new RALClient2("192.168.1.183",5999,destIpAddress);
		     		client = new RALClient2(RALServerIP,RALServerPort,destIpAddress);
		       		String clientResponse = null;
		       		clientResponse = client.getQueryResponse();
		       		boolean isDstRegistered = false;
		       		if(clientResponse.equals("Unregistered"))
		       			System.out.println("too bad, the " + destIpAddress + " is not registered");
		       		else //(clientResponse != null)
		       		{
		       			System.out.println("great, the host " +destIpAddress+ " is registered");
		       			isDstRegistered = true;
		       		}
		       		client.bye();

		       		//client = new RALClient2("192.168.1.183",5999,srcIpAddress);
		       		client = new RALClient2(RALServerIP,RALServerPort,srcIpAddress);

		       		clientResponse = client.getQueryResponse();
		       		boolean isSrcRegistered = false;
		       		if(clientResponse.equals("Unregistered"))
		       			System.out.println("too bad, the " + srcIpAddress + " is not registered");
		       		else //(clientResponse != null)
		       		{
		       			System.out.println("great, the host " + srcIpAddress + " is registered");
		       			isSrcRegistered = true;
		       		}

		       		//if( ipv4.getDestinationAddress().toString().equals("10.0.1.2") &&
		            	//	 ipv4.getSourceAddress().toString().equals("10.0.0.1"))
		            if(isDstRegistered && isSrcRegistered)
		       		{
		            	 System.out.println("$$$$$-ALLOW-$$$$$ ");
		            	 OFFactory myOF10Factory = sw.getOFFactory();
		            	 OFVersion detectedVersion = myOF10Factory.getVersion();

		            	 switch (detectedVersion.toString())
		            	 {
	            	 		case "OF_10":
	            	 			System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString());

	            	 			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
	            	 			OFActions actions = myOF10Factory.actions();


				            	OFActionOutput output = actions.buildOutput()
				            	     //.setMaxLen(0xFFffFFff)
				            	    .setPort(OFPort.NORMAL)//process as L2/L3 learning switch
				            	    .build();
				            	actionList.add(output);
					            System.out.println("$$$$$-actionsList built-$$$$$ ");

				            	/* RULE 1 */
				            	//create a match object
								Match myMatch = myOF10Factory.buildMatch()
									 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
									 .setExact(MatchField.IPV4_SRC,IPv4Address.of(srcIpAddress))
									 .setExact(MatchField.IPV4_DST,IPv4Address.of(destIpAddress))
									 .setExact(MatchField.IP_PROTO, IpProtocol.TCP)
									 .setExact(MatchField.TCP_DST, dstPort)
									 .build();
								System.out.println("$$$$$-match object built-$$$$$ ");

								//compose an OFFlowAdd to allow traffic
								OFFlowAdd flowAdd = myOF10Factory.buildFlowAdd()
								 	.setMatch(myMatch)
								    .setBufferId(OFBufferId.NO_BUFFER)
								    .setHardTimeout(3600)
								    .setIdleTimeout(60)
								    .setPriority(32768)//mid priority
								    .setActions(actionList)
								    .build();
								 System.out.println("$$$$$-Rule 1 composed-$$$$$ ");
								 sw.write(flowAdd);
								 System.out.println("$$$$$-Rule 1 sent to switch -$$$$$ ");

								 /* RULE 2 */
				            	 //create a match object
								 Match myMatch2 = myOF10Factory.buildMatch()
									 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
									 .setExact(MatchField.IPV4_SRC,IPv4Address.of(destIpAddress))
									 .setExact(MatchField.IPV4_DST,IPv4Address.of(srcIpAddress))
									 .setExact(MatchField.IP_PROTO, IpProtocol.TCP)
									 .setExact(MatchField.TCP_SRC, dstPort)
									 .build();
								 System.out.println("$$$$$-match object built-$$$$$ ");

								 //compose an OFFlowAdd to allow traffic
								 OFFlowAdd flowAdd2 = myOF10Factory.buildFlowAdd()
								 	.setMatch(myMatch2)
								    .setBufferId(OFBufferId.NO_BUFFER)
								    .setHardTimeout(3600)
								    .setIdleTimeout(60)
								    .setPriority(32768)//mid priority
								    .setActions(actionList)
								    .build();
								 System.out.println("$$$$$-Rule 2 composed-$$$$$ ");
								 sw.write(flowAdd2);
								 System.out.println("$$$$$-Rule 2 sent to switch -$$$$$ ");

								 /* Rule 3 */
								 Match myMatch3 = myOF10Factory.buildMatch()
									 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
									 .setExact(MatchField.IP_PROTO, IpProtocol.TCP)//match all TCP flows
									 .build();
								 System.out.println("$$$$$-match object built-$$$$$ ");
								 OFFlowAdd flowAdd3 = myOF10Factory.buildFlowAdd()
								 	.setMatch(myMatch3)
								    .setBufferId(OFBufferId.NO_BUFFER)
								    .setHardTimeout(3600)
								    .setIdleTimeout(60)
								    .setPriority(2)//low priority
								    //.setActions(actionList) //commenting this out results in default action of drop
								    .build();
								 System.out.println("$$$$$-Rule 3 composed-$$$$$ ");
								 sw.write(flowAdd3);
								 System.out.println("$$$$$-Rule 3 sent to switch -$$$$$ ");

				            	 break;
		            	 	case "OF_13":
		            	 		//TODO support OF 1.3 later
		            	 		System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString() + " is currently not supported");
		            	 		break;
		            	 	default:
		            	 		System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString() + " is currently not supported");
		            	 		break;
		            	}

		       		}
			        else
			        {
			            //add flow rule in switch 1 to deny traffic
			        	System.out.println("$$$$$-DENY-$$$$$ ");

			            //OF 1.0
			            OFFactory myOF10Factory = sw.getOFFactory();

			            OFVersion detectedVersion = myOF10Factory.getVersion();
			            //System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString());
			            switch (detectedVersion.toString())
			            {
			            	case "OF_10":
			             		System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString());

					          	//create a match object
								Match myMatch = myOF10Factory.buildMatch()
									 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
									 .setExact(MatchField.IPV4_SRC,IPv4Address.of(srcIpAddress))
									 .setExact(MatchField.IPV4_DST,IPv4Address.of(destIpAddress))
									 .setExact(MatchField.IP_PROTO, IpProtocol.TCP)
									 .setExact(MatchField.TCP_DST, dstPort)
									 .build();
								System.out.println("$$$$$-match object built-$$$$$ ");

								//compose an OFFlowAdd message to drop TCP traffic
								OFFlowAdd flowAdd = myOF10Factory.buildFlowAdd()
								 	.setMatch(myMatch)
								    .setBufferId(OFBufferId.NO_BUFFER)
								    .setHardTimeout(3600)
								    .setIdleTimeout(60)//seconds
								    .setPriority(2)//
								    .setMatch(myMatch)
								    //.setActions(actionList)//not setting this value results in actions=drop
								    .build();
								 System.out.println("$$$$$-Rule 4 composed-$$$$$ ");
								 sw.write(flowAdd);
								 System.out.println("$$$$$-Rule 4 sent to switch -$$$$$ ");

				            	 break;
		            	 	case "OF_13":
		            	 		//TODO support OF 1.3
		            	 		System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString() + " is currently not supported");
		            	 		break;
		            	 	default:
		            	 		System.out.println("$$$$$-OFFactory Version-$$$$$ " + detectedVersion.toString() + " is currently not supported");
		            	 		break;
			            }//END SWITCH

			         }//end else for source/destination not registered

		         } // end of IpProtocol.TCP
		         else if (ipv4.getProtocol() == IpProtocol.UDP) {
		             /* We got a UDP packet; get the payload from IPv4 */
		             //UDP udp = (UDP) ipv4.getPayload();

		             /* Various getters and setters are exposed in UDP */
		             //TransportPort srcPort = udp.getSourcePort();
		             //TransportPort dstPort = udp.getDestinationPort();

		             /* Your logic here! */
		         }
		     } //end of EthType.IPv4
		     else if (eth.getEtherType() == EthType.ARP) {
		         /* We got an ARP packet; get the payload from Ethernet */
		         //ARP arp = (ARP) eth.getPayload();

		         /* Various getters and setters are exposed in ARP */
		         //boolean gratuitous = arp.isGratuitous();

		     }
		     else {
		         /* Unhandled ethertype */
		     }

		     break; // end of  case PACKET_IN:

	     default:
	    	 break;
	   }
	   return Command.CONTINUE;//this allows this packet to continue to be handled by other PACKET_In handlers
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);


		// read our config options
		Map<String, String> configOptions = context.getConfigParams(this);
		RALServerIP = configOptions.get("RALServerIP");
		RALServerPort = Integer.parseInt(configOptions.get("RALServerPort"));

		}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}
}
