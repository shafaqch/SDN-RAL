# SDN-RAL
Distributed reactive SDN application built as a floodlight module for facilitating multi-agency data access.

The instructions assume Java JDK1.8, Apache Ant, Oracle VirtualBox and Mininet VM available on your machine. Apache Ant can be downloaded from [1] and the installation instructions can be found at [2]. Oracle VirtualBox can be downloaded from [4]. Mininet VM can be downloaded from [3]. The instructions below first install the RAL server on your host machine, then run two Floodlight version 1.0 [5] VMs with the reactive SDN module. The Floodlight VMs have a dependency on Java 7 and the custom module application which needs to know the IP address and Port number of the RAL server. Then the instructions end with running the custom topology on Mininet and specific commands on the Mininet CLI. Installation instructions for Floodlight can be found at [6].


## Instructions:
1)	On the host machine serving as the RAL Server, build and run the RALServer files, which starts the RAL server listening on port 5999. 
2)	Download and run the Floodlight version 1.0 VM https://floodlight.atlassian.net
3)	Install the module (HeaderExtract and RALClient) following the instructions at [7].
a.	Set the RAL Server IP address by defining the following property net.floodlightcontroller.headerextract.HeaderExtract.RALServerIP in the file src/main/resources/floodlightdefault.properties
4)	Run the floodlight controller, it listens for switch Packet In messages at port 6653.
5)	Repeat 2-4 for a second Floodlight VM running the reactive SDN application.
6)	Download Mininet VM 
7)	On Mininet VM, run the python script that generates the mininet topology and connects to the remote controllers. The python script automatically clears the mininet environment, so there is no need to run the sudo mn -c command. The python script expects two arguments, which are the IP addresses of the two remote controllers, FLVM1 and FLVM2. The python script also runs SimpleHTTPServer on host h4. For example, type
sudo python multicontroller.py 192.168.56.103 192.168.56.104
8)	At the Mininet CLI, type h1 wget -O - h4 to see a successful response.
At the Mininet CLI, type sh ovs-ofctl dump-flows s1 to see the flows in s1. You will see the different flow rules installed to allow communication between source and destination.
At the Mininet CLI, type sh ovs-ofctl dump-flows s2 to see the flows in s2. You will see the different flow rules installed to allow communication between source and destination.
9)	At the Mininet CLI, type h2 wget --timeout=1 --tries=1 --retry-connrefused -O - h4 You should see a connection timed out message.
At the Mininet CLI, type sh ovs-ofctl dump-flows s1 to see the flows in s1
At the Mininet CLI, type sh ovs-ofctl dump-flows s2 to see the flows in s2

## References:
[1] Apache Ant Download. https://ant.apache.org/bindownload.cgi

[2] Apache Ant Installation Guide. https://ant.apache.org/manual/install.html

[3] Mininet VM. https://github.com/mininet/mininet/wiki/Mininet-VM-Images

[4] Oracle VM VirtualBox.https://www.virtualbox.org/

[5] R. Izard. Floodlight v1.0. https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343555/Floodlight+v1.0

[6] Floodlight Installation Guide. https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343544/Installation+Guide

[7] R. Izard. How to Write a Module. https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/


