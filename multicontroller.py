#!/usr/bin/python

"""

*********** Instructions *************
To run this topology, type: sudo python multicontroller.py
*********** Instructions *************


Two switches connected to two remote controllers using the net.add*() API
and manually starting the switches and controllers.
Each switch has two hosts connected to it.
The switches are not linked.
Each host should run a Quagga instance if each was a different AS (TODO)

      h1
        \
         S1--------C1 (remote)
        / |        |
      h2  |        |
   h3     |        |
     \    |        |
       \  |        |
         S2--------C2 (remote)
        /
      /
    h4



"""

from mininet.net import Mininet
from mininet.node import Controller, OVSSwitch, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info

import sys, subprocess, time



def multiControllerNet(argv):


    "Create a network from semi-scratch with multiple controllers."

    net = Mininet( controller=Controller, switch=OVSSwitch )

    print "*** Creating (reference) controllers"
    c1 = net.addController( 'c1', controller=RemoteController,
		ip=arg1, port=6653)
    c2 = net.addController( 'c2', controller=RemoteController,
		ip=arg2, port=6653)


    print "*** Creating switches"
    s1 = net.addSwitch( 's1' )
#   s1 = net.addSwitch ( 's1', protocols="OpenFlow13" )
    s2 = net.addSwitch( 's2' )
#   s2 = net.addSwitch ( 's2', protocols="OpenFlow13" )

    print "*** Creating hosts"
    hosts1 = [ net.addHost('h1',ip='10.0.0.1')]
    hosts1.append ( net.addHost('h2',ip='10.0.0.2') )
    hosts2 = [ net.addHost('h3',ip='10.0.1.1')]
    hosts2.append ( net.addHost('h4',ip='10.0.1.2') )
    print hosts1
    print hosts2

    time.sleep(1)

    print "*** Creating links"
    for h in hosts1:
        net.addLink( s1, h )
	print "Added link s1 -> " +h.name
    for h in hosts2:
        net.addLink( s2, h )
	print "Added link s2 -> " +h.name

    net.addLink( s1, s2 )
    print "Added link s1 -> s2"


    print "*** Starting network"
    net.build()

    c1.start()
    print "Started controller "+ c1.name
    c2.start()
    print "Started controller "+ c2.name

    s1.start( [ c1 ] )
    print "Started switch "+ s1.name + " with controller " + c1.name
    s2.start( [ c2 ] )
    print "Started switch "+ s2.name + " with controller " + c2.name

    print "*** Testing network"
    net.pingAll()

    #if test1 is specified or no test is specified, run SimpleHTTPServer on h4
    print "*** Starting SimpleHTTPServer on host h4"
    h1 = net.hosts[0]
    h2 = net.hosts[1]
    h3 = net.hosts[2]
    h4 = net.hosts[3]
    print h4.cmd('python -m SimpleHTTPServer 80 &')
    time.sleep(1)

    print "*** Running CLI"
    CLI( net )

    print "*** Stopping network"
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )  # for CLI output

    if (len(sys.argv)) <3:
        print "Usage: multicontroller.py <ip_controller1> <ip_controller2>"
        quit()
    arg1 = sys.argv[1]
    arg2 = sys.argv[2]

    print "*** clear previous mininet topologies ***"

    bashCommand = "sudo mn -c"
    process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()

    multiControllerNet(sys.argv[1:])
