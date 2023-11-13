> ./log/out.txt
java -cp lib/jade.jar:classes jade.Boot -gui & sleep .5
java -cp lib/jade.jar:classes jade.Boot -container -container-name c1 -host localhost >> ./log/out.txt  & sleep .5
java -cp lib/jade.jar:classes jade.Boot -container -container-name c2 -host localhost >> ./log/out.txt  & sleep .5
java -cp lib/jade.jar:classes jade.Boot -container -container-name c3 -host localhost >> ./log/out.txt  & sleep .5
java -cp lib/jade.jar:classes jade.Boot -container -container-name c0 -host localhost -agents "mol:AgenteInformante(c1, c2, c3)" >> ./log/out.txt & sleep .5

wait
pkill -9 -f "java"
