Compilar ficheiros java

javac ds/assign/trg/*.java


Inicializar Servidor

java ds.assign.trg.CalculatorMultiServer localhost


Inicializar Peers

java ds.assign.trg.Peer localhost 10000 localhost 20000 localhost 44444

java ds.assign.trg.Peer localhost 20000 localhost 30000 localhost 44444

java ds.assign.trg.Peer localhost 30000 localhost 40000 localhost 44444

java ds.assign.trg.Peer localhost 40000 localhost 50000 localhost 44444

java ds.assign.trg.Peer localhost 50000 localhost 10000 localhost 44444

Injetar o Token
java ds.assign.ring.Token localhost 10000 