sudo mn --custom mininet-topology/custom.py --topo mytopo \
 --mac --switch ovsk --controller remote,ip=127.0.0.1,port=6653,protocols=OpenFlow13 --ipbase=10.0.0.0

