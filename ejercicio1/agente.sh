java -cp lib/jade.jar:classes jade.Boot -container -container-name c1 & sleep .4
java -cp lib/jade.jar:classes jade.Boot -container -container-name c2 & sleep .4

java -cp lib/jade.jar:classes jade.Boot -container -container-name c0 -agents "mol:AgenteInformante(c1, c2, c0)" & sleep 2