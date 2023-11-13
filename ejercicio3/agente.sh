# VACIO EL ARCHIVO CON LOS PRINTS
> ./log/out.txt
# EJECUTO LA INTERFAZ PARA MANEJAR AGENTE
java -cp lib/jade.jar:classes jade.Boot -gui & sleep .5
# INSTANCIO EL CONTAINER DEL FS
java -cp lib/jade.jar:classes jade.Boot -container -container-name c1 >> ./log/out.txt & sleep .5
# INSTANCIO EL CONTAINER LOCAL
#java -cp lib/jade.jar:classes jade.Boot -container -container-name c0 -agents "mol:AgenteFileSystem(c1, write, prac4.pdf, 247567)" >> ./log/out.txt &
# CONTENEDOR CON AGENTE PARA EL READ.
java -cp lib/jade.jar:classes jade.Boot -container -container-name c0 -agents "mol:AgenteFileSystem(c1, read, prac4.pdf, 247567,0)" >> ./log/out.txt &

wait
pkill -9 -f "java"
