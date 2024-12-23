Compilar ficheiros java

javac ds/assign/trg/*.java


Inicializar Servidor

java ds.assign.trg.CalculatorMultiServer localhost


Inicializar Peers

java ds.assign.trg.Peer localhost 11111 TOKEN localhost 22222 localhost

java ds.assign.trg.Peer localhost 22222 NONE localhost 33333 localhost

java ds.assign.trg.Peer localhost 33333 NONE localhost 40000 localhost

java ds.assign.trg.Peer localhost 40000 NONE localhost 55555 localhost

java ds.assign.trg.Peer localhost 55555 NONE localhost 11111 localhost
