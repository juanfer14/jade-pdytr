import jade.org.*

import java.lang.IndexOutOfBoundsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class AgenteFileSystem extends Agent {

    /* UBICACIONES A DONDE DEBE IR EL AGENTE 
    *       origin: La ubicacion desde donde empieza
    *       destiny: La ubicacion a donde debe leer o escribir     
    */
    private Location origin;
    private Location destiny;
	
    /* ENTRADA */

    /* 
     *      name: Nombre del archivo a leer o escribir
     *      operation: Operacion a realizar 'read' o 'write'
     *      postion: Posicion del archivo a leer o escribir
     *      length: Cantidad de bytes solicitadas para leer o escribir
     */
    private String name;
    private String operation;
    private int position;
    private int length;
    
    /* SALIDA */

    /* DATOS EFECTIVAMENTE LEIDOS O ESCRITOS */
    private int effective;

    /* DATOS QUE SE RECIBEN DEL READ O DATOS QUE SE DEBEN ESCRIBIR EN EL WRITE  */
    private byte[] data;



    public void setup(){
        try {
            Object [] args = getArguments();

            if(args.length > 3 && args.length < 6){
                origin = here();
                destiny = new ContainerID((String) args[0]);
                operation = (String) args[1];
                name = (String) args[2];
                length = (int) args[3];

                /*  
                *       SI LA OPERACION ES 'READ', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_LEER, POSICION'
                */
                if(operation == "read" && length > 0 && args.length == 5){
                    position = (int) args[4];
                    doMove(destiny);   
                }
                /*  
                *       SI LA OPERACION ES 'WRITE', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_ESCRIBIR'
                */
                else if(operation == "write" && length > 0){
                    position = -1;
                    readFile(true);
                    doMove(destiny);
                }
                else throw new Exception(
                    "LOS ARGS. SON operacion (read o write), " + 
                    "nombre_archivo, posicion, cant_bytes (mayor a 0)");

            } else throw new Exception("LA CANTIDAD DE ARGS. ES INVALIDA");

        } catch (Exception e) {

        }
    }

    protected void afterMove() {
    }

    private void read(boolean local){
        try {
            File file = new File(local ? "local/" : "fs/" + name);
            FileInputStream fis = new FileInputStream(file);
            if(position != -1)
                fis.skip(position);
            int pending = Math.min(length, fis.available());
            data = new byte[pending];
            while(pending > 0){

            }
            byte[] bytesArray = new byte[Math.min(amount,fis.available())]; 
            int amountRead = fis.read(bytesArray);
            boolean finished = fis.available() == 0;
            fis.close();
            System.out.println("Se leyeron " + amountRead + " bytes de " + fileName);
        } catch (IOException e){
            throw new IOException("NO SE PUDO LEER EL ARCHIVO...");
        }
    }
}