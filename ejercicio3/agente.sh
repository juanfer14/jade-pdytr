MY_ID=$!

java -cp lib/jade.jar:classes jade.Boot -container -container-name c1 & sleep .5
java -cp lib/jade.jar:classes jade.Boot -container -container-name c0 -agents "mol:AgenteFileSystem(c1, write, prac4.pdf, 300)" & sleep .5

kill $MY_ID

