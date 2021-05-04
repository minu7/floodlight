"""Custom topology example

Two directly connected switches plus a host for each switch:

   host --- switch --- switch --- host

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=mytopo' from the command line.
"""

from mininet.topo import Topo

class MyTopo( Topo ):
    "Simple topology example."

    def build( self ):
        "Create custom topo."

        # Add hosts and switches
        c1 = self.addHost( 'c1' )
        c2 = self.addHost( 'c2' )
        r1 = self.addSwitch( 'r1' )
        r2 = self.addSwitch( 'r2' )
	r3 = self.addSwitch( 'r3' )
        r4 = self.addSwitch( 'r4' )
	r5 = self.addSwitch( 'r5' )
	s1 = self.addHost('s1')
	s2 = self.addHost('s2')

        # Add links
        self.addLink( s1, r3 )
        self.addLink( r3, r1 )
        self.addLink( r3, r4 )
	self.addLink( r1, c1 )
	self.addLink( r1, r4 )
	self.addLink( r4, r2 )
	self.addLink( r4, r5 )
	self.addLink( r2, c2 )
	self.addLink( r2, r5 )
	self.addLink( r5, s2 )

topos = { 'mytopo': ( lambda: MyTopo() ) }
